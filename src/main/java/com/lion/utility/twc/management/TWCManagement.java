package com.lion.utility.twc.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import com.lion.utility.tool.common.Tool;
import com.lion.utility.tool.thread.CustomThreadFactory;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.TWCAddress;
import com.lion.utility.twc.entity.TWCManagementParamObj;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.entity.config.TWCManagementConfig;
import com.lion.utility.twc.tool.CommonLIB;
import com.lion.utility.tool.file.JsonLIB;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.framework.web.i.ILIB;
import com.lion.utility.framework.web.i.constant.IConstant;
import com.lion.utility.framework.web.i.entity.IResult;
import com.lion.utility.http.net.NetLIB;

/**
 * TWC服务端集群管理（连接server集群，对client进行发消息等操作，发送的是client的方法--）
 * 
 * @author lion
 *
 */
public class TWCManagement {
	// [start] 变量定义

	/**
	 * 管理端标识（全局唯一）
	 */
	protected String managementId;
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
	 * 服务地址（用于指定地址进行连接）
	 */
	protected TWCAddress serverAddress;

	/**
	 * twc链接对象map（key：链接的地址和端口，value：TWCMConnect对象）
	 */
	protected ConcurrentHashMap<String, TWCMConnect> twcMConnectMap = new ConcurrentHashMap<>();

	/**
	 * twc TWCMmanagement配置
	 */
	protected TWCManagementConfig twcManagementConfig;

	/**
	 * 业务处理线程池
	 */
	protected ExecutorService bizThreadPool;

	/**
	 * netty对象
	 */
	protected Bootstrap bootstrap;

	/**
	 * 用于同步方法结果上下文传递
	 */
	protected Cache<Integer, Object> methodSyncCache;

	/**
	 * 当前ip
	 */
	protected String ip;

	/**
	 * 客户端消息标识
	 */
	protected AtomicInteger msgId = new AtomicInteger(0);

	// [end]

	/**
	 * 基于ip,端口直连服务
	 * 
	 * @param clientId
	 *            客户端标识
	 * @param serverAddress
	 *            直连地址
	 */
	public TWCManagement(String clientId, TWCAddress serverAddress) {
		this(clientId, "", "", serverAddress);
	}

	/**
	 * 基于服务名，通过服务发现集群进行连接
	 * 
	 * @param managementId
	 *            管理端标识
	 * @param serviceName
	 *            目标服务名（用于服务发现）
	 * @param zkServerUrl
	 *            zookeeper服务器集群地址，192.168.36.54:2181,192.168.36.55:2181,192.168.36.56:2181
	 */
	public TWCManagement(String managementId, String serviceName, String zkServerUrl) {
		this(managementId, serviceName, zkServerUrl, null);
	}

	/**
	 * 初始化
	 * 
	 * @param managementId
	 *            管理端标识
	 * @param serviceName
	 *            目标服务名（用于服务发现）
	 * @param zkServerUrl
	 *            zookeeper服务器集群地址，192.168.36.54:2181,192.168.36.55:2181,192.168.36.56:2181
	 * @param serverAddress
	 *            直连地址
	 */
	private TWCManagement(String managementId, String serviceName, String zkServerUrl, TWCAddress serverAddress) {
		this.managementId = managementId;
		this.serviceName = serviceName;
		this.zkServerUrl = zkServerUrl;
		this.serverAddress = serverAddress;

		this.twcManagementConfig = new TWCManagementConfig();

		// 初始化cache
		this.methodSyncCache = Caffeine.newBuilder()
				.maximumSize(1000000)
				.expireAfterWrite(300, TimeUnit.SECONDS)
				.build();
	}

	/**
	 * 设置twc客户端配置（需要在调用start前设置）
	 * 
	 * @param twcManagementConfig
	 *            twc management配置
	 */
	public void setTWCManagementConfig(TWCManagementConfig twcManagementConfig) {
		this.twcManagementConfig = twcManagementConfig;
	}

