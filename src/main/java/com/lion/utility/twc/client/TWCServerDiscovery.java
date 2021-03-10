package com.lion.utility.twc.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
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
class TWCServerDiscovery {
	private TWCClient twcClient;

	private ServiceDiscovery<Object> serviceDiscovery = null;

	public TWCServerDiscovery(TWCClient twcClient) {
		this.twcClient = twcClient;
	}

	/**
	 * 直连
	 * 
	 * @param serverAddress 服务地址
	 */
	public void directHandler(TWCAddress serverAddress) {
		try {
			LogLIB.info("TWCServerDiscovery directHandler start");

			TWCAddress serverBasicInfo = new TWCAddress();
			serverBasicInfo.setIp(serverAddress.getIp());
			serverBasicInfo.setPort(serverAddress.getPort());

			TWCConnect twcConnect = new TWCConnect(this.twcClient, serverBasicInfo);
			twcConnect.start();

			this.twcClient.twcConnectMap.put(serverAddress.getKey(), twcConnect);
			this.twcClient.twcConnects.add(twcConnect);

			LogLIB.info("TWCServerDiscovery directHandler start Succeed");
		} catch (Exception e) {
			LogLIB.error("TWCServerDiscovery directHandler start exception", e);
		}
	}

	/**
	 * zk服务发现
	 * 
	 * @return
	 */
	public void zkHandler() {
		try {
			LogLIB.info("TWCServerDiscovery zkHandler start");

			CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
					.namespace(Constant.TWC_REGISTER_ZK_NAMESPACE)
					.connectString(this.twcClient.zkServerUrl)
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
			ScheduledExecutorService ses = Executors.newScheduledThreadPool(1, new CustomThreadFactory("TWCServer", "TWCServerDiscoveryTimeTask"));
			ses.scheduleAtFixedRate(new TWCServerDiscoveryTimeTask(this), 10L, 10L, TimeUnit.SECONDS);

			LogLIB.info("TWCServerDiscovery zkHandler start Succeed");
		} catch (Exception e) {
			LogLIB.error("TWCServerDiscovery zkHandler start exception", e);
		}
	}

	/**
	 * 设置twc服务
	 * 
	 * @throws Exception
	 */
	protected void setTWCServer() throws Exception {
		StringBuilder slog = new StringBuilder();
		slog.append("TWCServerDiscovery: ");

		// 从zk中获取全量服务集合
		Map<String, TWCAddress> availableMap = new HashMap<>();
		Collection<ServiceInstance<Object>> services = this.serviceDiscovery.queryForInstances(this.twcClient.serviceName);
		for (ServiceInstance<Object> service : services) {
			TWCAddress serverBasicInfo = new TWCAddress();
			serverBasicInfo.setIp(service.getAddress());
			serverBasicInfo.setPort(service.getPort());
			availableMap.put(serverBasicInfo.getKey(), serverBasicInfo);
		}

		// 遍历可用全量服务集合，对进入twc连接超时禁止名单的连接进行删除
		Iterator<Entry<String, TWCAddress>> it = availableMap.entrySet().iterator();
		while (it.hasNext()) {
			if (availableMap.size() <= 1) {
				// 确保当前连接集合至少存在1个
				slog.append("availableMap is " + availableMap.size() + " so not check(in forbid list), ");
				break;
			}

			Entry<String, TWCAddress> entry = it.next();
			if (this.twcClient.twcConnectTimeoutCache.getIfPresent(entry.getKey()) != null) {
				// 处于禁止名单，则删除
				it.remove();
				slog.append("availableMap " + entry.getKey() + " remove(in forbid list), ");
			}
		}

		boolean isChange = false;

		// 遍历当前twc连接集合，用于对当前连接集合进行更新和删除
		for (Entry<String, TWCConnect> entry : this.twcClient.twcConnectMap.entrySet()) {
			TWCAddress serverBasicInfo = availableMap.get(entry.getKey());
			if (serverBasicInfo == null) {
				TWCConnect twcConnect = entry.getValue();
				twcConnect.stop();
				this.twcClient.twcConnectMap.remove(entry.getKey());

				isChange = true;
				slog.append("twcConnectMap " + twcConnect.getServerInfo() + " remove, ");
			}
		}

		// 遍历zk获取的全量服务集合，用于对当前连接集合进行增加
		for (Entry<String, TWCAddress> entry : availableMap.entrySet()) {
			if (!this.twcClient.twcConnectMap.containsKey(entry.getKey())) {
				TWCConnect twcConnect = new TWCConnect(this.twcClient, entry.getValue());
				twcConnect.start();
				this.twcClient.twcConnectMap.put(entry.getKey(), twcConnect);

				isChange = true;
				slog.append("twcConnectMap " + twcConnect.getServerInfo() + " add, ");
			}
		}

		// 如果有变化，就更新list
		if (isChange) {
			// list删除
			for (TWCConnect twcConnect : this.twcClient.twcConnects) {
				if (!this.twcClient.twcConnectMap.containsKey(twcConnect.serverBasicInfo.getKey())) {
					this.twcClient.twcConnects.remove(twcConnect);
					slog.append("twcConnects " + twcConnect.getServerInfo() + " remove, ");
				}
			}

			// list增加
			for (Entry<String, TWCConnect> entry : this.twcClient.twcConnectMap.entrySet()) {
				Optional<TWCConnect> obj = this.twcClient.twcConnects.stream().filter(
						twcConnect -> twcConnect.serverBasicInfo.getKey().equals(entry.getKey())).limit(1).findFirst();
				if (!obj.isPresent()) {
					this.twcClient.twcConnects.add(entry.getValue());
					slog.append("twcConnects " + entry.getValue().getServerInfo() + " add, ");
				}
			}

			slog.append("Final TWCServerDiscovery twcConnectMap:" + JsonLIB.toJson(this.twcClient.twcConnectMap.keySet()) + ", ");
			slog.append("Final TWCServerDiscovery twcConnects:" + this.twcClient.twcConnects.stream().map(
					twcConnect -> {
						return twcConnect.serverBasicInfo.getKey();
					}).collect(Collectors.joining(",")));

			LogLIB.info(slog.toString());
		}
	}
}
