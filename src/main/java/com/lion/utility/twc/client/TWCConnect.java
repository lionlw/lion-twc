package com.lion.utility.twc.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import com.lion.utility.tool.thread.CustomThreadFactory;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.framework.web.i.constant.IConstant;
import com.lion.utility.framework.web.i.entity.IResult;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.NettyTWCSyncResponse;
import com.lion.utility.twc.entity.TWCAddress;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.tool.CommonLIB;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TWC客户端链接类
 * 
 * @author lion
 *
 */
public class TWCConnect {
	// [start] 变量定义

	/**
	 * twc客户端
	 */
	protected TWCClient twcClient;
	/**
	 * 服务监听地址
	 */
	protected TWCAddress serverBasicInfo;

	/**
	 * 是否在执行twc连接动作
	 */
	protected AtomicBoolean isDOConnect = new AtomicBoolean(false);
	/**
	 * 是否启用
	 */
	protected AtomicBoolean isEnable = new AtomicBoolean(false);
	/**
	 * twc连接超时次数
	 */
	protected AtomicInteger connectTimeoutTotal = new AtomicInteger(0);

	protected Channel channel;

	/**
	 * twc连接延迟定时器
	 */
	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1, new CustomThreadFactory("TWCConnect", "TWCConnectConnectTimerTask"));

	// [end]

	/**
	 * 实例化
	 * 
	 * @param twcClient       twc客户端
	 * @param serverBasicInfo 地址
	 */
	public TWCConnect(TWCClient twcClient, TWCAddress serverBasicInfo) {
		this.twcClient = twcClient;
		this.serverBasicInfo = serverBasicInfo;

	}

	/**
	 * 启动
	 * 
	 * @throws Exception 异常
	 */
	public void start() throws Exception {
		this.isEnable.set(true);

		// 链接服务端
		this.doConnect();
	}

	/**
	 * 停止
	 */
	public void stop() {
		this.isEnable.set(false);

		// TOTO 实现停止连接，需要阻塞
		if (this.channel != null) {
			this.channel.close();
		}
	}

	/**
	 * 执行twc（同步返回处理结果）
	 * 
	 * @param twcRequest twc请求类
	 * @return 结果
	 */
	public TWCMessage handler(TWCMessage twcRequest) {
		TWCMessage twcResponse = new TWCMessage();
		twcResponse.setMsgType(Constant.MESSAGE_TYPE_RESPONSE);
		twcResponse.setiResult(new IResult<>());
		twcResponse.getiResult().setCode(IConstant.RETURN_CODE_TWCSYSTEM_ERROR);

		// 阻塞直到链接成功
		if (!this.waitSuccess(this.twcClient.twcClientConfig.getConnectTimeoutSecond())) {
			// 累加连接超时错误
			this.connectTimeoutTotal.incrementAndGet();
			twcResponse.getiResult().setMsg("connectTimeout");
			LogLIB.error(this.getServerInfo() + ", TWCConnect " + twcResponse.getiResult().toResultString() + ", connectTimeoutSecond > " + this.twcClient.twcClientConfig.getConnectTimeoutSecond() + "s, twcRequest:" + twcRequest.toString());
		} else {
			NettyTWCSyncResponse nettyTWCSyncResponse = null;
			try {
				nettyTWCSyncResponse = new NettyTWCSyncResponse();
				nettyTWCSyncResponse.setType(Constant.RESPONSE_TYPE_SINGLE);
				nettyTWCSyncResponse.setHaveClearMsg(false);
				nettyTWCSyncResponse.setLock(new ReentrantLock());
				nettyTWCSyncResponse.setLockCondition(nettyTWCSyncResponse.getLock().newCondition());
				nettyTWCSyncResponse.setIsDone(false);

				// 写入全局netty同步处理缓存
				this.twcClient.methodSyncCache.put(twcRequest.getMsgId(), nettyTWCSyncResponse);

				// 异步
				this.channel.writeAndFlush(twcRequest);

				// 阻塞等待返回结果
				twcResponse = CommonLIB.getSyncTWCResponse(
						nettyTWCSyncResponse,
						twcRequest.getReadTimeoutSecond(),
						this.getServerInfo(),
						twcRequest.toString());
			} catch (Exception e) {
				twcResponse.getiResult().setMsg("exception");
				LogLIB.error(this.getServerInfo() + ", TWCConnect " + twcResponse.getiResult().toResultString() + ", twcRequest:" + twcRequest.toString() + ", twcResponse:" + twcResponse.toString(), e);
			} finally {
				// 及时清理cache，降低内存消耗
				CommonLIB.clearMethodSyncCache(this.twcClient.methodSyncCache, nettyTWCSyncResponse, twcRequest.getMsgId());

				if (this.twcClient.twcClientConfig.getLogLevel() == Constant.LOGLEVEL_INOUTERROR) {
					LogLIB.twc(this.getServerInfo() + ", TWCConnect " + twcResponse.getiResult().toResultString() + ", twcRequest:" + twcRequest.toString() + ", twcResponse:" + twcResponse.toString());
				} else if (this.twcClient.twcClientConfig.getLogLevel() == Constant.LOGLEVEL_INERROR) {
					LogLIB.twc(this.getServerInfo() + ", TWCConnect " + twcResponse.getiResult().toResultString() + ", twcRequest:" + twcRequest.toString());
				}
			}
		}

		return twcResponse;
	}

	/**
	 * 获取服务信息
	 * 
	 * @return 结果
	 */
	protected String getServerInfo() {
		return this.twcClient.serviceName + "-" + this.serverBasicInfo.getIp() + ":" + this.serverBasicInfo.getPort();
	}

	/**
	 * 连接服务端
	 */
	protected void doConnect() {
		// 非启动状态，则退出，防止重连
		if (!this.isEnable.get()) {
			return;
		}

		ChannelFuture future = this.twcClient.bootstrap.connect(this.serverBasicInfo.getIp(), this.serverBasicInfo.getPort());
		// 监听链接事件
		future.addListener(new TWCConnectChannelListener(this));

		LogLIB.info(this.getServerInfo() + " doConnect");
	}

	/**
	 * 延迟连接机制,每隔指定时间重新连接一次服务器（加锁，防止并发重连）
	 */
	protected void doDelayConnect() {
		// 非启动状态，则退出，防止重连
		if (!this.isEnable.get()) {
			return;
		}

		// 若在执行，则抛弃此次请求
		if (this.isDOConnect.getAndSet(true)) {
			return;
		}

		// 延迟执行
		this.ses.schedule(new TWCConnectConnectTimerTask(this), Constant.TWC_RETRY_SECOND, TimeUnit.SECONDS);
	}

	/**
	 * 获取twc是否处于链接状态
	 * 
	 * @return 结果
	 */
	protected boolean isAlive() {
		if (this.channel != null && this.channel.isActive()) {
			return true;
		}

		return false;
	}

	/**
	 * 阻塞直到链接成功
	 * 
	 * @param connectTimeoutSecond 链接超时秒数
	 * @throws Exception
	 */
	private boolean waitSuccess(int connectTimeoutSecond) {
		int t = 0;

		while (!this.isAlive()) {
			try {
				t++;
				if (t > connectTimeoutSecond) {
					return false;
				}

				Thread.sleep(1000L);
			} catch (Exception e) {
				LogLIB.error("", e);
			}
		}

		return true;
	}
}