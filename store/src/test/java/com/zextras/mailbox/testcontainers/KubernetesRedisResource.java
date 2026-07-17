// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.testcontainers;

import com.zextras.mailbox.testcontainers.KubernetesPods.LaunchedPod;
import java.util.List;
import java.util.Map;

public class KubernetesRedisResource implements RedisResource {

  private static final int PORT = 6379;

  private final LaunchedPod pod;

  public KubernetesRedisResource(String image) {
    this.pod = KubernetesPods.start("redis-test", image, List.of(PORT), Map.of(), List.of());
  }

  @Override
  public String getHost() {
    return pod.ip();
  }

  @Override
  public int getPort() {
    return PORT;
  }

  @Override
  public void close() {
    pod.delete();
  }
}
