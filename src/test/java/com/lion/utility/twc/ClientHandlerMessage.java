package com.lion.utility.twc;

import com.lion.utility.twc.tool.IHandlerMessage;
import com.lion.utility.framework.web.i.ILIB;
import com.lion.utility.framework.web.i.entity.IResult;

public class ClientHandlerMessage implements IHandlerMessage {

	@Override
	public IResult<Object> handler(String methodId, Object paramObj) {
		IResult<Object> result = null;

		switch (methodId) {
		case "client-test1": {
			Integer i = (Integer) paramObj;
			result = ILIB.getIResultSucceed("client-test1 result", (i + 1000));
		}
			break;
		case "client-test2": {
			Integer i = (Integer) paramObj;
			result = ILIB.getIResultSucceed("client-test2 result", (i + 2000));

			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
			}
		}
			break;
		}

		return result;
	}

}
