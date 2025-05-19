// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only
package com.zextras.mailbox.messagebroker;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.config.enums.Service;
import com.zextras.mailbox.client.ServiceDiscoverHttpClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MessageBrokerFactory {
  private static final String SERVICE_NAME = "carbonio-message-broker";

  private static MessageBrokerClient instance;

  private MessageBrokerFactory() {
  }

  public static MessageBrokerClient getMessageBrokerClientInstance() throws CreateMessageBrokerException {
    if (instance != null) {
      return instance;
    }
    synchronized (MessageBrokerFactory.class) {
      if (instance == null) {
        instance = createMessageBrokerClientInstance();
      }
    }
    return instance;
  }

  // Returns a working and healthy MessageBrokerClient instance or throws an exception
  private static MessageBrokerClient createMessageBrokerClientInstance() throws CreateMessageBrokerException {
    try {
      String token = getToken();
      var client = createMessageBrokerClientInstance(token);
      if (!client.healthCheck()) {
        throw new CreateMessageBrokerException("Message broker healthcheck failed");
      }
      return client;
    } catch (IOException e) {
      throw new CreateMessageBrokerException(e);
    }
  }

  private static String getToken() throws IOException {
    return Files.readString(Paths.get("/etc/carbonio/mailbox/service-discover/token"));
  }

  private static MessageBrokerClient createMessageBrokerClientInstance(String token) {
    ServiceDiscoverHttpClient serviceDiscoverHttpClient = ServiceDiscoverHttpClient.defaultUrl().withToken(token);
    return MessageBrokerClient.fromConfig(
                    "127.78.0.7",
                    20005,
                    serviceDiscoverHttpClient.getConfig(SERVICE_NAME, "default/username")
                            .getOrElse("carbonio-message-broker"),
                    serviceDiscoverHttpClient.getConfig(SERVICE_NAME, "default/password")
                            .getOrElse("")
            )
            .withCurrentService(Service.MAILBOX);
  }
}
