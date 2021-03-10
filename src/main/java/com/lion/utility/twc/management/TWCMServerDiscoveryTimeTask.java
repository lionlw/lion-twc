package com.lion.utility.twc.management;

import java.util.TimerTask;

import com.lion.utility.tool.log.LogLIB;

/**
 * twc服务发现
 * 
 * @author lion
 *
 */
class TWCMServerDiscoveryTimeTask extends TimerTask {
	private TWCMServerDiscovery twcMServerDiscovery;

	public TWCMServerDiscoveryTimeTask(TWCMServerDiscovery twcMServerDiscovery) {
		this.twcMServerDiscovery = twcMServerDiscovery;
	}

	@Override
	public void run() {
		try {
			this.twcMServerDiscovery.setTWCServer();
		} catch (Exception e) {
			LogLIB.error("TWCMServerDiscoveryTimeTask exception", e);
		}
	}
}
