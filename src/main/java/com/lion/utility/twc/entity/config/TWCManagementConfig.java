package com.lion.utility.twc.entity.config;

import com.lion.utility.twc.constant.Constant;

/**
 * twc management配置
 * 
 * @author lion
 *
 */
public class TWCManagementConfig {
	/**
	 * 业务线程池中线程个数
	 */
	private Integer bizThreadPoolThreadTotal = Constant.TWC_BIZTHREAD_TOTAL_DEFAULT;
	/**
	 * twc链接超时秒数
	 */
	private Integer connectTimeoutSecond = Constant.TWC_CONNECTTIMEOUT_SECOND_DEFAULT;
	/**
	 * twc读取超时秒数
	 */
	private Integer readTimeoutSecond = Constant.TWC_READTIMEOUT_SECOND_DEFAULT;

	/**
	 * 是否debug模式
	 */
	private Boolean isDebug = false;
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
	 * io线程数
	 */
	private Integer ioThreads = Constant.TWC_CLIENT_IOTHREADS_DEFAULT;
	/**
	 * 连接空闲时间（单位：秒）
	 */
	private Integer idleTimeSeconds = 60;

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
	 * 设置twc链接超时秒数
	 * 
	 * @param connectTimeoutSecond
	 *            twc链接超时秒数
	 */
	public void setConnectTimeoutSecond(Integer connectTimeoutSecond) {
		this.connectTimeoutSecond = connectTimeoutSecond;
	}

	/**
	 * 获取twc链接超时秒数
	 * 
	 * @return twc链接超时秒数
	 */
	public Integer getConnectTimeoutSecond() {
		return connectTimeoutSecond;
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

	/**
	 * 设置连接空闲时间（单位：秒）
	 * 
	 * @param idleTimeSeconds
	 *            连接空闲时间（单位：秒）
	 */
	public void setIdleTimeSeconds(Integer idleTimeSeconds) {
		this.idleTimeSeconds = idleTimeSeconds;
	}

	/**
	 * 获取连接空闲时间（单位：秒）
	 * 
	 * @return 连接空闲时间（单位：秒）
	 */
	public Integer getIdleTimeSeconds() {
		return idleTimeSeconds;
	}

}
