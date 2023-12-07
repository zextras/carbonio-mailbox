// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.health;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class HealthService {

  private final List<HealthCheck> healthChecks;

  @Inject
  public HealthService(List<HealthCheck> healthChecks) {
    this.healthChecks = healthChecks;
  }

  public boolean isReady() {
    return healthChecks.stream().map(HealthCheck::isReady).reduce(
        true, (totalReadiness, isReady) -> totalReadiness && isReady
    );
  }

  public List<Object> dependencies() {
    return new ArrayList<>();
  }

}
