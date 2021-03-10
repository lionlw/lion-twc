package com.lion.utility.twc.client;

import java.util.TimerTask;
import java.util.Map.Entry;

import com.lion.utility.tool.log.LogLIB;

/**
 * twc连接超时管理
 * 
 * @author lion
 *
 */
class TWCClientConnectTimeoutManageTimerTask extends TimerTask {
	private TWCClient twcClient;

	public TWCClientConnectTimeoutManageTimerTask(TWCClient twcClient) {
		this.twcClient = twcClient;
	}

	@Override
	public void run() {
		try {
			for (Entry<String, TWCConnect> entry : this.twcClient.twcConnectMap.entrySet()) {
				// 判断twcconnect连接超时错误是否达到阈值
				int curTotal = entry.getValue().connectTimeoutTotal.get();
				if (curTotal >= this.twcClient.twcClientConfig.getConnectTimeoutThreshold()) {
					// 写入禁止访问名单
					this.twcClient.twcConnectTimeoutCache.put(entry.getKey(), "timeout");
					LogLIB.info(entry.getKey() + " " + this.twcClient.twcClientConfig.getConnectTimeoutCheckIntervalSecond() +
							"s connectTimeout total is " + curTotal + " >= " + this.twcClient.twcClientConfig.getConnectTimeoutThreshold() +
							", so add to forbid list");
				}

				// 阈值清0
				entry.getValue().connectTimeoutTotal.set(0);
			}
		} catch (Exception e) {
			LogLIB.error("TWCClientConnectTimeoutManageTimerTask exception", e);
		}
	}
}
