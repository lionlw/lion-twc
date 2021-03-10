package com.lion.utility.twc.management;

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
class TWCManagementChannelInit extends ChannelInitializer<SocketChannel> {
	private TWCManagement twcManagement;

	public TWCManagementChannelInit(TWCManagement twcManagement) {
		this.twcManagement = twcManagement;
	}

	@Override
	protected void initChannel(SocketChannel s) throws Exception {
		ChannelPipeline pipeline = s.pipeline();
		pipeline.addLast(new IdleStateHandler(this.twcManagement.twcManagementConfig.getIdleTimeSeconds() * 2L,
				this.twcManagement.twcManagementConfig.getIdleTimeSeconds(),
				this.twcManagement.twcManagementConfig.getIdleTimeSeconds() * 4L,
				TimeUnit.SECONDS));
		// out
		pipeline.addLast(new ProtostuffEncoder<TWCMessage>(TWCMessage.class, this.twcManagement.twcManagementConfig.getCompressPolicy(), this.twcManagement.twcManagementConfig.getEncryptPolicy()));

		// in
		// 此处参数设置需注意，当协议调整时需参考下LengthFieldBasedFrameDecoder的参数说明
		// https://www.cnblogs.com/crazymakercircle/p/10294745.html
		pipeline.addLast(new LengthFieldBasedFrameDecoder(this.twcManagement.twcManagementConfig.getMessageRecieveMaxLength(), 0, 4, 1, 4));
		pipeline.addLast(new ProtostuffDecoder<TWCMessage>(TWCMessage.class, this.twcManagement.twcManagementConfig.getEncryptPolicy()));
		pipeline.addLast(new TWCManagementHandler(this.twcManagement));
	}
}
