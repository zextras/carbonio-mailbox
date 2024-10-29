// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only
package com.zextras.mailbox.messageBroker;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.config.enums.Service;
import com.zextras.mailbox.client.ServiceDiscoverHttpClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MessageBrokerFactory {
	private MessageBrokerFactory() {
	};

	public static MessageBrokerClient getMessageBrokerClientInstance()
			throws CreateMessageBrokerException {
		Path filePath = Paths.get("/etc/carbonio/mailbox/service-discover/token");
		String token;
		try {
			token = Files.readString(filePath);
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
		} catch (IOException e) {
			throw new CreateMessageBrokerException(e);
		}
	}
}
