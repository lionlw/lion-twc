package com.lion.utility.twc.server;

import java.util.concurrent.Executors;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import com.lion.utility.tool.common.Tool;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.tool.thread.CustomThreadFactory;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.tool.ServiceRegister;

/**
 * twc服务异步启动类，防止阻塞
 * 
 * @author lion
 *
 */
class TWCServerStartThread implements Runnable {
	private TWCServer twcServer;

	public TWCServerStartThread(TWCServer twcServer) {
		this.twcServer = twcServer;
	}

	@Override
	public void run() {
		EventLoopGroup acceptorGroup = null;
		EventLoopGroup ioGroup = null;

		try {
			this.twcServer.bizThreadPool = Executors.newFixedThreadPool(
					this.twcServer.twcServerConfig.getBizThreadPoolThreadTotal(),
					new CustomThreadFactory("twcServer", "biz"));

			Class<? extends ServerChannel> serverChannel = null;
			String logpre = "";

			if (Epoll.isAvailable()) {
				acceptorGroup = new EpollEventLoopGroup(this.twcServer.twcServerConfig.getAcceptorThreads(), new DefaultThreadFactory("TWCServer1", true));
				ioGroup = new EpollEventLoopGroup(this.twcServer.twcServerConfig.getIoThreads(), new DefaultThreadFactory("TWCServer2", true));
				serverChannel = EpollServerSocketChannel.class;
				logpre = "start-Epoll";
			} else {
				acceptorGroup = new NioEventLoopGroup(this.twcServer.twcServerConfig.getAcceptorThreads(), new DefaultThreadFactory("TWCServer1", true));
				ioGroup = new NioEventLoopGroup(this.twcServer.twcServerConfig.getIoThreads(), new DefaultThreadFactory("TWCServer2", true));
				serverChannel = NioServerSocketChannel.class;
				logpre = "start-Nio";
			}

			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(acceptorGroup, ioGroup)
					.channel(serverChannel)
					.childHandler(new TWCServerChannelInit(this.twcServer))
					.option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
			// ChannelOption.SO_KEEPALIV 心跳保活机制，由于需2个小时没数据传输才会生效，此处可去除

			ChannelFuture future = bootstrap.bind(this.twcServer.serverAddress.getIp(), this.twcServer.serverAddress.getPort()).sync();

			// twc服务注册到集群
			this.twcServerRegister();

			LogLIB.info(this.twcServer.getServerInfo() + ", TWCServer " + logpre + " listen");

			// 阻塞
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			LogLIB.error(this.twcServer.getServerInfo() + ", TWCServerStartThread exception", e);
		} finally {
			if (acceptorGroup != null) {
				acceptorGroup.shutdownGracefully();
			}
			if (ioGroup != null) {
				ioGroup.shutdownGracefully();
			}
		}
	}

	/**
	 * 服务注册（用于服务发现）
	 * 
	 * @throws Exception
	 */
	private void twcServerRegister() throws Exception {
		// 目前仅支持zk
		if (Tool.checkHaveValue(this.twcServer.zkServerUrl)) {
			ServiceRegister.zkHandler(
					Constant.TWC_SERVER_REGISTER_ZK_BASEPATH,
					this.twcServer.serviceName,
					this.twcServer.serverAddress,
					this.twcServer.zkServerUrl);
		}
	}
}
