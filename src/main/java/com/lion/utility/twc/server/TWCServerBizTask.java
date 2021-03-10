package com.lion.utility.twc.server;

import java.util.Map;

import io.netty.channel.ChannelHandlerContext;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.framework.web.i.ILIB;
import com.lion.utility.framework.web.i.constant.IConstant;
import com.lion.utility.framework.web.i.entity.IResult;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.NettyTWCSyncResponse;
import com.lion.utility.twc.entity.TWCManagementParamObj;
import com.lion.utility.twc.entity.TWCMessage;
import com.lion.utility.twc.tool.CommonLIB;

/**
 * 业务逻辑处理线程（处理从client发起的接口调用）
 * 
 * @author lion
 *
 */
class TWCServerBizTask implements Runnable {
	private ChannelHandlerContext ctx;
	private TWCMessage twcMessage;
	private TWCServer twcServer;

	public TWCServerBizTask(ChannelHandlerContext ctx, TWCMessage twcMessage, TWCServer twcServer) {
		this.ctx = ctx;
		this.twcMessage = twcMessage;
		this.twcServer = twcServer;
	}

	@Override
	public void run() {
		if (twcMessage.getMsgType().equals(Constant.MESSAGE_TYPE_REQUEST)) {
			this.handlerTWCClientRequest(twcMessage);
		} else if (twcMessage.getMsgType().equals(Constant.MESSAGE_TYPE_MANAGEMENTREQUEST)) {
			this.handlerTWCManagementRequest(twcMessage);
		} else {
			this.handlerTWCClientResponse(twcMessage);
		}
	}

	/**
	 * 处理client的接口请求
	 * 
	 * @param twcResponse 响应
	 * @throws Exception 异常
	 */
	private void handlerTWCClientRequest(TWCMessage twcRequest) {
		TWCMessage twcResponse = new TWCMessage();
		twcResponse.setMsgType(Constant.MESSAGE_TYPE_RESPONSE);
		twcResponse.setMsgId(twcRequest.getMsgId());

		try {
			if (twcRequest.getMethodId().equals(Constant.METHODID_HEARTBEAT)) {
				// 心跳方法执行
				twcResponse.setiResult(ILIB.getIResultSucceed());
			} else if (twcRequest.getMethodId().equals(Constant.METHODID_REGISTER)) {
				// 客户端注册方法执行
				String ipport = CommonLIB.getNettyRequestIp(this.ctx);
				String clientId = (String) twcRequest.getParamObj();

				this.twcServer.clientIPPortClientIdMap.put(ipport, clientId);
				this.twcServer.clientIdCtxMap.put(clientId, this.ctx);
				LogLIB.twc(this.twcServer.getServerInfo() + " client register succeed, clientId:" + clientId + ", ipport:" + ipport);

				twcResponse.setiResult(ILIB.getIResultSucceed());
			} else {
				// twc方法执行
				IResult<Object> iResult = this.twcServer.iHandlerMessage.handler(twcRequest.getMethodId(), twcRequest.getParamObj());
				if (iResult == null) {
					iResult = ILIB.getIResultFailed(IConstant.RETURN_CODE_PARM_ERROR, "invalid method");
				}
				twcResponse.setiResult(iResult);
			}

			// 返回
			this.ctx.writeAndFlush(twcResponse);
		} catch (Exception e) {
			LogLIB.error(this.twcServer.getServerInfo() + ", TWCRequest exception, twcRequest:" + twcRequest.toString() + ", twcResponse:" + twcResponse.toString(), e);

			IResult<Object> iResult = new IResult<>();
			iResult.setCode(IConstant.RETURN_CODE_SYETEM_ERROR);
			iResult.setMsg(e.getMessage());

			twcResponse.setiResult(iResult);

			this.ctx.writeAndFlush(twcResponse);
		} finally {
			if (this.twcServer.twcServerConfig.getLogLevel() == Constant.LOGLEVEL_INOUTERROR) {
				LogLIB.twc(this.twcServer.getServerInfo() + " TWCRequest, requestIp:" + CommonLIB.getNettyRequestIp(ctx) + ", twcRequest:" + twcRequest.toString() + ", twcResponse:" + twcResponse.toString());
			} else if (this.twcServer.twcServerConfig.getLogLevel() == Constant.LOGLEVEL_INERROR) {
				LogLIB.twc(this.twcServer.getServerInfo() + " TWCRequest, requestIp:" + CommonLIB.getNettyRequestIp(ctx) + ", twcRequest:" + twcRequest.toString());
			}
		}
	}

