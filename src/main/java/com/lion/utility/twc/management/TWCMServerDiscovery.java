package com.lion.utility.twc.management;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;

import com.lion.utility.tool.thread.CustomThreadFactory;
import com.lion.utility.tool.file.JsonLIB;
import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.TWCAddress;

/**
 * twc服务发现
 * 
 * @author lion
 *
 */
class TWCMServerDiscovery {
	private TWCManagement twcManagement;

	private ServiceDiscovery<Object> serviceDiscovery = null;

	public TWCMServerDiscovery(TWCManagement twcManagement) {
		this.twcManagement = twcManagement;
	}

	/**
	 * 直连
	 * 
	 * @param serverAddress
	 *            服务地址
	 */
	public void directHandler(TWCAddress serverAddress) {
		try {
			LogLIB.info("TWCMServerDiscovery directHandler start");

			TWCAddress serverBasicInfo = new TWCAddress();
			serverBasicInfo.setIp(serverAddress.getIp());
			serverBasicInfo.setPort(serverAddress.getPort());

			TWCMConnect twcConnect = new TWCMConnect(this.twcManagement, serverBasicInfo);
			twcConnect.start();

			this.twcManagement.twcMConnectMap.put(serverAddress.getKey(), twcConnect);

			LogLIB.info("TWCMServerDiscovery directHandler start Succeed");
		} catch (Exception e) {
			LogLIB.error("TWCMServerDiscovery directHandler start exception", e);
		}
	}

	/**
	 * zk服务发现
	 * 
	 * @return
	 */
	public void zkHandler() {
		try {
			LogLIB.info("TWCMServerDiscovery zkHandler start");

			CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
					.namespace(Constant.TWC_REGISTER_ZK_NAMESPACE)
					.connectString(this.twcManagement.zkServerUrl)
					.sessionTimeoutMs(30000)
					.connectionTimeoutMs(15000)
					.retryPolicy(new ExponentialBackoffRetry(Constant.ZK_RETRYPOLICY_BASESLEEPTIMEMS, Constant.ZK_RETRYPOLICY_MAXRETRIES))
					.build();
			curatorFramework.start();

			this.serviceDiscovery = ServiceDiscoveryBuilder.builder(Object.class)
					.client(curatorFramework)
					.basePath(Constant.TWC_SERVER_REGISTER_ZK_BASEPATH)
					.build();
			this.serviceDiscovery.start();

			// 强制处理
			this.setTWCServer();

			// 定时获取处理
			ScheduledExecutorService ses = Executors.newScheduledThreadPool(1, new CustomThreadFactory("TWCMServer", "TWCMServerDiscoveryTimeTask"));
			ses.scheduleAtFixedRate(new TWCMServerDiscoveryTimeTask(this), 10L, 10L, TimeUnit.SECONDS);

			LogLIB.info("TWCMServerDiscovery zkHandler start Succeed");
		} catch (Exception e) {
			LogLIB.error("TWCMServerDiscovery zkHandler start exception", e);
		}
	}

	/**
	 * 设置twc服务
	 * 
	 * @throws Exception
	 */
	protected void setTWCServer() throws Exception {
		// 根据名称获取服务
		Map<String, TWCAddress> sMap = new HashMap<>();
		Collection<ServiceInstance<Object>> services = this.serviceDiscovery.queryForInstances(this.twcManagement.serviceName);
		for (ServiceInstance<Object> service : services) {
			TWCAddress serverBasicInfo = new TWCAddress();
			serverBasicInfo.setIp(service.getAddress());
			serverBasicInfo.setPort(service.getPort());
			sMap.put(serverBasicInfo.getKey(), serverBasicInfo);
		}

		boolean isChange = false;
		StringBuilder slog = new StringBuilder();

		// map删除
		for (Entry<String, TWCMConnect> entry : this.twcManagement.twcMConnectMap.entrySet()) {
			TWCAddress serverBasicInfo = sMap.get(entry.getKey());
			if (serverBasicInfo == null) {
				TWCMConnect twcConnect = entry.getValue();
				twcConnect.stop();
				this.twcManagement.twcMConnectMap.remove(entry.getKey());

				isChange = true;
				slog.append("TWCMServerDiscovery twcMConnectMap " + twcConnect.getServerInfo() + " remove, ");
			}
		}

		// map增加
		for (Entry<String, TWCAddress> entry : sMap.entrySet()) {
			if (!this.twcManagement.twcMConnectMap.containsKey(entry.getKey())) {
				TWCMConnect twcConnect = new TWCMConnect(this.twcManagement, entry.getValue());
				twcConnect.start();
				this.twcManagement.twcMConnectMap.put(entry.getKey(), twcConnect);

				isChange = true;
				slog.append("TWCMServerDiscovery twcMConnectMap " + twcConnect.getServerInfo() + " add, ");
			}
		}

		if (isChange) {
			slog.append("Final TWCMServerDiscovery twcMConnectMap:" + JsonLIB.toJson(this.twcManagement.twcMConnectMap.keySet()));
			LogLIB.info(slog.toString());
		}
	}
}
