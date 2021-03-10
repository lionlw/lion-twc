package com.lion.utility.twc.tool;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import com.lion.utility.tool.log.LogLIB;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.TWCAddress;

/**
 * 服务注册
 * 
 * @author lion
 *
 */
public class ServiceRegister {
	private ServiceRegister() {
	}

	private static CuratorFramework curatorFramework = null;

	/**
	 * zk执行
	 * 
	 * @param basePath      基础路径
	 * @param serviceName   服务名
	 * @param serverAddress 服务地址
	 * @param zkServerUrl   zk地址
	 */
	public static void zkHandler(String basePath, String serviceName, TWCAddress serverAddress, String zkServerUrl) {
		try {
			LogLIB.info("ServiceRegister zk start");

			// zk初始化
			ServiceRegister.curatorFramework = CuratorFrameworkFactory.builder()
					.namespace(Constant.TWC_REGISTER_ZK_NAMESPACE)
					.connectString(zkServerUrl)
					.sessionTimeoutMs(30000)
					.connectionTimeoutMs(15000)
					.retryPolicy(new ExponentialBackoffRetry(Constant.ZK_RETRYPOLICY_BASESLEEPTIMEMS, Constant.ZK_RETRYPOLICY_MAXRETRIES))
					.build();
			ServiceRegister.curatorFramework.start();

			ServiceInstanceBuilder<Object> sib = ServiceInstance.builder();
			sib.address(serverAddress.getIp());
			sib.port(serverAddress.getPort());
			sib.name(serviceName);

			ServiceInstance<Object> instance = sib.build();

			ServiceDiscovery<Object> serviceDiscovery = ServiceDiscoveryBuilder.builder(Object.class)
					.client(ServiceRegister.curatorFramework)
					.serializer(new JsonInstanceSerializer<Object>(Object.class))
					.basePath(basePath)
					.build();
			// 服务注册
			serviceDiscovery.registerService(instance);
			serviceDiscovery.start();

			LogLIB.info("ServiceRegister zk start Succeed");
		} catch (Exception e) {
			LogLIB.error("ServiceRegister zk start exception", e);
		}
	}
}
