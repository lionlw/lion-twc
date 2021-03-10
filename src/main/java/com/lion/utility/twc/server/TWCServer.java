package com.lion.utility.twc.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.netty.channel.ChannelHandlerContext;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.framework.web.i.ILIB;
import com.lion.utility.framework.web.i.constant.IConstant;
import com.lion.utility.framework.web.i.entity.IResult;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.NettyTWCSyncResponse;
import com.lion.utility.twc.entity.TWCAddress;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.entity.config.TWCServerConfig;
import com.lion.utility.twc.tool.CommonLIB;
import com.lion.utility.twc.tool.IHandlerMessage;
import com.lion.utility.tool.file.JsonLIB;

/**
 * 注意点
 * 
 * 双向通信使用场景（基于tcp）：
 * 1、leaf：一个主，多个从。主分发任务，从执行，然后从可以将生成的文件汇总到主，主再分发给每个从。 
 * 2、任务调度：一个主，多个从。主从连接后，可以双向通信。 
 * 3、消息推送：多个主，多个从。消息群发，服务端分发所有客户端。
 */

/**
 * TWC服务端
 * 
 * @author lion
 *
 */
public class TWCServer {
	// [start] 变量定义

	/**
	 * 服务名（注册到服务发现集群中，必须全平台唯一）
	 */
	protected String serviceName;
	/**
	 * zookeeper服务器集群地址
	 * 空表示不采用zk做服务发现，192.168.36.54:2181,192.168.36.55:2181,192.168.36.56:2181
	 */
	protected String zkServerUrl;
	/**
	 * 服务监听地址
	 */
	protected TWCAddress serverAddress;
	/**
	 * 消息处理类
	 */
	protected IHandlerMessage iHandlerMessage;

	/**
	 * twc 服务端配置
	 */
	protected TWCServerConfig twcServerConfig;

	/**
	 * 业务处理线程池
	 */
	protected ExecutorService bizThreadPool;

	/**
	 * 用于同步方法结果上下文传递
	 */
	protected Cache<Integer, Object> methodSyncCache;

	/**
	 * 客户端ip端口与客户端标识映射
	 */
	protected ConcurrentHashMap<String, String> clientIPPortClientIdMap = new ConcurrentHashMap<>();
	/**
	 * 客户端标识与客户端通道映射
	 */
	protected ConcurrentHashMap<String, ChannelHandlerContext> clientIdCtxMap = new ConcurrentHashMap<>();

	/**
	 * 消息标识
	 */
	protected AtomicInteger msgId = new AtomicInteger(0);

	// [end]

	/**
	 * 基于ip,端口启动服务
	 * 
	 * @param serverAddress   服务地址
	 * @param iHandlerMessage 消息处理类
	 * @throws Exception 异常
	 */
	public TWCServer(TWCAddress serverAddress, IHandlerMessage iHandlerMessage) throws Exception {
		this("", "", serverAddress, iHandlerMessage);
	}

	/**
	 * 基于服务名，通过服务发现集群进行注册
	 * 
	 * @param serviceName     服务名（用于服务发现）
	 * @param zkServerUrl     zookeeper服务器集群地址
	 * @param serverAddress   服务地址
	 * @param iHandlerMessage 消息处理类
	 * @throws Exception 异常
	 */
	public TWCServer(String serviceName, String zkServerUrl, TWCAddress serverAddress, IHandlerMessage iHandlerMessage) throws Exception {
		this.serviceName = serviceName;
		this.zkServerUrl = zkServerUrl;
		this.serverAddress = serverAddress;
		this.iHandlerMessage = iHandlerMessage;

		this.twcServerConfig = new TWCServerConfig();

		// 初始化cache
		this.methodSyncCache = Caffeine.newBuilder()
				.maximumSize(1000000)
				.expireAfterWrite(300, TimeUnit.SECONDS)
				.build();
	}

	/**
	 * 设置twc服务端配置（需要在调用start前设置）
	 * 
	 * @param twcServerConfig twc服务端配置
	 */
	public void setTWCServerConfig(TWCServerConfig twcServerConfig) {
		this.twcServerConfig = twcServerConfig;
	}

