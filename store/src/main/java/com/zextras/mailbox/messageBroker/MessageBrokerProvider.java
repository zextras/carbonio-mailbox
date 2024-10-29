package com.zextras.mailbox.messageBroker;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.config.enums.Service;
import com.zextras.mailbox.client.ServiceDiscoverHttpClient;
import com.zextras.mailbox.consul.ConsulTokenProvider;
import com.zextras.mailbox.consul.FileConsulTokenProvider;
import java.nio.file.Paths;

public class MessageBrokerProvider {

	private final ConsulTokenProvider consulTokenProvider;

	public MessageBrokerProvider(ConsulTokenProvider consulTokenProvider) {
		this.consulTokenProvider = consulTokenProvider;
	}

	public static MessageBrokerProvider getDefault() {
		return new MessageBrokerProvider(
				new FileConsulTokenProvider(Paths.get("/etc/carbonio/mailbox/service-discover/token")));
	}

	public MessageBrokerClient getMessageBrokerClientInstance()
			throws CreateMessageBrokerException {
		final String token = consulTokenProvider.getToken()
				.getOrElseThrow(CreateMessageBrokerException::new);
		ServiceDiscoverHttpClient serviceDiscoverHttpClient =
				ServiceDiscoverHttpClient.defaultURL("carbonio-message-broker")
						.withToken(token);

		return MessageBrokerClient.fromConfig(
						"127.78.0.7",
						20005,
						serviceDiscoverHttpClient.getConfig("default/username")
								.getOrElse("carbonio-message-broker"),
						serviceDiscoverHttpClient.getConfig("default/password").getOrElse("")
				)
				.withCurrentService(Service.MAILBOX);
	}
}
