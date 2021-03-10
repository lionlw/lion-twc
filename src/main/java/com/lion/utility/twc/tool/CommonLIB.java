package com.lion.utility.twc.tool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.benmanes.caffeine.cache.Cache;

import io.netty.channel.ChannelHandlerContext;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.framework.web.i.constant.IConstant;
import com.lion.utility.framework.web.i.entity.IResult;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.NettyTWCSyncResponse;
import com.lion.utility.twc.entity.TWCMessage;

/**
 * 工具类
 * 
 * @author lion
 *
 */
public class CommonLIB {
	private CommonLIB() {
	}

	/**
	 * 获取客户端ip
	 * 
	 * @param ctx 上下文
	 * @return 结果
	 */
	public static String getNettyRequestIp(ChannelHandlerContext ctx) {
		try {
			// 返回的格式：/114.80.117.180:48534

			return ctx.channel().remoteAddress().toString().replace("/", "");
		} catch (Exception e) {
			LogLIB.error("", e);
		}

		return "";
	}

	/**
	 * 处理用户输入的key
	 * 
	 * @param key 加密秘钥
	 * @return 值
	 */
	public static String getEncryptKey(String key) {
		// 系统自动处理为16位.多余删除，不足补随机字符串,多余则删除

		int max = 16;

		if (key.length() > max) {
			return key.substring(0, max);
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < max - key.length(); i++) {
				sb.append("0");
			}

			return key + sb.toString();
		}
	}

	/**
	 * 同步等待twc响应
	 * 
	 * @param nettyTWCSyncResponse netty响应
	 * @param readTimeoutSecond    读取超时
	 * @param serverInfo           服务信息
	 * @param twcRequestLog        twc请求日志
	 * @return 结果
	 * @throws Exception 异常
	 */
	public static TWCMessage getSyncTWCResponse(
			NettyTWCSyncResponse nettyTWCSyncResponse, int readTimeoutSecond,
			String serverInfo, String twcRequestLog) throws Exception {
		TWCMessage twcResponse = new TWCMessage();
		twcResponse.setMsgType(Constant.MESSAGE_TYPE_RESPONSE);
		twcResponse.setiResult(new IResult<>());
		twcResponse.getiResult().setCode(IConstant.RETURN_CODE_TWCSYSTEM_ERROR);

		// 阻塞等待返回结果
		boolean twcResult = false;
		try {
			// 加锁
			nettyTWCSyncResponse.getLock().lock();

			// 判断是否已经返回结果（防止返回速度高于上述代码执行速度的场景）
			if (nettyTWCSyncResponse.getIsDone()) {
				// 此时无需阻塞，直接返回成功
				twcResult = true;
			} else {
				// 没有返回，则阻塞（读取超时）
				// 阻塞当前线程，故若调用方为类似springmvc，由于原生的请求就是采用并发多线程，因此不会造成并发用户的阻塞
				twcResult = nettyTWCSyncResponse.getLockCondition().await(readTimeoutSecond, TimeUnit.SECONDS);
			}
		} finally {
			// 结束则释放锁
			nettyTWCSyncResponse.getLock().unlock();
		}

		if (twcResult) {
			twcResponse = nettyTWCSyncResponse.getTwcResponse();

			if (!twcResponse.getiResult().getCode().equals(IConstant.RETURN_CODE_SUCCEED)) {
				LogLIB.error(serverInfo + ", " + twcResponse.getiResult().toResultString() + ", twcRequest:" + twcRequestLog + ", twcResponse:" + twcResponse.toString());
			}

			// twc调用成功
		} else {
			twcResponse.getiResult().setMsg("readTimeout");
			LogLIB.error(serverInfo + ", " + twcResponse.getiResult().toResultString() + ", readTimeoutSecond > " + readTimeoutSecond + "s, twcRequest:" + twcRequestLog);
		}

		return twcResponse;
	}

	/**
	 * 清除方法缓存
	 * 
	 * @param methodSyncCache      缓存
	 * @param nettyTWCSyncResponse netty twc对象类
	 * @param msgId                消息标识
	 */
	public static void clearMethodSyncCache(Cache<Integer, Object> methodSyncCache, NettyTWCSyncResponse nettyTWCSyncResponse, int msgId) {
		// 及时清理cache，降低内存消耗
		if (nettyTWCSyncResponse != null && !nettyTWCSyncResponse.getHaveClearMsg()) {
			methodSyncCache.invalidate(msgId);
			nettyTWCSyncResponse.setHaveClearMsg(true);
		}
	}

	/**
	 * 获取消息标识（getAndIncrement()增长到MAX_VALUE时，再增长会变为MIN_VALUE，负数也可以作为标识）
	 * 确保每个client标识唯一即可
	 * 
	 * @param msgId 消息变量
	 * @return 结果
	 */
	public static int getMsgId(AtomicInteger msgId) {
		return msgId.getAndIncrement();
	}
}
