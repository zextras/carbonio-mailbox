// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.testcontainers;

import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class DockerMariaDBResource implements MariaDBResource {

  private static final int PORT = 3306;
  private final GenericContainer<?> container;

  public DockerMariaDBResource(String image) {
    container =
        new GenericContainer<>(DockerImageName.parse(image))
            .withEnv("MARIADB_ROOT_PASSWORD", ROOT_PASSWORD)
            .withExposedPorts(PORT)
            .waitingFor(
                Wait.forLogMessage(".*ready for connections.*", 2)
                    .withStartupTimeout(Duration.ofMinutes(2)));
    container.start();
  }

  @Override
  public String getHost() {
    return container.getHost();
  }

  @Override
  public int getPort() {
    return container.getMappedPort(PORT);
  }

  @Override
  public void close() {
    container.stop();
  }
}
