package com.lion.utility.twc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.TWCAddress;
import com.lion.utility.twc.entity.config.TWCServerConfig;
import com.lion.utility.twc.server.TWCServer;
import com.lion.utility.tool.file.JsonLIB;
import com.lion.utility.framework.web.i.entity.IResult;

public class TWCServerTest2 {

	public static TWCServer twcServer;

	public static void main(String[] args) throws Exception {
		TWCAddress serverAddress = new TWCAddress();
		serverAddress.setIp("127.0.0.1");
		serverAddress.setPort(8889);

		TWCServerConfig twcServerConfig = new TWCServerConfig();
		twcServerConfig.setIsDebug(true);
		twcServerConfig.setLogLevel(Constant.LOGLEVEL_INOUTERROR);

		TWCServerTest2.twcServer = new TWCServer("TestService", "192.168.2.131:2181", serverAddress, new ServerIHandlerMessage());
		TWCServerTest2.twcServer.setTWCServerConfig(twcServerConfig);
		TWCServerTest2.twcServer.start();

		System.out.println("start");

		while (true) {
			try {
				TWCServerTest2.test1();
				TWCServerTest2.test2();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Thread.sleep(1L * 5000L);
		}
	}

	public static void test1() throws Exception {
		System.out.println("TWCServerTest1: getClientIds: " + TWCServerTest2.twcServer.getClientIds());

		IResult<Integer> result = TWCServerTest2.twcServer.handlerClient("user1", "client-test1", 1);
		System.out.println("TWCServerTest1: client-test1: " + JsonLIB.toJson(result));

		List<String> list = new ArrayList<>();
		list.add("user1");
		Map<String, IResult<Integer>> resultMap = TWCServerTest2.twcServer.handlerClients(list, "client-test1", 2);
		System.out.println("TWCServerTest1: client-test1: " + JsonLIB.toJson(resultMap));

		Map<String, IResult<Integer>> resultMap2 = TWCServerTest2.twcServer.handlerAllClient("client-test1", 3);
		System.out.println("TWCServerTest1: client-test1: " + JsonLIB.toJson(resultMap2));
	}

	public static void test2() throws Exception {
		IResult<Integer> result = TWCServerTest2.twcServer.handlerClient("user1", "client-test2", 1, 20);
		System.out.println("TWCServerTest1: client-test2: " + JsonLIB.toJson(result));
	}

}
