package com.lion.utility.twc.management;

import io.netty.channel.ChannelHandlerContext;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.NettyTWCSyncResponse;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.tool.CommonLIB;
import com.lion.utility.tool.log.LogLIB;

/**
 * 业务逻辑处理线程
 * 
 * @author lion
 *
 */
class TWCManagementBizTask implements Runnable {
	private ChannelHandlerContext ctx;
	private TWCMessage twcMessage;
	private TWCMConnect twcmConnect;

	public TWCManagementBizTask(ChannelHandlerContext ctx, TWCMessage twcMessage, TWCMConnect twcmConnect) {
		this.ctx = ctx;
		this.twcMessage = twcMessage;
		this.twcmConnect = twcmConnect;
	}

	@Override
	public void run() {
		// 只处理响应，server不会请求management
		if (this.twcMessage.getMsgType() == Constant.MESSAGE_TYPE_RESPONSE) {
			this.handlerTWCServerResponse(this.ctx, this.twcMessage);
		}
	}

	/**
	 * 处理server的响应
	 * 
	 * @param ctx         通道
	 * @param twcResponse 响应
	 */
	private void handlerTWCServerResponse(ChannelHandlerContext ctx, TWCMessage twcResponse) {
		try {
			Object obj = this.twcmConnect.twcManagement.methodSyncCache.getIfPresent(twcResponse.getMsgId());
			if (obj != null) {
				if ((obj instanceof NettyTWCSyncResponse)) {
					// 设置指定任务阻塞结束，并写入任务执行结果
					NettyTWCSyncResponse nettyTWCSyncResponse = (NettyTWCSyncResponse) obj;
					nettyTWCSyncResponse.setTwcResponse(twcResponse);

					if (nettyTWCSyncResponse.getType() == Constant.RESPONSE_TYPE_SINGLE) {
						try {
							// 尝试获取锁
							nettyTWCSyncResponse.getLock().lock();
							// 设置twc完成
							nettyTWCSyncResponse.setIsDone(true);
							// 唤醒等待线程
							nettyTWCSyncResponse.getLockCondition().signal();
						} finally {
							// 结束则释放锁
							nettyTWCSyncResponse.getLock().unlock();

							// 消息接收成功，清理cache，降低内存消耗
							CommonLIB.clearMethodSyncCache(this.twcmConnect.twcManagement.methodSyncCache, nettyTWCSyncResponse, twcResponse.getMsgId());
						}
					}
				} else if (obj instanceof String) {
					// twc内部工具方法
					String methodId = (String) obj;
					if (methodId.equals(Constant.METHODID_HEARTBEAT)) {
						// 心跳无需做业务处理
						if (this.twcmConnect.twcManagement.twcManagementConfig.getIsDebug()) {
							LogLIB.twc(this.twcmConnect.getServerInfo() + ", management heartbeat recieve, twcResponse:" + twcResponse.toString());
						}

						// 消息接收成功，清理cache，降低内存消耗
						this.twcmConnect.twcManagement.methodSyncCache.invalidate(twcResponse.getMsgId());
					} else if (methodId.equals(Constant.METHODID_REGISTER)) {
						// 管理端端注册无需做业务处理
						LogLIB.info(this.twcmConnect.getServerInfo() + ", management register complete, twcResponse:" + twcResponse.toString());

						// 消息接收成功，清理cache，降低内存消耗
						this.twcmConnect.twcManagement.methodSyncCache.invalidate(twcResponse.getMsgId());
					} else {
						LogLIB.error(this.twcmConnect.getServerInfo() + ", invalid methodId, " + obj.toString() + ", twcResponse:" + twcResponse.toString());
					}
				} else {
					LogLIB.error(this.twcmConnect.getServerInfo() + ", obj isn't NettyTWCSyncResponse, " + obj.toString() + ", twcResponse:" + twcResponse.toString());
				}
			} else {
				LogLIB.error(this.twcmConnect.getServerInfo() + ", msg not found, obj is null, twcResponse:" + twcResponse.toString());
			}
		} catch (Exception e) {
			LogLIB.error(this.twcmConnect.getServerInfo() + ", handlerTWCResponse exception, " + twcResponse.toString(), e);
		}
	}
}
