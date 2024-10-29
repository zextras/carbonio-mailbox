package com.zextras.mailbox.messageBroker;

public class CreateMessageBrokerException extends Exception {

	public CreateMessageBrokerException(Exception e) {
		super("Cannot create message broker client", e);
	}

}
