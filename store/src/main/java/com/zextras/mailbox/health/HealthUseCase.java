// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.health;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class HealthUseCase {

  private final List<ServiceDependency> serviceDependencies;

  @Inject
  public HealthUseCase(List<ServiceDependency> serviceDependencies) {
    this.serviceDependencies = serviceDependencies;
  }

  public boolean isReady() {
    return serviceDependencies.stream().allMatch(ServiceDependency::isReady);
  }

  public List<DependencyHealthResult> dependenciesHealthSummary() {
    return serviceDependencies.stream()
        .map(x -> createHealthResult(x))
        .collect(Collectors.toList());
  }

  public boolean isLive() {
    return serviceDependencies.stream().allMatch(ServiceDependency::isLive);
  }

  private DependencyHealthResult createHealthResult(ServiceDependency x) {
    return new DependencyHealthResult(x.getName(), x.getType(), x.isReady(), x.isLive());
  }
}
