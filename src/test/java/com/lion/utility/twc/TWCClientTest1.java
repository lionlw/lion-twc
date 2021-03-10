package com.lion.utility.twc;

import com.lion.utility.twc.client.TWCClient;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.config.TWCClientConfig;
import com.lion.utility.tool.file.JsonLIB;
import com.lion.utility.framework.web.i.entity.IResult;

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
