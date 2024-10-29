package com.zextras.mailbox.messageBroker;

public class CreateMessageBrokerException extends Exception {

	public CreateMessageBrokerException(Throwable e) {
		super("Cannot create message broker client", e);
	}

}
