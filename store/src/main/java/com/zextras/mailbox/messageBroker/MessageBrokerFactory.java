// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only
package com.zextras.mailbox.messageBroker;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.config.enums.Service;
import com.zextras.mailbox.client.ServiceDiscoverHttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageBrokerFactory {
  private static final String SERVICE_NAME = "carbonio-message-broker";

  private static final Map<Boolean, MessageBrokerClient> clientMap = new ConcurrentHashMap<>();

  private MessageBrokerFactory() {
  }

  public static MessageBrokerClient getMessageBrokerClientInstance() {
    return clientMap.computeIfAbsent(true, _key -> createMessageBrokerClientInstance());
  }

  // Returns a working and healthy MessageBrokerClient instance or throws an exception
  private static MessageBrokerClient createMessageBrokerClientInstance() {
    try {
      String token = getToken();
      var client = createMessageBrokerClientInstance(token);
      if (!client.healthCheck()) throw new CreateMessageBrokerException("Message broker healthcheck failed");
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
