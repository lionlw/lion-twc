package com.lion.utility.twc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lion.utility.twc.constant.Constant;
import com.lion.utility.twc.entity.TWCAddress;
import com.lion.utility.twc.entity.config.TWCManagementConfig;
import com.lion.utility.twc.management.TWCManagement;
import com.lion.utility.tool.file.JsonLIB;
import com.lion.utility.framework.web.i.entity.IResult;

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
