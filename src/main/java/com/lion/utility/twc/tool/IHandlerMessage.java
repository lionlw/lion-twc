package com.lion.utility.twc.tool;

import com.lion.utility.framework.web.i.entity.IResult;

/**
 * 处理消息接口
 * 
 * @author lion
 *
 */
@FunctionalInterface
public interface IHandlerMessage {
	/**
	 * 处理消息
	 * 
	 * @param methodId
	 *            方法标识
	 * @param paramObj
	 *            请求参数对象
	 * @return 结果
	 */
	IResult<Object> handler(String methodId, Object paramObj);
}
