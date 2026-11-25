// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.testcontainers;

import com.zextras.mailbox.testcontainers.KubernetesPods.LaunchedPod;
import java.util.List;
import java.util.Map;

public class KubernetesMariaDBResource implements MariaDBResource {

  private static final int PORT = 3306;
  private final LaunchedPod pod;

  public KubernetesMariaDBResource(String image) {
    this.pod =
        KubernetesPods.start(
            "mariadb-test",
            image,
            List.of(PORT),
            Map.of("MARIADB_ROOT_PASSWORD", ROOT_PASSWORD),
            List.of(),
            List.of(DATA_DIRECTORY));
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
