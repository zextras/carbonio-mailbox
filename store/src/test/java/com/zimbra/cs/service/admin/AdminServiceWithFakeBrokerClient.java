package com.zimbra.cs.service.admin;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import io.vavr.control.Try;
import org.mockito.Mockito;

public class AdminServiceWithFakeBrokerClient extends AdminService {

	@Override
	protected Try<MessageBrokerClient> tryGetMessageBroker() {
		return Try.of(() -> Mockito.mock(MessageBrokerClient.class));
	}
}
