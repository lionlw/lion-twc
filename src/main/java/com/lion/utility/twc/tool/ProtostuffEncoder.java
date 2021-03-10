package com.lion.utility.twc.tool;

import org.xerial.snappy.Snappy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import com.lion.utility.tool.code.SEncryptLIB;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.config.CompressPolicy;
import com.lion.utility.twc.entity.config.EncryptPolicy;

/**
 * 编码器
 * 
 * @author lion
 *
 * @param <T>
 *            泛型
 */
public class ProtostuffEncoder<T> extends MessageToByteEncoder<Object> {
	private Class<T> cls;
	private CompressPolicy compressPolicy;
	private EncryptPolicy encryptPolicy;

	public ProtostuffEncoder(Class<T> cls, CompressPolicy compressPolicy, EncryptPolicy encryptPolicy) {
		this.cls = cls;
		this.compressPolicy = compressPolicy;
		this.encryptPolicy = encryptPolicy;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
		if (cls.isInstance(in)) {
			// 序列化
			Schema<T> schema = RuntimeSchema.getSchema(this.cls);
			byte[] data;
			LinkedBuffer buffer = LinkedBuffer.allocate(512);
			try {
				data = ProtostuffIOUtil.toByteArray((T) in, schema, buffer);
			} finally {
				buffer.clear();
			}

			// 加密
			if (this.encryptPolicy.getIsEncrypt()) {
				data = SEncryptLIB.encrypt(SEncryptLIB.SENCRYPTTYPE_AES, Constant.SENCRYPT_MODE, this.encryptPolicy.getEncryptKey(), Constant.SENCRYPT_IV, data, Constant.ENCODING);
			}

			// 是否压缩标识，仅在开启压缩，且传输内容>=指定字节数，才压缩
			boolean isCompressTemp = false;
			if (this.compressPolicy.getIsCompress() && data.length >= this.compressPolicy.getCompressMinLength()) {
				data = Snappy.compress(data);
				isCompressTemp = true;
			}

			// 将消息长度作为int（4个字节）写入消息头部
			out.writeInt(data.length);
			// 将压缩与否写入
			out.writeBoolean(isCompressTemp);
			// 写入消息
			out.writeBytes(data);
		}
	}
}