	/**
	 * 启动
	 * 
	 * @throws Exception 异常
	 */
	public void start() throws Exception {
		// 打印配置信息
		LogLIB.info("twcServerConfig: " + JsonLIB.toJson(this.twcServerConfig));

		// 异步启动服务，防止阻塞
		new Thread(new TWCServerStartThread(this), "TWCServer-TWCServerStartThread").start();
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）-发给所有连接的客户端
	 * 
	 * @param methodId 方法标识
	 * @param paramObj 请求参数对象
	 * @param <T>      泛型
	 * @return 结果
	 * @throws Exception 异常
	 */
	public <T> Map<String, IResult<T>> handlerAllClient(String methodId, Object paramObj) throws Exception {
		return this.handlerAllClient(methodId, paramObj, this.twcServerConfig.getReadTimeoutSecond());
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）-发给所有连接的客户端
	 * 
	 * @param methodId          方法标识
	 * @param paramObj          请求参数对象
	 * @param readTimeoutSecond 读取超时秒数
	 * @param <T>               泛型
	 * @return 结果（key：clientId，value：结果）
	 * @throws Exception 异常
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<String, IResult<T>> handlerAllClient(String methodId, Object paramObj, int readTimeoutSecond) throws Exception {
		CopyOnWriteArrayList<NettyTWCSyncResponse> nettyTWCSyncResponses = new CopyOnWriteArrayList<>();

		if (this.clientIdCtxMap.size() > 0) {
			// 此处发送给客户端执行为异步处理，通过阀门阻塞获取最终结果
			CountDownLatch latch = new CountDownLatch(this.clientIdCtxMap.size());

			for (Entry<String, ChannelHandlerContext> entry : this.clientIdCtxMap.entrySet()) {
				this.handlerDo(entry.getKey(), entry.getValue(), methodId, paramObj, readTimeoutSecond, nettyTWCSyncResponses, latch);
			}

			latch.await();
		}

		Map<String, IResult<T>> map = new HashMap<>();
		for (NettyTWCSyncResponse nettyTWCSyncResponse : nettyTWCSyncResponses) {
			IResult<T> iResult = (IResult<T>) nettyTWCSyncResponse.getTwcResponse().getiResult();
			map.put(nettyTWCSyncResponse.getClientId(), iResult);

			if (!iResult.getCode().equals(IConstant.RETURN_CODE_SUCCEED)) {
				LogLIB.error(this.getServerInfo() + ", " + iResult.toResultString() + ", twcRequest:" + nettyTWCSyncResponse.getTwcRequest().toString() + ", twcResponse:" + nettyTWCSyncResponse.getTwcResponse().toString());
			}

			if (this.twcServerConfig.getLogLevel() == Constant.LOGLEVEL_INOUTERROR) {
				LogLIB.twc(this.getServerInfo() + ", TWCConnect " + iResult.toResultString() + ", twcRequest:" + nettyTWCSyncResponse.getTwcRequest().toString() + ", twcResponse:" + nettyTWCSyncResponse.getTwcResponse().toString());
			} else if (this.twcServerConfig.getLogLevel() == Constant.LOGLEVEL_INERROR) {
				LogLIB.twc(this.getServerInfo() + ", TWCConnect " + iResult.toResultString() + ", twcRequest:" + nettyTWCSyncResponse.getTwcRequest().toString());
			}
		}
		return map;
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）
	 * 
	 * @param clientIds 客户端标识列表
	 * @param methodId  方法标识
	 * @param paramObj  请求参数对象
	 * @param <T>       泛型
	 * @return 结果
	 * @throws Exception 异常
	 */
	public <T> Map<String, IResult<T>> handlerClients(List<String> clientIds, String methodId, Object paramObj) throws Exception {
		return this.handlerClients(clientIds, methodId, paramObj, this.twcServerConfig.getReadTimeoutSecond());
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）
	 * 
	 * @param clientIds         客户端标识列表
	 * @param methodId          方法标识
	 * @param paramObj          请求参数对象
	 * @param readTimeoutSecond 读取超时秒数
	 * @param <T>               泛型
	 * @return 结果（key：clientId，value：结果）
	 * @throws Exception 异常
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<String, IResult<T>> handlerClients(List<String> clientIds, String methodId, Object paramObj, int readTimeoutSecond) throws Exception {
		CopyOnWriteArrayList<NettyTWCSyncResponse> nettyTWCSyncResponses = new CopyOnWriteArrayList<>();

		if (this.clientIdCtxMap.size() > 0) {
			// 此处发送给客户端执行为异步处理，通过阀门阻塞获取最终结果
			CountDownLatch latch = new CountDownLatch(clientIds.size());

			for (String clientId : clientIds) {
				ChannelHandlerContext ctx = this.clientIdCtxMap.get(clientId);
				if (ctx != null) {
					this.handlerDo(clientId, ctx, methodId, paramObj, readTimeoutSecond, nettyTWCSyncResponses, latch);
				} else {
					NettyTWCSyncResponse nettyTWCSyncResponse = new NettyTWCSyncResponse();
					nettyTWCSyncResponse.setType(Constant.RESPONSE_TYPE_BATCH);
					nettyTWCSyncResponse.setHaveClearMsg(false);
					nettyTWCSyncResponse.setClientId(clientId);
					nettyTWCSyncResponse.setLatch(latch);

					TWCMessage twcResponse = new TWCMessage();
					twcResponse.setMsgType(Constant.MESSAGE_TYPE_RESPONSE);
					twcResponse.setiResult(new IResult<>());
					twcResponse.getiResult().setCode(IConstant.RETURN_CODE_TWCSYSTEM_ERROR);
					twcResponse.getiResult().setMsg(clientId + " invalid");
					nettyTWCSyncResponse.setTwcResponse(twcResponse);

					nettyTWCSyncResponses.add(nettyTWCSyncResponse);

					latch.countDown();
				}
			}

			latch.await();
		}

		Map<String, IResult<T>> map = new HashMap<>();
		for (NettyTWCSyncResponse nettyTWCSyncResponse : nettyTWCSyncResponses) {
			IResult<T> iResult = (IResult<T>) nettyTWCSyncResponse.getTwcResponse().getiResult();
			map.put(nettyTWCSyncResponse.getClientId(), iResult);

			if (!iResult.getCode().equals(IConstant.RETURN_CODE_SUCCEED)) {
				LogLIB.error(this.getServerInfo() + ", " + iResult.toResultString() + ", twcRequest:" + nettyTWCSyncResponse.getTwcRequest().toString() + ", twcResponse:" + nettyTWCSyncResponse.getTwcResponse().toString());
			}

			if (this.twcServerConfig.getLogLevel() == Constant.LOGLEVEL_INOUTERROR) {
				LogLIB.twc(this.getServerInfo() + ", TWCConnect " + iResult.toResultString() + ", twcRequest:" + nettyTWCSyncResponse.getTwcRequest().toString() + ", twcResponse:" + nettyTWCSyncResponse.getTwcResponse().toString());
			} else if (this.twcServerConfig.getLogLevel() == Constant.LOGLEVEL_INERROR) {
				LogLIB.twc(this.getServerInfo() + ", TWCConnect " + iResult.toResultString() + ", twcRequest:" + nettyTWCSyncResponse.getTwcRequest().toString());
			}
		}
		return map;
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）
	 * 
	 * @param clientId 客户端标识
	 * @param methodId 方法标识
	 * @param paramObj 请求参数对象
	 * @param <T>      泛型
	 * @return 结果
	 * @throws Exception 异常
	 */
	public <T> IResult<T> handlerClient(String clientId, String methodId, Object paramObj) throws Exception {
		return this.handlerClient(clientId, methodId, paramObj, this.twcServerConfig.getReadTimeoutSecond());
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）
	 * 
	 * @param clientId          客户端标识
	 * @param methodId          方法标识
	 * @param paramObj          请求参数对象
	 * @param readTimeoutSecond 读取超时秒数
	 * @param <T>               泛型
	 * @return 结果（key：clientId，value：结果）
	 * @throws Exception 异常
	 */
	public <T> IResult<T> handlerClient(String clientId, String methodId, Object paramObj, int readTimeoutSecond) throws Exception {
		List<String> clientIds = new ArrayList<>();
		clientIds.add(clientId);

		Map<String, IResult<T>> resultMap = this.handlerClients(clientIds, methodId, paramObj, readTimeoutSecond);
		if (resultMap.size() == 0) {
			return ILIB.getIResultFailed(IConstant.RETURN_CODE_PARM_ERROR, "no client");
		}

		return resultMap.get(clientId);
	}

	/**
	 * 获取客户端标识列表（由于client是每台server都保持连接，因此此处获取的就是全量的clientid）
	 * 
	 * @return 结果
	 * @throws Exception 异常
	 */
	public List<String> getClientIds() throws Exception {
		int size = this.clientIPPortClientIdMap.size();
		if (size > 100000) {
			throw new Exception("clientId count is " + size + ", so bigger, can't get");
		}

		return new ArrayList<>(this.clientIPPortClientIdMap.values());
	}

	/**
	 * 获取服务信息
	 * 
	 * @return 结果
	 */
	protected String getServerInfo() {
		return this.serviceName + "-" + this.serverAddress.getIp() + ":" + this.serverAddress.getPort();
	}

	/**
	 * 执行twc发送消息
	 * 
	 * @param ctx                   客户端通道
	 * @param methodId              方法标识
	 * @param paramObj              请求参数对象
	 * @param readTimeoutSecond     读取超时秒数
	 * @param nettyTWCSyncResponses 结果集合
	 * @param latch                 阀门
	 * @throws Exception 异常
	 */
	private void handlerDo(String clientId, ChannelHandlerContext ctx, String methodId, Object paramObj,
			int readTimeoutSecond, CopyOnWriteArrayList<NettyTWCSyncResponse> nettyTWCSyncResponses, CountDownLatch latch) throws Exception {
		TWCMessage twcRequest = new TWCMessage();
		twcRequest.setReadTimeoutSecond(readTimeoutSecond);
		twcRequest.setMsgType(Constant.MESSAGE_TYPE_REQUEST);
		twcRequest.setMsgId(CommonLIB.getMsgId(this.msgId));
		twcRequest.setMethodId(methodId);
		twcRequest.setParamObj(paramObj);

		// 直接使用通道，不需要等待连接成功

		NettyTWCSyncResponse nettyTWCSyncResponse = new NettyTWCSyncResponse();
		nettyTWCSyncResponse.setType(Constant.RESPONSE_TYPE_BATCH);
		nettyTWCSyncResponse.setHaveClearMsg(false);
		nettyTWCSyncResponse.setClientId(clientId);
		nettyTWCSyncResponse.setLatch(latch);
		nettyTWCSyncResponse.setTwcRequest(twcRequest);

		nettyTWCSyncResponses.add(nettyTWCSyncResponse);

		try {
			// 写入全局netty同步处理缓存
			this.methodSyncCache.put(twcRequest.getMsgId(), nettyTWCSyncResponse);

			// 异步
			ctx.writeAndFlush(twcRequest);
		} catch (Exception e) {
			TWCMessage twcResponse = new TWCMessage();
			twcResponse.setMsgType(Constant.MESSAGE_TYPE_RESPONSE);
			twcResponse.setiResult(new IResult<>());
			twcResponse.getiResult().setCode(IConstant.RETURN_CODE_TWCSYSTEM_ERROR);
			twcResponse.getiResult().setMsg("exception");
			nettyTWCSyncResponse.setTwcResponse(twcResponse);

			LogLIB.error(this.getServerInfo() + ", TWCServer " + twcResponse.getiResult().toResultString() + ", twcRequest:" + twcRequest.toString() + ", twcResponse:" + twcResponse.toString(), e);
		}
	}
}
