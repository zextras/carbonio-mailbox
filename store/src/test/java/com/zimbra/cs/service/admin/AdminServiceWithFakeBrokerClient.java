package com.zimbra.cs.service.admin;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import org.mockito.Mockito;

public class AdminServiceWithFakeBrokerClient extends AdminService {

	@Override
	protected MessageBrokerClient getMessageBroker() {
		return Mockito.mock(MessageBrokerClient.class);
	}
}
