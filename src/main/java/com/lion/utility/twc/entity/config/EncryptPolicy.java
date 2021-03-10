package com.lion.utility.twc.entity.config;

/**
 * 加密策略
 * 
 * @author lion
 */
public class EncryptPolicy {
	/**
	 * 是否输出加密传输
	 */
	private Boolean isEncrypt;
	/**
	 * 加密传输的key，当isEncrypt=true时有效
	 */
	private String encryptKey;

	/**
	 * 构造方法
	 * 
	 * @param isEncrypt
	 *            是否输出加密传输
	 * @param encryptKey
	 *            加密传输的key，当isEncrypt=true时有效
	 */
	public EncryptPolicy(boolean isEncrypt, String encryptKey) {
		this.isEncrypt = isEncrypt;
		this.encryptKey = encryptKey;
	}

	/**
	 * 设置是否输出加密传输
	 * 
	 * @param isEncrypt
	 *            是否输出加密传输
	 */
	public void setIsEncrypt(Boolean isEncrypt) {
		this.isEncrypt = isEncrypt;
	}

	/**
	 * 获取是否输出加密传输
	 * 
	 * @return 是否输出加密传输
	 */
	public Boolean getIsEncrypt() {
		return isEncrypt;
	}

	/**
	 * 设置加密传输的key，当isEncrypt=true时有效
	 * 
	 * @param encryptKey
	 *            加密传输的key，当isEncrypt=true时有效
	 */
	public void setEncryptKey(String encryptKey) {
		this.encryptKey = encryptKey;
	}

	/**
	 * 获取加密传输的key，当isEncrypt=true时有效
	 * 
	 * @return 加密传输的key，当isEncrypt=true时有效
	 */
	public String getEncryptKey() {
		return encryptKey;
	}

}
