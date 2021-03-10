package com.lion.utility.twc.client;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.tool.ProtostuffDecoder;
import com.lion.utility.twc.tool.ProtostuffEncoder;

/**
 * channel初始化
 * 
 * @author lion
 *
 */
class TWCClientChannelInit extends ChannelInitializer<SocketChannel> {
	private TWCClient twcClient;

	public TWCClientChannelInit(TWCClient twcClient) {
		this.twcClient = twcClient;
	}

	@Override
	protected void initChannel(SocketChannel s) throws Exception {
		ChannelPipeline pipeline = s.pipeline();
		pipeline.addLast(new IdleStateHandler(this.twcClient.twcClientConfig.getIdleTimeSeconds() * 2L,
				this.twcClient.twcClientConfig.getIdleTimeSeconds(),
				0,
				TimeUnit.SECONDS));
		// out
		pipeline.addLast(new ProtostuffEncoder<TWCMessage>(TWCMessage.class, this.twcClient.twcClientConfig.getCompressPolicy(), this.twcClient.twcClientConfig.getEncryptPolicy()));

		// in
		// 此处参数设置需注意，当协议调整时需参考下LengthFieldBasedFrameDecoder的参数说明
		// https://www.cnblogs.com/crazymakercircle/p/10294745.html
		pipeline.addLast(new LengthFieldBasedFrameDecoder(this.twcClient.twcClientConfig.getMessageRecieveMaxLength(), 0, 4, 1, 4));
		pipeline.addLast(new ProtostuffDecoder<TWCMessage>(TWCMessage.class, this.twcClient.twcClientConfig.getEncryptPolicy()));
		pipeline.addLast(new TWCClientHandler(this.twcClient));
	}
}