	/**
	 * 处理management的接口请求
	 * 
	 * @param twcResponse 响应
	 * @throws Exception 异常
	 */
	private void handlerTWCManagementRequest(TWCMessage twcRequest) {
		TWCMessage twcResponse = new TWCMessage();
		twcResponse.setMsgType(Constant.MESSAGE_TYPE_RESPONSE);
		twcResponse.setMsgId(twcRequest.getMsgId());

		try {
			if (twcRequest.getMethodId().equals(Constant.METHODID_HEARTBEAT)) {
				// 心跳方法执行
				twcResponse.setiResult(ILIB.getIResultSucceed());
			} else if (twcRequest.getMethodId().equals(Constant.METHODID_REGISTER)) {
				// 管理端注册方法执行
				String ipport = CommonLIB.getNettyRequestIp(this.ctx);
				String managementId = (String) twcRequest.getParamObj();

				LogLIB.twc(this.twcServer.getServerInfo() + " management register succeed, managementId:" + managementId + ", ipport:" + ipport);

				twcResponse.setiResult(ILIB.getIResultSucceed());
			} else if (twcRequest.getMethodId().equals(Constant.METHODID_GETCLIENTIDS)) {
				// 获取客户端标识列表标识方法执行
				twcResponse.setiResult(ILIB.getIResultSucceed(this.twcServer.getClientIds()));
			} else {
				// twc management方法执行，即将management的指令，发送给client
				TWCManagementParamObj twcManagementParamObj = (TWCManagementParamObj) twcRequest.getParamObj();
				if (twcManagementParamObj.getType().equals(Constant.MANAGEREQUEST_TYPE_ALLCLIENTID)) {
					Map<String, IResult<Object>> result = this.twcServer.handlerAllClient(twcRequest.getMethodId(), twcManagementParamObj.getParamObj(), twcRequest.getReadTimeoutSecond());
					twcResponse.setiResult(ILIB.getIResultSucceed(result));
				} else if (twcManagementParamObj.getType().equals(Constant.MANAGEREQUEST_TYPE_CLIENTIDS)) {
					Map<String, IResult<Object>> result = this.twcServer.handlerClients(twcManagementParamObj.getClientIds(), twcRequest.getMethodId(), twcManagementParamObj.getParamObj(), twcRequest.getReadTimeoutSecond());
					twcResponse.setiResult(ILIB.getIResultSucceed(result));
				} else {
					twcResponse.setiResult(ILIB.getIResultFailed(IConstant.RETURN_CODE_PARM_ERROR, "invalid twcManagementParamObj type " + twcManagementParamObj.getType()));
				}
			}

			// 返回
			this.ctx.writeAndFlush(twcResponse);
		} catch (Exception e) {
			LogLIB.error(this.twcServer.getServerInfo() + ", TWCRequest exception, twcRequest:" + twcRequest.toString() + ", twcResponse:" + twcResponse.toString(), e);

			IResult<Object> iResult = new IResult<>();
			iResult.setCode(IConstant.RETURN_CODE_SYETEM_ERROR);
			iResult.setMsg(e.getMessage());

			twcResponse.setiResult(iResult);

			this.ctx.writeAndFlush(twcResponse);
		} finally {
			if (this.twcServer.twcServerConfig.getLogLevel() == Constant.LOGLEVEL_INOUTERROR) {
				LogLIB.twc(this.twcServer.getServerInfo() + " TWCRequest, requestIp:" + CommonLIB.getNettyRequestIp(ctx) + ", twcRequest:" + twcRequest.toString() + ", twcResponse:" + twcResponse.toString());
			} else if (this.twcServer.twcServerConfig.getLogLevel() == Constant.LOGLEVEL_INERROR) {
				LogLIB.twc(this.twcServer.getServerInfo() + " TWCRequest, requestIp:" + CommonLIB.getNettyRequestIp(ctx) + ", twcRequest:" + twcRequest.toString());
			}
		}
	}

	/**
	 * 处理client的响应（server对client发起请求时，client会响应）
	 * 
	 * @param twcResponse 响应
	 */
	private void handlerTWCClientResponse(TWCMessage twcResponse) {
		try {
			Object obj = this.twcServer.methodSyncCache.getIfPresent(twcResponse.getMsgId());
			if (obj != null) {
				if ((obj instanceof NettyTWCSyncResponse)) {
					// 设置指定任务阻塞结束，并写入任务执行结果
					NettyTWCSyncResponse nettyTWCSyncResponse = (NettyTWCSyncResponse) obj;
					nettyTWCSyncResponse.setTwcResponse(twcResponse);

					if (nettyTWCSyncResponse.getType() == Constant.RESPONSE_TYPE_BATCH) {
						// 阀门设置-1
						nettyTWCSyncResponse.getLatch().countDown();

						// 消息接收成功，清理cache，降低内存消耗
						CommonLIB.clearMethodSyncCache(this.twcServer.methodSyncCache, nettyTWCSyncResponse, twcResponse.getMsgId());
					}
				} else {
					LogLIB.error(this.getLogPre(ctx) + ", obj isn't NettyTWCSyncResponse, " + obj.toString() + ", twcResponse:" + twcResponse.toString());
				}
			} else {
				LogLIB.error(this.getLogPre(ctx) + ", msg not found, obj is null, twcResponse:" + twcResponse.toString());
			}
		} catch (Exception e) {
			LogLIB.error(this.twcServer.getServerInfo() + ", handlerTWCClientResponse exception, " + twcResponse.toString(), e);
		}
	}

	/**
	 * 获取日志前缀
	 * 
	 * @param ctx
	 * @return
	 */
	private String getLogPre(ChannelHandlerContext ctx) {
		return this.twcServer.serviceName + "-" + CommonLIB.getNettyRequestIp(ctx);
	}
}
