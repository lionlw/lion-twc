package com.lion.utility.twc.server;

import java.net.InetSocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.AbstractRemoteAddressFilter;
import com.lion.utility.tool.common.Tool;
import com.lion.utility.tool.log.LogLIB;

/**
 * ip防火墙（用于server）
 * 
 * @author lion
 *
 */
class IPFilterFirewall extends AbstractRemoteAddressFilter<InetSocketAddress> {
	private TWCServer twcServer;

	public IPFilterFirewall(TWCServer twcServer) {
		this.twcServer = twcServer;
	}

	@Override
	protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
		if (!Tool.checkHaveValue(this.twcServer.twcServerConfig.getRequestIPSet())) {
			return true;
		}

		if (this.twcServer.twcServerConfig.getRequestIPSet().contains(remoteAddress.getAddress().getHostAddress())) {
			return true;
		}

		LogLIB.error("IPFilterFirewall failed, ip:" + remoteAddress.getAddress().getHostAddress());
		return false;
	}

}