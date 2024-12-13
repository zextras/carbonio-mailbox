// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only
package com.zextras.mailbox.messageBroker;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.config.enums.Service;
import com.zextras.mailbox.client.ServiceDiscoverHttpClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MessageBrokerFactory {
	private static final String SERVICE_NAME = "carbonio-message-broker";

	private MessageBrokerFactory() {
	};

	// Returns a working and healthy MessageBrokerClient instance or throws an exception
	public static MessageBrokerClient getMessageBrokerClientInstance()
			throws CreateMessageBrokerException {
		Path filePath = Paths.get("/etc/carbonio/mailbox/service-discover/token");
		String token;
		try {
			token = Files.readString(filePath);
			ServiceDiscoverHttpClient serviceDiscoverHttpClient =
					ServiceDiscoverHttpClient.defaultUrl()
							.withToken(token);

			MessageBrokerClient client =
					MessageBrokerClient.fromConfig(
							"127.78.0.7",
							20005,
							serviceDiscoverHttpClient.getConfig(SERVICE_NAME,"default/username")
									.getOrElse("carbonio-message-broker"),
							serviceDiscoverHttpClient.getConfig(SERVICE_NAME,"default/password")
									.getOrElse("")
					)
					.withCurrentService(Service.MAILBOX);

			if (!client.healthCheck()) throw new RuntimeException("Message broker healthcheck failed");

			return client;
		} catch (Exception e) {
			throw new CreateMessageBrokerException(e);
		}
	}
}
