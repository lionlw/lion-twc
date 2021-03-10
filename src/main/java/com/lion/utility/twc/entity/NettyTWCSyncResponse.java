package com.lion.utility.twc.entity;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * netty处理结果封装类（用于同步）
 * 
 * @author lion
 */
public class NettyTWCSyncResponse {
	/**
	 * 类型
	 */
	private Integer type;
	/**
	 * 是否已经清除缓存中消息（提升性能）
	 */
	private Boolean haveClearMsg;

	/**
	 * 客户端标识（用于批量执行命令场景）
	 */
	private String clientId;
	/**
	 * 同步阀门（用于批量执行命令场景）
	 */
	private CountDownLatch latch;

	/**
	 * 锁（用于单个执行命令场景）
	 */
	private Lock lock;
	/**
	 * 锁条件（用于单个执行命令场景）
	 */
	private Condition lockCondition;
	/**
	 * 是否返回结果（用于单个执行命令场景）
	 */
	private Boolean isDone;

	/**
	 * twc请求类（用于批量执行命令场景）
	 */
	private TWCMessage twcRequest;
	/**
	 * twc响应类
	 */
	private TWCMessage twcResponse;

	/**
	 * 设置类型
	 * 
	 * @param type 类型
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * 获取类型
	 * 
	 * @return 类型
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * 设置是否已经清除缓存中消息（提升性能）
	 * 
	 * @param haveClearMsg 是否已经清除缓存中消息（提升性能）
	 */
	public void setHaveClearMsg(Boolean haveClearMsg) {
		this.haveClearMsg = haveClearMsg;
	}

	/**
	 * 获取是否已经清除缓存中消息（提升性能）
	 * 
	 * @return 是否已经清除缓存中消息（提升性能）
	 */
	public Boolean getHaveClearMsg() {
		return haveClearMsg;
	}

	/**
	 * 设置客户端标识（用于批量执行命令场景）
	 * 
	 * @param clientId 客户端标识（用于批量执行命令场景）
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * 获取客户端标识（用于批量执行命令场景）
	 * 
	 * @return 客户端标识（用于批量执行命令场景）
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * 设置同步阀门（用于批量执行命令场景）
	 * 
	 * @param latch 同步阀门（用于批量执行命令场景）
	 */
	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

	/**
	 * 获取同步阀门（用于批量执行命令场景）
	 * 
	 * @return 同步阀门（用于批量执行命令场景）
	 */
	public CountDownLatch getLatch() {
		return latch;
	}

	/**
	 * 设置锁（用于单个执行命令场景）
	 * 
	 * @param lock 锁（用于单个执行命令场景）
	 */
	public void setLock(Lock lock) {
		this.lock = lock;
	}

	/**
	 * 获取锁（用于单个执行命令场景）
	 * 
	 * @return 锁（用于单个执行命令场景）
	 */
	public Lock getLock() {
		return lock;
	}

	/**
	 * 设置锁条件（用于单个执行命令场景）
	 * 
	 * @param lockCondition 锁条件（用于单个执行命令场景）
	 */
	public void setLockCondition(Condition lockCondition) {
		this.lockCondition = lockCondition;
	}

	/**
	 * 获取锁条件（用于单个执行命令场景）
	 * 
	 * @return 锁条件（用于单个执行命令场景）
	 */
	public Condition getLockCondition() {
		return lockCondition;
	}

	/**
	 * 设置是否返回结果（用于单个执行命令场景）
	 * 
	 * @param isDone 是否返回结果（用于单个执行命令场景）
	 */
	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}

	/**
	 * 获取是否返回结果（用于单个执行命令场景）
	 * 
	 * @return 是否返回结果（用于单个执行命令场景）
	 */
	public Boolean getIsDone() {
		return isDone;
	}

	/**
	 * 设置twc请求类（用于批量执行命令场景）
	 * 
	 * @param twcRequest twc请求类（用于批量执行命令场景）
	 */
	public void setTwcRequest(TWCMessage twcRequest) {
		this.twcRequest = twcRequest;
	}

	/**
	 * 获取twc请求类（用于批量执行命令场景）
	 * 
	 * @return twc请求类（用于批量执行命令场景）
	 */
	public TWCMessage getTwcRequest() {
		return twcRequest;
	}

	/**
	 * 设置twc响应类
	 * 
	 * @param twcResponse twc响应类
	 */
	public void setTwcResponse(TWCMessage twcResponse) {
		this.twcResponse = twcResponse;
	}

	/**
	 * 获取twc响应类
	 * 
	 * @return twc响应类
	 */
	public TWCMessage getTwcResponse() {
		return twcResponse;
	}

}
