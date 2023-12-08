// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.health;

import java.util.function.Supplier;

/**
 * Abstract class representing service as dependency of another service.
 */
public abstract class ServiceDependency implements HealthStatus {

  private final String name;
  private final ServiceType type;

  private final int pollingIntervalMillis;
  private final Supplier<Long> currentTimeProvider;
  private Long lastCheckTimestamp;
  private boolean lastHealthStatus;


  protected ServiceDependency(String name,
      ServiceType type, int pollingIntervalMillis,
      Supplier<Long> currentTimeProvider) {
    this.name = name;
    this.type = type;

    this.pollingIntervalMillis = pollingIntervalMillis;
    this.currentTimeProvider = currentTimeProvider;
    this.lastHealthStatus = false;
  }

  public String getName() {
    return name;
  }

  public ServiceType getType() {
    return type;
  }

  protected abstract boolean doCheckStatus();

  protected boolean canConnectToService() {
    final long currentTime = currentTimeProvider.get();

    if (lastCheckTimestamp == null || currentTime > lastCheckTimestamp + pollingIntervalMillis) {
      lastHealthStatus = doCheckStatus();
      lastCheckTimestamp = currentTime;
    }

    return lastHealthStatus;
  }

  public enum ServiceType {
    OPTIONAL,
    REQUIRED
  }
}


