package com.lion.utility.twc.entity.config;

import java.util.HashSet;
import java.util.Set;

import com.lion.utility.twc.constant.Constant;

/**
 * twc server配置
 * 
 * @author lion
 *
 */
public class TWCServerConfig {
	/**
	 * 是否debug模式
	 */
	private Boolean isDebug = false;

	/**
	 * ip限制白名单（为空则表示不验证）
	 */
	private Set<String> requestIPSet = new HashSet<>();

	/**
	 * 业务线程池中线程个数
	 */
	private Integer bizThreadPoolThreadTotal = Constant.TWC_BIZTHREAD_TOTAL_DEFAULT;

	/**
	 * twc读取超时秒数
	 */
	private Integer readTimeoutSecond = Constant.TWC_READTIMEOUT_SECOND_DEFAULT;

	/**
	 * 日志输出级别
	 */
	private Integer logLevel = Constant.LOGLEVEL_ERROR;
	/**
	 * 最大接收消息字节数
	 */
	private Integer messageRecieveMaxLength = Constant.TWC_MESSAGERECIEVE_MAXLENGTH_DEFAULT;

	/**
	 * 压缩策略
	 */
	private CompressPolicy compressPolicy = new CompressPolicy(false, Constant.TWC_COMPRESSMINLENGTH_DEFAULT);
	/**
	 * 加密策略
	 */
	private EncryptPolicy encryptPolicy = new EncryptPolicy(false, Constant.TWC_ENCRYPTKEY_DEFAULT);

	/**
	 * Acceptor线程数
	 */
	private Integer acceptorThreads = Constant.TWC_SERVER_ACCEPTORTHREADS_DEFAULT;
	/**
	 * io线程数
	 */
	private Integer ioThreads = Constant.TWC_SERVER_IOTHREADS_DEFAULT;

	/**
	 * 设置是否debug模式
	 * 
	 * @param isDebug
	 *            是否debug模式
	 */
	public void setIsDebug(Boolean isDebug) {
		this.isDebug = isDebug;
	}

	/**
	 * 获取是否debug模式
	 * 
	 * @return 是否debug模式
	 */
	public Boolean getIsDebug() {
		return isDebug;
	}

	/**
	 * 设置ip限制白名单（为空则表示不验证）
	 * 
	 * @param requestIPSet
	 *            ip限制白名单（为空则表示不验证）
	 */
	public void setRequestIPSet(Set<String> requestIPSet) {
		this.requestIPSet = requestIPSet;
	}

	/**
	 * 获取ip限制白名单（为空则表示不验证）
	 * 
	 * @return ip限制白名单（为空则表示不验证）
	 */
	public Set<String> getRequestIPSet() {
		return requestIPSet;
	}

	/**
	 * 设置业务线程池中线程个数
	 * 
	 * @param bizThreadPoolThreadTotal
	 *            业务线程池中线程个数
	 */
	public void setBizThreadPoolThreadTotal(Integer bizThreadPoolThreadTotal) {
		this.bizThreadPoolThreadTotal = bizThreadPoolThreadTotal;
	}

	/**
	 * 获取业务线程池中线程个数
	 * 
	 * @return 业务线程池中线程个数
	 */
	public Integer getBizThreadPoolThreadTotal() {
		return bizThreadPoolThreadTotal;
	}

	/**
	 * 设置twc读取超时秒数
	 * 
	 * @param readTimeoutSecond
	 *            twc读取超时秒数
	 */
	public void setReadTimeoutSecond(Integer readTimeoutSecond) {
		this.readTimeoutSecond = readTimeoutSecond;
	}

	/**
	 * 获取twc读取超时秒数
	 * 
	 * @return twc读取超时秒数
	 */
	public Integer getReadTimeoutSecond() {
		return readTimeoutSecond;
	}

	/**
	 * 设置日志输出级别
	 * 
	 * @param logLevel
	 *            日志输出级别
	 */
	public void setLogLevel(Integer logLevel) {
		this.logLevel = logLevel;
	}

	/**
	 * 获取日志输出级别
	 * 
	 * @return 日志输出级别
	 */
	public Integer getLogLevel() {
		return logLevel;
	}

	/**
	 * 设置最大接收消息字节数
	 * 
	 * @param messageRecieveMaxLength
	 *            最大接收消息字节数
	 */
	public void setMessageRecieveMaxLength(Integer messageRecieveMaxLength) {
		this.messageRecieveMaxLength = messageRecieveMaxLength;
	}

	/**
	 * 获取最大接收消息字节数
	 * 
	 * @return 最大接收消息字节数
	 */
	public Integer getMessageRecieveMaxLength() {
		return messageRecieveMaxLength;
	}

	/**
	 * 设置压缩策略
	 * 
	 * @param compressPolicy
	 *            压缩策略
	 */
	public void setCompressPolicy(CompressPolicy compressPolicy) {
		this.compressPolicy = compressPolicy;
	}

	/**
	 * 获取压缩策略
	 * 
	 * @return 压缩策略
	 */
	public CompressPolicy getCompressPolicy() {
		return compressPolicy;
	}

	/**
	 * 设置加密策略
	 * 
	 * @param encryptPolicy
	 *            加密策略
	 */
	public void setEncryptPolicy(EncryptPolicy encryptPolicy) {
		this.encryptPolicy = encryptPolicy;
	}

	/**
	 * 获取加密策略
	 * 
	 * @return 加密策略
	 */
	public EncryptPolicy getEncryptPolicy() {
		return encryptPolicy;
	}

	/**
	 * 设置Acceptor线程数
	 * 
	 * @param acceptorThreads
	 *            Acceptor线程数
	 */
	public void setAcceptorThreads(Integer acceptorThreads) {
		this.acceptorThreads = acceptorThreads;
	}

	/**
	 * 获取Acceptor线程数
	 * 
	 * @return Acceptor线程数
	 */
	public Integer getAcceptorThreads() {
		return acceptorThreads;
	}

	/**
	 * 设置io线程数
	 * 
	 * @param ioThreads
	 *            io线程数
	 */
	public void setIoThreads(Integer ioThreads) {
		this.ioThreads = ioThreads;
	}

	/**
	 * 获取io线程数
	 * 
	 * @return io线程数
	 */
	public Integer getIoThreads() {
		return ioThreads;
	}

}
