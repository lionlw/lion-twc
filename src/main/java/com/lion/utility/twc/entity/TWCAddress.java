package com.lion.utility.twc.entity;

/**
 * 地址
 * 
 * @author lion
 *
 */
public class TWCAddress {
	/**
	 * 服务ip
	 */
	private String ip;
	/**
	 * 服务端口
	 */
	private Integer port;

	/**
	 * 设置服务ip
	 * 
	 * @param ip
	 *            服务ip
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * 获取服务ip
	 * 
	 * @return 服务ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * 设置服务端口
	 * 
	 * @param port
	 *            服务端口
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * 获取服务端口
	 * 
	 * @return 服务端口
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * 获取 key
	 * 
	 * @return key
	 */
	public String getKey() {
		return this.ip + ":" + this.port;
	}
}
