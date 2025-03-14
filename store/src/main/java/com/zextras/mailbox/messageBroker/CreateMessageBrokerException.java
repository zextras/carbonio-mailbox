// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.messageBroker;

public class CreateMessageBrokerException extends Exception {

	public CreateMessageBrokerException(Exception e) {
		super("Cannot create message broker client", e);
	}

	public CreateMessageBrokerException(String msg) {
		super("Cannot create message broker client: " + msg);
	}

}
