package com.lion.utility.twc.entity.config;

/**
 * 压缩策略
 * 
 * @author lion
 */
public class CompressPolicy {
	/**
	 * 是否输出进行压缩
	 */
	private Boolean isCompress;
	/**
	 * 当输出内容大于等于此值时进行压缩，当isCompress=true时有效
	 */
	private Integer compressMinLength;

	/**
	 * 构造方法
	 * 
	 * @param isCompress
	 *            是否输出进行压缩
	 * @param compressMinLength
	 *            当输出内容大于等于此值时进行压缩，当isCompress=true时有效
	 */
	public CompressPolicy(boolean isCompress, int compressMinLength) {
		this.isCompress = isCompress;
		this.compressMinLength = compressMinLength;
	}

	/**
	 * 设置是否输出进行压缩
	 * 
	 * @param isCompress
	 *            是否输出进行压缩
	 */
	public void setIsCompress(Boolean isCompress) {
		this.isCompress = isCompress;
	}

	/**
	 * 获取是否输出进行压缩
	 * 
	 * @return 是否输出进行压缩
	 */
	public Boolean getIsCompress() {
		return isCompress;
	}

	/**
	 * 设置当输出内容大于等于此值时进行压缩，当isCompress=true时有效
	 * 
	 * @param compressMinLength
	 *            当输出内容大于等于此值时进行压缩，当isCompress=true时有效
	 */
	public void setCompressMinLength(Integer compressMinLength) {
		this.compressMinLength = compressMinLength;
	}

	/**
	 * 获取当输出内容大于等于此值时进行压缩，当isCompress=true时有效
	 * 
	 * @return 当输出内容大于等于此值时进行压缩，当isCompress=true时有效
	 */
	public Integer getCompressMinLength() {
		return compressMinLength;
	}

}
