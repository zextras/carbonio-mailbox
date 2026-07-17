// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.testcontainers;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension providing a shared Redis instance for the whole JVM. It selects a Kubernetes
 * pod when running inside a cluster ({@link ContainerRuntime#isKubernetes()}) and a Docker
 * container (testcontainers) otherwise.
 *
 * <p>Register it as a static field so tests keep calling {@code getHost()/getPort()}:
 *
 * <pre>{@code
 * @RegisterExtension
 * static final RedisExtension redis = new RedisExtension();
 * }</pre>
 *
 * <p>The instance is a JVM-wide singleton so it starts once and is torn down on JVM shutdown.
 */
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
