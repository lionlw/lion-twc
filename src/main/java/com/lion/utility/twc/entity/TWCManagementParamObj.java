package com.lion.utility.twc.entity;

import java.util.List;

/**
 * 管理端参数实体
 * 
 * @author lion
 *
 */
public class TWCManagementParamObj {
	/**
	 * 请求类型
	 */
	private Integer type;
	/**
	 * 客户端id列表
	 */
	private List<String> clientIds;
	/**
	 * 请求参数对象
	 */
	private Object paramObj;

	/**
	 * 设置请求类型
	 * 
	 * @param type
	 *            请求类型
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * 获取请求类型
	 * 
	 * @return 请求类型
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * 设置客户端id列表
	 * 
	 * @param clientIds
	 *            客户端id列表
	 */
	public void setClientIds(List<String> clientIds) {
		this.clientIds = clientIds;
	}

	/**
	 * 获取客户端id列表
	 * 
	 * @return 客户端id列表
	 */
	public List<String> getClientIds() {
		return clientIds;
	}

	/**
	 * 设置请求参数对象
	 * 
	 * @param paramObj
	 *            请求参数对象
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

}
