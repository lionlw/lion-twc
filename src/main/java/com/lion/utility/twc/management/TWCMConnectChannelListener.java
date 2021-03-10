package com.lion.utility.twc.management;

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
class TWCMConnectChannelListener implements ChannelFutureListener {
	private TWCMConnect twcMConnect;

	public TWCMConnectChannelListener(TWCMConnect twcMConnect) {
		this.twcMConnect = twcMConnect;
	}

	@Override
	public void operationComplete(ChannelFuture futureListener) {
		// 恢复状态，使得可以重连
		this.twcMConnect.isDOConnect.set(false);

		if (futureListener.isSuccess()) {
			this.twcMConnect.channel = futureListener.channel();
			// 设置标识
			this.twcMConnect.channel.attr(Constant.TWC_CLIENT_CONNECT_ID).set(this.twcMConnect.serverBasicInfo.getKey());
			LogLIB.info(this.twcMConnect.getServerInfo() + ", connect to server succeed");

			// 客户端注册
			TWCMessage twcRequest = new TWCMessage();
			twcRequest.setMsgType(Constant.MESSAGE_TYPE_MANAGEMENTREQUEST);
			twcRequest.setMsgId(CommonLIB.getMsgId(this.twcMConnect.twcManagement.msgId));
			twcRequest.setMethodId(Constant.METHODID_REGISTER);
			twcRequest.setParamObj(this.twcMConnect.twcManagement.managementId);
			// 写入全局netty处理缓存，用于回调处理
			this.twcMConnect.twcManagement.methodSyncCache.put(twcRequest.getMsgId(), twcRequest.getMethodId());
			// 异步发送，若调用twcconnect中的handler，则会阻塞当前线程，导致问题
			this.twcMConnect.channel.writeAndFlush(twcRequest);
		} else {
			LogLIB.error(this.twcMConnect.getServerInfo() + ", failed to connect to server, try connect after " + Constant.TWC_RETRY_SECOND + "s");
			this.twcMConnect.doDelayConnect();
		}
	}
}