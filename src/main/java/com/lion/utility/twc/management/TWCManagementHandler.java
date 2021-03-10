package com.lion.utility.twc.management;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.tool.CommonLIB;

/**
 * 业务处理类
 * 
 * @author lion
 *
 */
class TWCManagementHandler extends SimpleChannelInboundHandler<TWCMessage> {
	private TWCManagement twcManagement;

	public TWCManagementHandler(TWCManagement twcManagement) {
		this.twcManagement = twcManagement;
	}

	/**
	 * 通道读取数据
	 */
	@Override
	public void channelRead0(ChannelHandlerContext ctx, TWCMessage twcMessage) {
		try {
			TWCMConnect twcmConnect = this.twcManagement.twcMConnectMap.get(ctx.channel().attr(Constant.TWC_CLIENT_CONNECT_ID).get());

			this.twcManagement.bizThreadPool.execute(new TWCManagementBizTask(ctx, twcMessage, twcmConnect));
		} catch (Exception e) {
			LogLIB.error(this.getLogPre(ctx) + ", channelRead exception, " + twcMessage.toString(), e);
		}
	}

	/**
	 * 通道激活动作
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
	}

	/**
	 * 异常处理
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LogLIB.error(this.getLogPre(ctx) + ", requestIp:" + CommonLIB.getNettyRequestIp(ctx) + ", exceptionCaught", cause);
		ctx.close();
	}

	/**
	 * 接收心跳检测结果,event.state()的状态分别对应上面三个参数的时间设置，当满足某个时间的条件时会触发事件。
	 */
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);

		try {
			if (evt instanceof IdleStateEvent) {
				IdleStateEvent event = (IdleStateEvent) evt;
				if (event.state().equals(IdleState.READER_IDLE)) {
					LogLIB.error(this.getLogPre(ctx) + ", long time not recieve server's data");

					// 长时间没有从服务端读取数据，则重连（关闭链接，后续转到channelInactive处理）
					ctx.close();
				} else if (event.state().equals(IdleState.WRITER_IDLE)) {
					if (this.twcManagement.twcManagementConfig.getIsDebug()) {
						LogLIB.twc(this.getLogPre(ctx) + ", long time not send data to server");
					}

					// 长时间没有发送数据到服务端，则发送心跳，保持连接
					TWCMessage twcRequest = new TWCMessage();
					twcRequest.setMsgType(Constant.MESSAGE_TYPE_MANAGEMENTREQUEST);
					twcRequest.setMsgId(CommonLIB.getMsgId(this.twcManagement.msgId));
					twcRequest.setMethodId(Constant.METHODID_HEARTBEAT);
					// 写入全局netty处理缓存，用于回调处理
					this.twcManagement.methodSyncCache.put(twcRequest.getMsgId(), twcRequest.getMethodId());
					// 异步发送心跳，若调用twcconnect中的handler，则会阻塞当前线程，导致问题
					ctx.writeAndFlush(twcRequest);

					if (this.twcManagement.twcManagementConfig.getIsDebug()) {
						LogLIB.twc(this.getLogPre(ctx) + ", heartbeat send, twcRequest:" + twcRequest.toString());
					}
				}
			}
		} catch (Exception e) {
			LogLIB.error("IdleStateEvent handler exception", e);
		}
	}

	/**
	 * 运行中连接断开时重试
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LogLIB.error(this.getLogPre(ctx) + ", channelInactive");

		super.channelInactive(ctx);

		// 指定twcconnect进行重试
		TWCMConnect twcmConnect = this.twcManagement.twcMConnectMap.get(ctx.channel().attr(Constant.TWC_CLIENT_CONNECT_ID).get());
		if (twcmConnect != null) {
			// 加个判断做保护，防止setRPCServer逻辑中，由于移除对象而导致空指针
			twcmConnect.doDelayConnect();
		}
	}

	/**
	 * 获取日志前缀
	 * 
	 * @param ctx
	 * @return
	 */
	private String getLogPre(ChannelHandlerContext ctx) {
		return this.twcManagement.serviceName + "-" + ctx.channel().attr(Constant.TWC_CLIENT_CONNECT_ID).get();
	}
}