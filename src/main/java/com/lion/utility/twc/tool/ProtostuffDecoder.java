package com.lion.utility.twc.tool;

import java.util.List;

import org.xerial.snappy.Snappy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import com.lion.utility.tool.code.SEncryptLIB;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.config.EncryptPolicy;

/**
 * 解码器
 * 
 * @author lion
 *
 * @param <T>
 *            泛型
 */
public class ProtostuffDecoder<T> extends ByteToMessageDecoder {
	private Class<T> cls;
	private EncryptPolicy encryptPolicy;

	public ProtostuffDecoder(Class<T> cls, EncryptPolicy encryptPolicy) {
		this.cls = cls;
		this.encryptPolicy = encryptPolicy;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// 读取消息
		boolean isCompressTemp = in.readBoolean();
		byte[] data = new byte[in.readableBytes()];
		in.readBytes(data);

		// 解压
		if (isCompressTemp) {
			data = Snappy.uncompress(data);
		}

		// 解密 
		if (this.encryptPolicy.getIsEncrypt()) {
			data = SEncryptLIB.decrypt(SEncryptLIB.SENCRYPTTYPE_AES, Constant.SENCRYPT_MODE, this.encryptPolicy.getEncryptKey(), Constant.SENCRYPT_IV, data, Constant.ENCODING);
		}

		// 反序列化
		Schema<T> schema = RuntimeSchema.getSchema(this.cls);
		T t = schema.newMessage();
		ProtostuffIOUtil.mergeFrom(data, t, schema);

		out.add(t);
	}
}
