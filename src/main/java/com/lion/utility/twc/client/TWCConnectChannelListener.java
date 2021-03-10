package com.lion.utility.twc.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.tool.CommonLIB;

/**
 * channel监听
 * 
 * @author lion
 *
 */
class TWCConnectChannelListener implements ChannelFutureListener {
	private TWCConnect twcConnect;

	public TWCConnectChannelListener(TWCConnect twcConnect) {
		this.twcConnect = twcConnect;
	}

	@Override
	public void operationComplete(ChannelFuture futureListener) {
		// 恢复状态，使得可以重连
		this.twcConnect.isDOConnect.set(false);

		if (futureListener.isSuccess()) {
			this.twcConnect.channel = futureListener.channel();
			// 设置标识
			this.twcConnect.channel.attr(Constant.TWC_CLIENT_CONNECT_ID).set(this.twcConnect.serverBasicInfo.getKey());
			LogLIB.info(this.twcConnect.getServerInfo() + ", connect to server succeed");

			// 客户端注册
			TWCMessage twcRequest = new TWCMessage();
			twcRequest.setMsgType(Constant.MESSAGE_TYPE_REQUEST);
			twcRequest.setMsgId(CommonLIB.getMsgId(this.twcConnect.twcClient.msgId));
			twcRequest.setMethodId(Constant.METHODID_REGISTER);
			twcRequest.setParamObj(this.twcConnect.twcClient.clientId);
			// 写入全局netty处理缓存，用于回调处理
			this.twcConnect.twcClient.methodSyncCache.put(twcRequest.getMsgId(), twcRequest.getMethodId());
			// 异步发送，若调用twcconnect中的handler，则会阻塞当前线程，导致问题
			this.twcConnect.channel.writeAndFlush(twcRequest);
		} else {
			LogLIB.error(this.twcConnect.getServerInfo() + ", failed to connect to server, try connect after " + Constant.TWC_RETRY_SECOND + "s");
			this.twcConnect.doDelayConnect();
		}
	}
}