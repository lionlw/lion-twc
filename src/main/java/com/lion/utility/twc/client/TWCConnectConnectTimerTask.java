package com.lion.utility.twc.client;

import java.util.TimerTask;

import com.lion.utility.tool.log.LogLIB;

/**
 * 延迟连接twc任务
 * 
 * @author lion
 *
 */
class TWCConnectConnectTimerTask extends TimerTask {
	private TWCConnect twcConnect;

	public TWCConnectConnectTimerTask(TWCConnect twcConnect) {
		this.twcConnect = twcConnect;
	}

	@Override
	public void run() {
		try {
			if (this.twcConnect.isAlive()) {
				return;
			}

			this.twcConnect.doConnect();
		} catch (Exception e) {
			LogLIB.error("TWCConnectConnectTimerTask exception", e);
		}
	}
}
