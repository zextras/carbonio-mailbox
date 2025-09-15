package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.service.ServiceException;

public class RightException extends ServiceException {

	private static final String NO_SUCH_RIGHT = "account.NO_SUCH_RIGHT";

	protected RightException(String message, String code, boolean isReceiversFault, Throwable cause,
			Argument... arguments) {
		super(message, code, isReceiversFault, cause, arguments);
	}

	public static RightException NO_SUCH_RIGHT(String name) {
		return new RightException(
				"no such right: " + name, NO_SUCH_RIGHT, ServiceException.SENDERS_FAULT, null);
	}

}
