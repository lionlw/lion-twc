package com.lion.utility.twc.client;

import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.framework.web.i.entity.IResult;
import com.lion.utility.tool.thread.CustomThreadFactory;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.TWCAddress;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.entity.config.TWCClientConfig;
import com.lion.utility.twc.tool.CommonLIB;
import com.lion.utility.twc.tool.IHandlerMessage;
import com.lion.utility.tool.file.JsonLIB;
import com.lion.utility.http.net.NetLIB;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

/**
 * TWC客户端
 * 
 * @author lion
 *
 */
public class TWCClient {
	// [start] 变量定义

	/**
	 * 客户端标识（全局唯一）
	 */
	protected String clientId;
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
	 * 消息处理类
	 */
	protected IHandlerMessage iHandlerMessage;

	/**
	 * twc链接对象map（key：链接的地址和端口，value：TWCConnect对象）
	 */
	protected ConcurrentHashMap<String, TWCConnect> twcConnectMap = new ConcurrentHashMap<>();
	/**
	 * twc链接对象数组（用于负载均衡，快速查找）
	 */
	protected CopyOnWriteArrayList<TWCConnect> twcConnects = new CopyOnWriteArrayList<>();
	/**
	 * twc连接超时禁止名单（key：链接的地址和端口）
	 */
	protected Cache<String, String> twcConnectTimeoutCache;

	/**
	 * twc client配置
	 */
	protected TWCClientConfig twcClientConfig;

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
	 * @param iHandlerMessage
	 *            消息处理类
	 */
	public TWCClient(String clientId, TWCAddress serverAddress, IHandlerMessage iHandlerMessage) {
		this(clientId, "", "", serverAddress, iHandlerMessage);
	}

	/**
	 * 基于服务名，通过服务发现集群进行连接
	 * 
	 * @param clientId
	 *            客户端标识
	 * @param serviceName
	 *            目标服务名（用于服务发现）
	 * @param zkServerUrl
	 *            zookeeper服务器集群地址，空表示不采用zk做服务发现，192.168.36.54:2181,192.168.36.55:2181,192.168.36.56:2181
	 * @param iHandlerMessage
	 *            消息处理类
	 */
	public TWCClient(String clientId, String serviceName, String zkServerUrl, IHandlerMessage iHandlerMessage) {
		this(clientId, serviceName, zkServerUrl, null, iHandlerMessage);
	}

	/**
	 * 初始化
	 * 
	 * @param clientId
	 *            客户端标识
	 * @param serviceName
	 *            目标服务名（用于服务发现）
	 * @param zkServerUrl
	 *            zookeeper服务器集群地址
	 * @param serverAddress
	 *            直连地址
	 * @param iHandlerMessage
	 *            消息处理类
	 */
	private TWCClient(String clientId, String serviceName, String zkServerUrl, TWCAddress serverAddress, IHandlerMessage iHandlerMessage) {
		this.clientId = clientId;
		this.serviceName = serviceName;
		this.zkServerUrl = zkServerUrl;
		this.serverAddress = serverAddress;
		this.iHandlerMessage = iHandlerMessage;

		this.twcClientConfig = new TWCClientConfig();

		// 初始化cache
		this.methodSyncCache = Caffeine.newBuilder()
				.maximumSize(1000000)
				.expireAfterWrite(300, TimeUnit.SECONDS)
				.build();

		// 初始化twc连接超时禁止名单
		this.twcConnectTimeoutCache = Caffeine.newBuilder()
				.maximumSize(1000)
				.expireAfterWrite(this.twcClientConfig.getConnectTimeoutForbidIntervalSecond(), TimeUnit.SECONDS)
				.build();
	}

	/**
	 * 设置twc客户端配置（需要在调用start前设置）
	 * 
	 * @param twcClientConfig
	 *            twc客户端配置
	 */
	public void setTWCClientConfig(TWCClientConfig twcClientConfig) {
		this.twcClientConfig = twcClientConfig;
	}

	/**
	 * 启动
	 * 
	 * @throws Exception
	 *             异常
	 */
	public void start() throws Exception {
		// 打印配置信息
		LogLIB.info("twcClientConfig: " + JsonLIB.toJson(this.twcClientConfig));
		
		this.ip = NetLIB.getIp();

		this.bizThreadPool = Executors.newFixedThreadPool(
				this.twcClientConfig.getBizThreadPoolThreadTotal(),
				new CustomThreadFactory("twcClient", "biz"));

		// 初始化连接超时处理
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1, new CustomThreadFactory("TWCClientConnectTimeoutManage", "TWCClientConnectTimeoutManageTimerTask"));
		ses.scheduleAtFixedRate(new TWCClientConnectTimeoutManageTimerTask(this), 10L, this.twcClientConfig.getConnectTimeoutCheckIntervalSecond(), TimeUnit.SECONDS);

		// 初始化netty
		this.initNetty();

		// 进行连接
		TWCServerDiscovery twcServerDiscovery = new TWCServerDiscovery(this);
		if (this.serverAddress != null) {
			twcServerDiscovery.directHandler(this.serverAddress);
		} else {
			twcServerDiscovery.zkHandler();
		}
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）
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
	public <T> IResult<T> handler(String methodId, Object paramObj) throws Exception {
		return this.handler(methodId, paramObj, this.twcClientConfig.getReadTimeoutSecond());
	}

	/**
	 * 执行twc发送消息（同步返回处理结果）
	 * 
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
	public <T> IResult<T> handler(String methodId, Object paramObj, int readTimeoutSecond) throws Exception {
		TWCMessage twcRequest = new TWCMessage();
		twcRequest.setReadTimeoutSecond(readTimeoutSecond);
		twcRequest.setMsgId(CommonLIB.getMsgId(this.msgId));
		twcRequest.setMsgType(Constant.MESSAGE_TYPE_REQUEST);
		twcRequest.setMethodId(methodId);
		twcRequest.setParamObj(paramObj);

		if (this.twcConnects.size() > 1) {
			int index = this.getLoadBalance();
			return (IResult<T>) this.twcConnects.get(index).handler(twcRequest).getiResult();
		} else if (this.twcConnects.size() == 1) {
			// 只有一个server，不执行任何策略
			return (IResult<T>) this.twcConnects.get(0).handler(twcRequest).getiResult();
		} else {
			throw new Exception("server total is 0");
		}
	}

	/**
	 * 执行负载均衡
	 * 
	 * @return
	 * @throws Exception
	 */
	private int getLoadBalance() throws Exception {
		return LoadBalance.random(this.twcConnects.size());
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
				ioGroup = new EpollEventLoopGroup(this.twcClientConfig.getIoThreads(), new DefaultThreadFactory("TWCClient_" + this.serviceName, true));
				clientChannel = EpollSocketChannel.class;
				logpre = "start-Epoll";
			} else {
				ioGroup = new NioEventLoopGroup(this.twcClientConfig.getIoThreads(), new DefaultThreadFactory("TWCClient_" + this.serviceName, true));
				clientChannel = NioSocketChannel.class;
				logpre = "start-Nio";
			}

			this.bootstrap = new Bootstrap();
			this.bootstrap.group(ioGroup)
					.channel(clientChannel)
					.handler(new TWCClientChannelInit(this));

			LogLIB.info(this.serviceName + ", " + logpre + " initNetty complete");
		} catch (Exception e) {
			LogLIB.error(this.serviceName + ", initNetty exception", e);
		}
	}
}