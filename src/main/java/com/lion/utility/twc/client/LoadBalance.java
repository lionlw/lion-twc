package com.lion.utility.twc.client;

import com.lion.utility.tool.common.Tool;

/**
 * 负载均衡实现类
 * 
 * @author lion
 *
 */
class LoadBalance {
	private LoadBalance() {
	}

	/**
	 * 随机
	 * 
	 * @param size 数据集大小
	 * @return 索引
	 * @throws Exception
	 */
	public static int random(int size) throws Exception {
		return Tool.getRndnum(0, size - 1);
	}
}