	/**
	 * 启动
	 * 
	 * @throws Exception
	 *             异常
	 */
	public void start() throws Exception {
		// 打印配置信息
		LogLIB.info("twcManagementConfig: " + JsonLIB.toJson(this.twcManagementConfig));
		
		this.ip = NetLIB.getIp();

		this.bizThreadPool = Executors.newFixedThreadPool(
				this.twcManagementConfig.getBizThreadPoolThreadTotal(),
				new CustomThreadFactory("twcManagement", "biz"));

		// 初始化netty
		this.initNetty();

		// 进行连接
		TWCMServerDiscovery twcMServerDiscovery = new TWCMServerDiscovery(this);
		if (this.serverAddress != null) {
			twcMServerDiscovery.directHandler(this.serverAddress);
		} else {
			twcMServerDiscovery.zkHandler();
		}
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）-发给所有连接的客户端
	 * 
	 * @param methodId
	 *            方法标识
	 * @param paramObj
	 *            请求参数对象
	 * @param <T>
	 *            泛型
	 * @return 结果
	 * @throws Exception
	 *             异常
	 */
	public <T> IResult<Map<String, IResult<T>>> handlerAllClient(String methodId, Object paramObj) throws Exception {
		return this.handlerAllClient(methodId, paramObj, this.twcManagementConfig.getReadTimeoutSecond());
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）-发给所有连接的客户端
	 * 
	 * @param methodId
	 *            方法标识
	 * @param paramObj
	 *            请求参数对象
	 * @param readTimeoutSecond
	 *            读取超时秒数
	 * @param <T>
	 *            泛型
	 * @return 结果（key：clientId，value：结果）
	 * @throws Exception
	 *             异常
	 */
	public <T> IResult<Map<String, IResult<T>>> handlerAllClient(String methodId, Object paramObj, int readTimeoutSecond) throws Exception {
		if (this.twcMConnectMap.size() > 0) {
			// 每个server都与所有的client保持连接，因此随机即可
			TWCMConnect twcmConnect = this.getRndTWCMConnect();
			return this.handlerDo(twcmConnect, Constant.MANAGEREQUEST_TYPE_ALLCLIENTID, null, methodId, paramObj, readTimeoutSecond);
		} else {
			throw new Exception("server total is 0");
		}
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）-调用所有的server
	 * 
	 * @param clientIds
	 *            客户端标识列表
	 * @param methodId
	 *            方法标识
	 * @param paramObj
	 *            请求参数对象
	 * @param <T>
	 *            泛型
	 * @return 结果
	 * @throws Exception
	 *             异常
	 */
	public <T> IResult<Map<String, IResult<T>>> handlerClients(List<String> clientIds, String methodId, Object paramObj) throws Exception {
		return this.handlerClients(clientIds, methodId, paramObj, this.twcManagementConfig.getReadTimeoutSecond());
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）-调用所有的server
	 * 
	 * @param clientIds
	 *            客户端标识列表
	 * @param methodId
	 *            方法标识
	 * @param paramObj
	 *            请求参数对象
	 * @param readTimeoutSecond
	 *            读取超时秒数
	 * @param <T>
	 *            泛型
	 * @return 结果（key：serverId，value：结果map（key：clientId，value：结果））
	 * @throws Exception
	 *             异常
	 */
	public <T> IResult<Map<String, IResult<T>>> handlerClients(List<String> clientIds, String methodId, Object paramObj, int readTimeoutSecond) throws Exception {
		if (this.twcMConnectMap.size() > 0) {
			// 每个server都与所有的client保持连接，因此随机即可
			TWCMConnect twcmConnect = this.getRndTWCMConnect();
			return this.handlerDo(twcmConnect, Constant.MANAGEREQUEST_TYPE_CLIENTIDS, clientIds, methodId, paramObj, readTimeoutSecond);
		} else {
			throw new Exception("server total is 0");
		}
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）
	 * 
	 * @param clientId
	 *            客户端标识
	 * @param methodId
	 *            方法标识
	 * @param paramObj
	 *            请求参数对象
	 * @param <T>
	 *            泛型
	 * @return 结果
	 * @throws Exception
	 *             异常
	 */
	public <T> IResult<T> handlerClient(String clientId, String methodId, Object paramObj) throws Exception {
		return this.handlerClient(clientId, methodId, paramObj, this.twcManagementConfig.getReadTimeoutSecond());
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）
	 * 
	 * @param clientId
	 *            客户端标识
	 * @param methodId
	 *            方法标识
	 * @param paramObj
	 *            请求参数对象
	 * @param readTimeoutSecond
	 *            读取超时秒数
	 * @param <T>
	 *            泛型
	 * @return 结果
	 * @throws Exception
	 *             异常
	 */
	public <T> IResult<T> handlerClient(String clientId, String methodId, Object paramObj, int readTimeoutSecond) throws Exception {
		List<String> clientIds = new ArrayList<>();
		clientIds.add(clientId);

		IResult<Map<String, IResult<T>>> iResult = this.handlerClients(clientIds, methodId, paramObj, readTimeoutSecond);
		if (iResult.getCode().equals(IConstant.RETURN_CODE_SUCCEED) &&
				iResult.getData() != null && iResult.getData().size() > 0) {
			return iResult.getData().get(clientId);
		}

		return ILIB.getIResultFailed(IConstant.RETURN_CODE_PARM_ERROR, "no client");
	}

	/**
	 * 获取客户端标识列表
	 * 
	 * @return 结果
	 * @throws Exception
	 *             异常
	 */
	public List<String> getClientIds() throws Exception {
		if (this.twcMConnectMap.size() > 0) {
			// 每个server都与所有的client保持连接，因此随机即可
			TWCMConnect twcmConnect = this.getRndTWCMConnect();

			IResult<List<String>> iResult = this.handlerDo(
					twcmConnect,
					Constant.MANAGEREQUEST_TYPE_ALLCLIENTID,
					null,
					Constant.METHODID_GETCLIENTIDS,
					null,
					this.twcManagementConfig.getReadTimeoutSecond());
			return iResult.getData();
		} else {
			throw new Exception("server total is 0");
		}
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）
	 * 
	 * @param twcMConnect
	 *            server连接对象
	 * @param type
	 *            请求类型
	 * @param clientIds
	 *            客户端标识列表
	 * @param methodId
	 *            方法标识
	 * @param paramObj
	 *            请求参数对象
	 * @param readTimeoutSecond
	 *            读取超时秒数
	 * @param <T>
	 *            泛型
	 * @return 结果
	 * @throws Exception
	 *             异常
	 */
	@SuppressWarnings("unchecked")
	private <T> IResult<T> handlerDo(TWCMConnect twcMConnect, int type, List<String> clientIds, String methodId, Object paramObj, int readTimeoutSecond) throws Exception {
		TWCManagementParamObj twcManagementParamObj = new TWCManagementParamObj();
		twcManagementParamObj.setType(type);
		twcManagementParamObj.setClientIds(clientIds);
		twcManagementParamObj.setParamObj(paramObj);

		TWCMessage twcRequest = new TWCMessage();
		twcRequest.setReadTimeoutSecond(readTimeoutSecond);
		twcRequest.setMsgId(CommonLIB.getMsgId(this.msgId));
		twcRequest.setMsgType(Constant.MESSAGE_TYPE_MANAGEMENTREQUEST);
		twcRequest.setMethodId(methodId);
		twcRequest.setParamObj(twcManagementParamObj);

		return (IResult<T>) twcMConnect.handler(twcRequest).getiResult();
	}

	/**
	 * 初始化netty
	 */
	private void initNetty() {
		try {
			EventLoopGroup ioGroup = null; // 后续不做关闭，以便可以不停重试链接
			Class<? extends Channel> clientChannel = null;
			String logpre = "";

			if (Epoll.isAvailable()) {
				ioGroup = new EpollEventLoopGroup(this.twcManagementConfig.getIoThreads(), new DefaultThreadFactory("TWCClient_" + this.serviceName, true));
				clientChannel = EpollSocketChannel.class;
				logpre = "start-Epoll";
			} else {
				ioGroup = new NioEventLoopGroup(this.twcManagementConfig.getIoThreads(), new DefaultThreadFactory("TWCClient_" + this.serviceName, true));
				clientChannel = NioSocketChannel.class;
				logpre = "start-Nio";
			}

			this.bootstrap = new Bootstrap();
			this.bootstrap.group(ioGroup)
					.channel(clientChannel)
					.handler(new TWCManagementChannelInit(this));

			LogLIB.info(this.serviceName + ", " + logpre + " initNetty complete");
		} catch (Exception e) {
			LogLIB.error(this.serviceName + ", initNetty exception", e);
		}
	}

	/**
	 * 获取随机TWCMConnect对象
	 * 
	 * @return 结果
	 * @throws Exception
	 *             异常
	 */
	private TWCMConnect getRndTWCMConnect() throws Exception {
		List<String> keys = Collections.list(this.twcMConnectMap.keys());
		String key = keys.get(Tool.getRndnum(0, keys.size() - 1));
		return this.twcMConnectMap.get(key);
	}
}
