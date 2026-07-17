// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.testcontainers;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RedisExtension implements BeforeAllCallback {

  private static final String IMAGE = "redis:6.2.6";

  private static volatile RedisResource shared;

  public static synchronized RedisResource sharedResource() {
    if (shared == null) {
      shared =
          ContainerRuntime.isKubernetes()
              ? new KubernetesRedisResource(IMAGE)
              : new DockerRedisResource(IMAGE);
      Runtime.getRuntime().addShutdownHook(new Thread(shared::close));
    }
    return shared;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    sharedResource();
  }

  public String getHost() {
    return sharedResource().getHost();
  }

  public int getPort() {
    return sharedResource().getPort();
  }
}
