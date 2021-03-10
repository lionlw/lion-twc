# lion-twc

## 特点
基于netty开发，实现client/server数据双向通信，同时提供manage与所有server交互，对client进行消息统一发送。
使用场景：分布式集群应用的双向数据通信。

## 说明

### client
客户端，可以理解为分布式集群应用的工作节点。
#### 配置
配置项|说明
----|-----
bizThreadPoolThreadTotal|业务线程池中线程个数
connectTimeoutSecond|twc链接超时秒数
connectTimeoutCheckIntervalSecond|twc连接超时检测时间间隔秒数
connectTimeoutThreshold|twc连接超时次数阈值
connectTimeoutForbidIntervalSecond|twc连接超时，达到阈值，禁止请求的时间间隔秒数
readTimeoutSecond|twc读取超时秒数
isDebug|是否debug模式
logLevel|日志输出级别
messageRecieveMaxLength|最大接收消息字节数
compressPolicy|压缩策略
encryptPolicy|加密策略
ioThreads|io线程数
idleTimeSeconds|连接空闲时间（单位：秒）
#### 示例
```
public class TWCClientTest1 {
	private static TWCClient twcClient;

	public static void main(String[] args) throws Exception {
		TWCClientConfig twcClientConfig = new TWCClientConfig();
		twcClientConfig.setIsDebug(true);
		twcClientConfig.setLogLevel(Constant.LOGLEVEL_INOUTERROR);

		TWCClientTest1.twcClient = new TWCClient("user1", "TestService", "192.168.2.131:2181", new ClientHandlerMessage());
		TWCClientTest1.twcClient.setTWCClientConfig(twcClientConfig);
		TWCClientTest1.twcClient.start();

		System.out.println("start");

		while (true) {
//			try {
//				TWCClientTest1.test1();
//				TWCClientTest1.test2();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

			Thread.sleep(1L * 5000L);
		}
	}

	private static void test1() throws Exception {
		IResult<Integer> iResult = TWCClientTest1.twcClient.handler("server-test1", 1);
		System.out.println("TWCClientTest: server-test1: " + JsonLIB.toJson(iResult));

		IResult<Integer> iResult2 = TWCClientTest1.twcClient.handler("server-test1", 10);
		System.out.println("TWCClientTest: server-test1: " + JsonLIB.toJson(iResult2));
	}

	private static void test2() throws Exception {
		IResult<Integer> iResult = TWCClientTest1.twcClient.handler("server-test2", 1, 20);
		System.out.println("TWCClientTest: server-test2: " + JsonLIB.toJson(iResult));
	}
}
```

