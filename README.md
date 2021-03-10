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
#### 示例


### management
管理端，某些分布式集群应用，需要有个统一的控制端，向server或者client发送指令消息。此时可通过集成管理端来实现。
#### 配置
#### 示例

