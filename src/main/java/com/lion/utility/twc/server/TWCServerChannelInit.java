package com.lion.utility.twc.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.tool.ProtostuffDecoder;
import com.lion.utility.twc.tool.ProtostuffEncoder;

/**
 * channel初始化
 * 
 * @author lion
 *
 */
class TWCServerChannelInit extends ChannelInitializer<SocketChannel> {
	private TWCServer twcServer;

	public TWCServerChannelInit(TWCServer twcServer) {
		this.twcServer = twcServer;
	}

	@Override
	protected void initChannel(SocketChannel s) throws Exception {
		ChannelPipeline pipeline = s.pipeline();

		// out
		pipeline.addLast(new ProtostuffEncoder<TWCMessage>(TWCMessage.class, this.twcServer.twcServerConfig.getCompressPolicy(), this.twcServer.twcServerConfig.getEncryptPolicy()));

		// in
		pipeline.addLast(new IPFilterFirewall(this.twcServer));
		// 此处参数设置需注意，当协议调整时需参考下LengthFieldBasedFrameDecoder的参数说明
		// https://www.cnblogs.com/crazymakercircle/p/10294745.html
		pipeline.addLast(new LengthFieldBasedFrameDecoder(this.twcServer.twcServerConfig.getMessageRecieveMaxLength(), 0, 4, 1, 4));
		pipeline.addLast(new ProtostuffDecoder<TWCMessage>(TWCMessage.class, this.twcServer.twcServerConfig.getEncryptPolicy()));
		pipeline.addLast(new TWCServerHandler(this.twcServer));
	}
}