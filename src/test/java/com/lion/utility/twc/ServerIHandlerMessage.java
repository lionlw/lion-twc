package com.lion.utility.twc;

import com.lion.utility.twc.tool.IHandlerMessage;
import com.lion.utility.framework.web.i.ILIB;
import com.lion.utility.framework.web.i.entity.IResult;

public class ServerIHandlerMessage implements IHandlerMessage {

	@Override
	public IResult<Object> handler(String methodId, Object paramObj) {
		IResult<Object> result = null;

		switch (methodId) {
		case "server-test1": {
			Integer i = (Integer) paramObj;
			result = ILIB.getIResultSucceed("server-test1 result", (i + 10));
		}
			break;
		case "server-test2": {
			Integer i = (Integer) paramObj;
			result = ILIB.getIResultSucceed("server-test2 result", (i + 20));

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
			break;
		}

		return result;
	}

}
