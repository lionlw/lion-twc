package com.lion.utility.twc.client;

import java.util.TimerTask;

import com.lion.utility.tool.log.LogLIB;

/**
 * twc服务发现
 * 
 * @author lion
 *
 */
class TWCServerDiscoveryTimeTask extends TimerTask {
	private TWCServerDiscovery twcServerDiscovery;

	public TWCServerDiscoveryTimeTask(TWCServerDiscovery twcServerDiscovery) {
		this.twcServerDiscovery = twcServerDiscovery;
	}

	@Override
	public void run() {
		try {
			this.twcServerDiscovery.setTWCServer();
		} catch (Exception e) {
			LogLIB.error("TWCServerDiscoveryTimeTask exception", e);
		}
	}
}
