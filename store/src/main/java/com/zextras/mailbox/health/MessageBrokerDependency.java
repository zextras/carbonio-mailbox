// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.health;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.config.enums.Service;
import com.zextras.mailbox.client.ServiceDiscoverHttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Class represents Message broker dependency of mailbox */
public class MessageBrokerDependency extends ServiceDependency {

  public MessageBrokerDependency() {
    super("Message Broker", ServiceType.OPTIONAL);
  }

  @Override
  public boolean isReady() {
    return canConnectToMessageBroker();
  }

  @Override
  public boolean isLive() {
    return this.canConnectToMessageBroker();
  }

  private boolean canConnectToMessageBroker() {
    Path filePath = Paths.get("/etc/carbonio/mailbox/service-discover/token");
    String token;
    try {
      token = Files.readString(filePath);
      ServiceDiscoverHttpClient serviceDiscoverHttpClient =
          ServiceDiscoverHttpClient.defaultURL("carbonio-message-broker")
              .withToken(token);

      MessageBrokerClient messageBrokerClient = MessageBrokerClient.fromConfig(
              "127.78.0.7",
              20005,
              serviceDiscoverHttpClient.getConfig("default/username").get(),
              serviceDiscoverHttpClient.getConfig("default/password").get()
          )
          .withCurrentService(Service.MAILBOX);

      return messageBrokerClient.healthCheck();
    } catch (IOException e) {
      return false;
    }
  }
}
