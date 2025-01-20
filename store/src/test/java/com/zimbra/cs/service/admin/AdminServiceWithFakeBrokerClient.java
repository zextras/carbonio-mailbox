package com.zimbra.cs.service.admin;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.mailbox.client.FilesInstalledProvider;
import com.zextras.mailbox.client.ServiceInstalledProvider;
import io.vavr.control.Try;
import org.mockito.Mockito;

public class AdminServiceWithFakeBrokerClient extends AdminService {

	private static class MockFilesInstalledProvider implements ServiceInstalledProvider {

		@Override
		public boolean isInstalled() {
			return false;
		}
	}

	@Override
	protected Try<MessageBrokerClient> getMessageBroker() {
		return Try.of(() -> Mockito.mock(MessageBrokerClient.class));
	}

	@Override
	protected ServiceInstalledProvider getFilesInstalledServiceProvider() {
		return new MockFilesInstalledProvider();
	}
}