### server
服务端，可以理解为分布式集群应用的控制节点，比如leader。
#### 配置
配置项|说明
----|-----
bizThreadPoolThreadTotal|业务线程池中线程个数
connectTimeoutSecond|twc链接超时秒数
connectTimeoutCheckIntervalSecond|twc连接超时检测时间间隔秒数
connectTimeoutThreshold|twc连接超时次数阈值
connectTimeoutForbidIntervalSecond|twc连接超时，达到阈值，禁止请求的时间间隔秒数
readTimeoutSecond|twc读取超时秒数
isDebug|是否debug模式
logLevel|日志输出级别
messageRecieveMaxLength|最大接收消息字节数
compressPolicy|压缩策略
encryptPolicy|加密策略
ioThreads|io线程数
idleTimeSeconds|连接空闲时间（单位：秒）
#### 示例
```
public class TWCServerTest1 {
	private static TWCServer twcServer;

	public static void main(String[] args) throws Exception {
		TWCAddress serverAddress = new TWCAddress();
		serverAddress.setIp("127.0.0.1");
		serverAddress.setPort(8888);

		TWCServerConfig twcServerConfig = new TWCServerConfig();
		twcServerConfig.setIsDebug(true);
		twcServerConfig.setLogLevel(Constant.LOGLEVEL_INOUTERROR);

		TWCServerTest1.twcServer = new TWCServer("TestService", "192.168.2.131:2181", serverAddress, new ServerIHandlerMessage());
		TWCServerTest1.twcServer.setTWCServerConfig(twcServerConfig);
		TWCServerTest1.twcServer.start();

		System.out.println("start");

		while (true) {
			try {
				TWCServerTest1.test1();
//				TWCServerTest1.test2();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Thread.sleep(1L * 5000L);
		}
	}

	public static void test1() throws Exception {
		System.out.println("TWCServerTest1: getClientIds: " + TWCServerTest1.twcServer.getClientIds());

		IResult<Integer> result = TWCServerTest1.twcServer.handlerClient("user1", "client-test1", 1);
		System.out.println("TWCServerTest1: client-test1: " + JsonLIB.toJson(result));
//
//		List<String> list = new ArrayList<>();
//		list.add("user1");
//		list.add("user2");
//		Map<String, IResult<Integer>> resultMap = TWCServerTest1.twcServer.handlerClients(list, "client-test1", 2);
//		System.out.println("TWCServerTest1: client-test1: " + JsonLIB.toJson(resultMap));

//		Map<String, IResult<Integer>> resultMap2 = TWCServerTest1.twcServer.handlerAllClient("client-test1", 3);
//		System.out.println("TWCServerTest1: client-test1: " + JsonLIB.toJson(resultMap2));
	}

	public static void test2() throws Exception {
		IResult<Integer> result = TWCServerTest1.twcServer.handlerClient("user1", "client-test2", 1, 20);
		System.out.println("TWCServerTest1: client-test2: " + JsonLIB.toJson(result));
	}

}
```

### management
管理端，某些分布式集群应用，需要有个统一的控制端，向server或者client发送指令消息。此时可通过集成管理端来实现。
#### 配置
#### 示例
```
public class TWCManagementTest {
	private static TWCManagement twcManagement;

	public static void main(String[] args) throws Exception {
		TWCAddress serverAddress = new TWCAddress();
		serverAddress.setIp("127.0.0.1");
		serverAddress.setPort(8888);

		TWCManagementConfig twcManagementConfig = new TWCManagementConfig();
		twcManagementConfig.setIsDebug(true);
		twcManagementConfig.setLogLevel(Constant.LOGLEVEL_INOUTERROR);

		TWCManagementTest.twcManagement = new TWCManagement("management1", "TestService", "192.168.2.131:2181");
		TWCManagementTest.twcManagement.setTWCManagementConfig(twcManagementConfig);
		TWCManagementTest.twcManagement.start();

		System.out.println("start");

		while (true) {
			TWCManagementTest.test1();
			TWCManagementTest.test2();

			Thread.sleep(1L * 5000L);
		}
	}

	public static void test1() throws Exception {
		System.out.println("TWCManagementTest: getClientIds: " + TWCManagementTest.twcManagement.getClientIds());

		IResult<Integer> result = TWCManagementTest.twcManagement.handlerClient("user1", "client-test1", 1);
		System.out.println("TWCManagementTest: client-test1: " + JsonLIB.toJson(result));

		List<String> list = new ArrayList<>();
		list.add("user1");
		IResult<Map<String, IResult<Integer>>> iResult = TWCManagementTest.twcManagement.handlerClients(list, "client-test1", 2);
		System.out.println("TWCManagementTest: client-test1: " + JsonLIB.toJson(iResult));

		IResult<Map<String, IResult<Integer>>> iResult2 = TWCManagementTest.twcManagement.handlerAllClient("client-test1", 3);
		System.out.println("TWCManagementTest: client-test1: " + JsonLIB.toJson(iResult2));
	}

	public static void test2() throws Exception {
		IResult<Integer> result = TWCManagementTest.twcManagement.handlerClient("user1", "client-test2", 1, 20);
		System.out.println("TWCManagementTest: client-test2: " + JsonLIB.toJson(result));
	}
}
```

