package com.zextras.mailbox.messageBroker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MessageBrokerFactoryTest {

	@Test
	void shouldFailCreatingClient_WhenConsulTokenIsMissing() throws Exception {
		Assertions.assertThrows(CreateMessageBrokerException.class,
				MessageBrokerFactory::getMessageBrokerClientInstance);
	}

}