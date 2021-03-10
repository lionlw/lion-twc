package com.lion.utility.twc.client;

import io.netty.channel.ChannelHandlerContext;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.framework.web.i.ILIB;
import com.lion.utility.framework.web.i.constant.IConstant;
import com.lion.utility.framework.web.i.entity.IResult;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.NettyTWCSyncResponse;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.tool.CommonLIB;

/**
 * 业务逻辑处理线程
 * 
 * @author lion
 *
 */
class TWCClientBizTask implements Runnable {
	private ChannelHandlerContext ctx;
	private TWCMessage twcMessage;
	private TWCConnect twcConnect;

	public TWCClientBizTask(ChannelHandlerContext ctx, TWCMessage twcMessage, TWCConnect twcConnect) {
		this.ctx = ctx;
		this.twcMessage = twcMessage;
		this.twcConnect = twcConnect;
	}

	@Override
	public void run() {
		if (this.twcMessage.getMsgType() == Constant.MESSAGE_TYPE_REQUEST) {
			this.handlerTWCServerRequest(this.ctx, this.twcMessage);
		} else {
			this.handlerTWCServerResponse(this.ctx, this.twcMessage);
		}
	}

	/**
	 * 处理server的接口请求
	 * 
	 * @param ctx         通道
	 * @param twcResponse 响应
	 * @throws Exception 异常
	 */
	private void handlerTWCServerRequest(ChannelHandlerContext ctx, TWCMessage twcRequest) {
		TWCMessage twcResponse = new TWCMessage();
		twcResponse.setMsgType(Constant.MESSAGE_TYPE_RESPONSE);
		twcResponse.setMsgId(twcRequest.getMsgId());

		try {
			// twc方法执行
			IResult<Object> iResult = this.twcConnect.twcClient.iHandlerMessage.handler(twcRequest.getMethodId(), twcRequest.getParamObj());
			if (iResult == null) {
				iResult = ILIB.getIResultFailed(IConstant.RETURN_CODE_PARM_ERROR, "invalid method");
			}
			twcResponse.setiResult(iResult);

			// 返回
			this.ctx.writeAndFlush(twcResponse);
		} catch (Exception e) {
			LogLIB.error(this.twcConnect.getServerInfo() + ", TWCRequest exception, twcRequest:" + twcRequest.toString() + ", twcResponse:" + twcResponse.toString(), e);

			IResult<Object> iResult = new IResult<>();
			iResult.setCode(IConstant.RETURN_CODE_SYETEM_ERROR);
			iResult.setMsg(e.getMessage());

			twcResponse.setiResult(iResult);

			this.ctx.writeAndFlush(twcResponse);
		} finally {
			if (this.twcConnect.twcClient.twcClientConfig.getLogLevel() == Constant.LOGLEVEL_INOUTERROR) {
				LogLIB.twc(this.twcConnect.getServerInfo() + " TWCRequest, requestIp:" + CommonLIB.getNettyRequestIp(ctx) + ", twcRequest:" + twcRequest.toString() + ", twcResponse:" + twcResponse.toString());
			} else if (this.twcConnect.twcClient.twcClientConfig.getLogLevel() == Constant.LOGLEVEL_INERROR) {
				LogLIB.twc(this.twcConnect.getServerInfo() + " TWCRequest, requestIp:" + CommonLIB.getNettyRequestIp(ctx) + ", twcRequest:" + twcRequest.toString());
			}
		}
	}

	/**
	 * 处理server的响应
	 * 
	 * @param ctx         通道
	 * @param twcResponse 响应
	 */
	private void handlerTWCServerResponse(ChannelHandlerContext ctx, TWCMessage twcResponse) {
		try {
			Object obj = this.twcConnect.twcClient.methodSyncCache.getIfPresent(twcResponse.getMsgId());
			if (obj != null) {
				if ((obj instanceof NettyTWCSyncResponse)) {
					// 设置指定任务阻塞结束，并写入任务执行结果
					NettyTWCSyncResponse nettyTWCSyncResponse = (NettyTWCSyncResponse) obj;
					nettyTWCSyncResponse.setTwcResponse(twcResponse);

					if (nettyTWCSyncResponse.getType() == Constant.RESPONSE_TYPE_SINGLE) {
						try {
							// 尝试获取锁
							nettyTWCSyncResponse.getLock().lock();
							// 设置twc完成
							nettyTWCSyncResponse.setIsDone(true);
							// 唤醒等待线程
							nettyTWCSyncResponse.getLockCondition().signal();
						} finally {
							// 结束则释放锁
							nettyTWCSyncResponse.getLock().unlock();

							// 消息接收成功，清理cache，降低内存消耗
							CommonLIB.clearMethodSyncCache(this.twcConnect.twcClient.methodSyncCache, nettyTWCSyncResponse, twcResponse.getMsgId());
						}
					}
				} else if (obj instanceof String) {
					// twc内部工具方法
					String methodId = (String) obj;
					if (methodId.equals(Constant.METHODID_HEARTBEAT)) {
						// 心跳无需做业务处理
						if (this.twcConnect.twcClient.twcClientConfig.getIsDebug()) {
							LogLIB.twc(this.twcConnect.getServerInfo() + ", heartbeat recieve, twcResponse:" + twcResponse.toString());
						}

						// 消息接收成功，清理cache，降低内存消耗
						this.twcConnect.twcClient.methodSyncCache.invalidate(twcResponse.getMsgId());
					} else if (methodId.equals(Constant.METHODID_REGISTER)) {
						// 客户端注册无需做业务处理
						LogLIB.info(this.twcConnect.getServerInfo() + ", client register complete, twcResponse:" + twcResponse.toString());

						// 消息接收成功，清理cache，降低内存消耗
						this.twcConnect.twcClient.methodSyncCache.invalidate(twcResponse.getMsgId());
					} else {
						LogLIB.error(this.twcConnect.getServerInfo() + ", invalid methodId, " + obj.toString() + ", twcResponse:" + twcResponse.toString());
					}
				} else {
					LogLIB.error(this.twcConnect.getServerInfo() + ", obj isn't NettyTWCSyncResponse, " + obj.toString() + ", twcResponse:" + twcResponse.toString());
				}
			} else {
				LogLIB.error(this.twcConnect.getServerInfo() + ", msg not found, obj is null, twcResponse:" + twcResponse.toString());
			}
		} catch (Exception e) {
			LogLIB.error(this.twcConnect.getServerInfo() + ", handlerTWCResponse exception, " + twcResponse.toString(), e);
		}
	}
}
