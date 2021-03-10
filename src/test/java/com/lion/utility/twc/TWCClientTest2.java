package com.lion.utility.twc;

import com.lion.utility.twc.client.TWCClient;
import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.config.TWCClientConfig;
import com.lion.utility.tool.file.JsonLIB;
import com.lion.utility.framework.web.i.entity.IResult;

public class TWCClientTest2 {
	private static TWCClient twcClient;

	public static void main(String[] args) throws Exception {
		TWCClientConfig twcClientConfig = new TWCClientConfig();
		twcClientConfig.setIsDebug(true);
		twcClientConfig.setLogLevel(Constant.LOGLEVEL_INOUTERROR);

		TWCClientTest2.twcClient = new TWCClient("user2", "TestService", "192.168.2.131:2181", new ClientHandlerMessage());
		TWCClientTest2.twcClient.setTWCClientConfig(twcClientConfig);
		TWCClientTest2.twcClient.start();

		System.out.println("start");

		while (true) {
//			try {
//				TWCClientTest2.test1();
//				TWCClientTest2.test2();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

			Thread.sleep(1L * 5000L);
		}
	}

	private static void test1() throws Exception {
		IResult<Integer> iResult = TWCClientTest2.twcClient.handler("server-test1", 1);
		System.out.println("TWCClientTest: server-test1: " + JsonLIB.toJson(iResult));

		IResult<Integer> iResult2 = TWCClientTest2.twcClient.handler("server-test1", 10);
		System.out.println("TWCClientTest: server-test1: " + JsonLIB.toJson(iResult2));
	}

	private static void test2() throws Exception {
		IResult<Integer> iResult = TWCClientTest2.twcClient.handler("server-test2", 1, 20);
		System.out.println("TWCClientTest: server-test2: " + JsonLIB.toJson(iResult));
	}
}
