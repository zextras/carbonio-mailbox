// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.testcontainers;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MariaDBExtension implements BeforeAllCallback {

  private static final String IMAGE = "mariadb:10.4";
  private static volatile MariaDBResource shared;

  public static synchronized MariaDBResource sharedResource() {
    if (shared == null) {
      shared =
          ContainerRuntime.isKubernetes()
              ? new KubernetesMariaDBResource(IMAGE)
              : new DockerMariaDBResource(IMAGE);
      Runtime.getRuntime().addShutdownHook(new Thread(shared::close));
    }
    return shared;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    sharedResource();
  }

  public String jdbcUrl() {
    MariaDBResource resource = sharedResource();
    return "jdbc:mariadb://" + resource.getHost() + ":" + resource.getPort() + "/";
  }
}
