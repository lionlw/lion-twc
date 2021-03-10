package com.lion.utility.twc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.tool.common.Tool;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.tool.CommonLIB;

/**
 * 业务处理类
 * 
 * @author lion
 *
 */
class TWCServerHandler extends SimpleChannelInboundHandler<TWCMessage> {
	private TWCServer twcServer;

	public TWCServerHandler(TWCServer twcServer) {
		this.twcServer = twcServer;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, TWCMessage twcMessage) throws Exception {
		try {
			// 业务线程池处理
			this.twcServer.bizThreadPool.execute(new TWCServerBizTask(ctx, twcMessage, this.twcServer));
		} catch (Exception e) {
			LogLIB.error(this.twcServer.getServerInfo() + ", channelRead exception, " + twcMessage.toString(), e);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LogLIB.error(this.twcServer.getServerInfo() + ", requestIp:" + CommonLIB.getNettyRequestIp(ctx) + ", exceptionCaught", cause);
		ctx.close();
	}

	/**
	 * 运行中连接断开时，清理资源
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		String ipport = CommonLIB.getNettyRequestIp(ctx);

		// 清理通道map
		String clientId = this.twcServer.clientIPPortClientIdMap.get(ipport);
		this.twcServer.clientIPPortClientIdMap.remove(ipport);
		if (Tool.checkHaveValue(clientId)) {
			this.twcServer.clientIdCtxMap.remove(clientId);
		}

		LogLIB.error(ipport + ", channelInactive, clean complete");

		super.channelInactive(ctx);
	}
}