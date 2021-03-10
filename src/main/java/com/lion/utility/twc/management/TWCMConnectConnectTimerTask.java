package com.lion.utility.twc.management;

import java.util.TimerTask;

import com.lion.utility.tool.log.LogLIB;

/**
 * 延迟连接twc任务
 * 
 * @author lion
 *
 */
class TWCMConnectConnectTimerTask extends TimerTask {
	private TWCMConnect twcMConnect;

	public TWCMConnectConnectTimerTask(TWCMConnect twcMConnect) {
		this.twcMConnect = twcMConnect;
	}

	@Override
	public void run() {
		try {
			if (this.twcMConnect.isAlive()) {
				return;
			}

			this.twcMConnect.doConnect();
		} catch (Exception e) {
			LogLIB.error("TWCMConnectConnectTimerTask exception", e);
		}
	}
}
