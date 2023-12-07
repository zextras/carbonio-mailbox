// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.health;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class HealthService {


  private final List<ServiceDependency> serviceDependencies;

  @Inject
  public HealthService(List<ServiceDependency> serviceDependencies) {
    this.serviceDependencies = serviceDependencies;
  }

  public boolean isReady() {
    return serviceDependencies.stream().allMatch(ServiceDependency::isReady);
  }

  public List<Object> dependencies() {
    return serviceDependencies.stream().map(ServiceDependency::getName)
        .collect(Collectors.toList());
  }

  public boolean isLive() {
    return serviceDependencies.stream().allMatch(ServiceDependency::isLive);
  }
}
