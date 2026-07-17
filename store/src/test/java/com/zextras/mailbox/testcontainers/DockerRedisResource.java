// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.testcontainers;

import com.redis.testcontainers.RedisContainer;
import org.testcontainers.utility.DockerImageName;

/** Docker-backed {@link RedisResource} using testcontainers. Used for local runs. */
public class DockerRedisResource implements RedisResource {

  private final RedisContainer container;

  public DockerRedisResource(String image) {
    container = new RedisContainer(DockerImageName.parse(image));
    container.start();
  }

  @Override
  public String getHost() {
    return container.getRedisHost();
  }

  @Override
  public int getPort() {
    return container.getRedisPort();
  }

  @Override
  public void close() {
    container.stop();
  }
}
