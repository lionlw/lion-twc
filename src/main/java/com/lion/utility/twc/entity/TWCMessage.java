package com.lion.utility.twc.entity;

import com.lion.utility.tool.file.JsonLIB;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.framework.web.i.entity.IResult;

/**
 * twc消息类
 * 
 * @author lion
 *
 */
public class TWCMessage {
	/**
	 * 唯一消息标识
	 */
	private Integer msgId;
	/**
	 * 消息类型
	 */
	private Integer msgType;
	/**
	 * 方法标识
	 */
	private String methodId;
	/**
	 * 请求参数对象
	 */
	private Object paramObj;
	/**
	 * 方法返回结果
	 */
	private IResult<Object> iResult;
	/**
	 * 读取超时秒数
	 */
	private Integer readTimeoutSecond;

	/**
	 * 设置唯一消息标识
	 * 
	 * @param msgId 唯一消息标识
	 */
	public void setMsgId(Integer msgId) {
		this.msgId = msgId;
	}

	/**
	 * 获取唯一消息标识
	 * 
	 * @return 唯一消息标识
	 */
	public Integer getMsgId() {
		return msgId;
	}

	/**
	 * 设置消息类型
	 * 
	 * @param msgType 消息类型
	 */
	public void setMsgType(Integer msgType) {
		this.msgType = msgType;
	}

	/**
	 * 获取消息类型
	 * 
	 * @return 消息类型
	 */
	public Integer getMsgType() {
		return msgType;
	}

	/**
	 * 设置方法标识
	 * 
	 * @param methodId 方法标识
	 */
	public void setMethodId(String methodId) {
		this.methodId = methodId;
	}

	/**
	 * 获取方法标识
	 * 
	 * @return 方法标识
	 */
	public String getMethodId() {
		return methodId;
	}

	/**
	 * 设置请求参数对象
	 * 
	 * @param paramObj 请求参数对象
	 */
	public void setParamObj(Object paramObj) {
		this.paramObj = paramObj;
	}

	/**
	 * 获取请求参数对象
	 * 
	 * @return 请求参数对象
	 */
	public Object getParamObj() {
		return paramObj;
	}

	/**
	 * 设置方法返回结果
	 * 
	 * @param iResult 方法返回结果
	 */
	public void setiResult(IResult<Object> iResult) {
		this.iResult = iResult;
	}

	/**
	 * 获取方法返回结果
	 * 
	 * @return 方法返回结果
	 */
	public IResult<Object> getiResult() {
		return iResult;
	}

	/**
	 * 设置读取超时秒数
	 * 
	 * @param readTimeoutSecond 读取超时秒数
	 */
	public void setReadTimeoutSecond(Integer readTimeoutSecond) {
		this.readTimeoutSecond = readTimeoutSecond;
	}

	/**
	 * 获取读取超时秒数
	 * 
	 * @return 读取超时秒数
	 */
	public Integer getReadTimeoutSecond() {
		return readTimeoutSecond;
	}

	@Override
	public String toString() {
		try {
			return JsonLIB.toJson(this);
		} catch (Exception e) {
			LogLIB.error("", e);
		}

		return "";
	}

}
