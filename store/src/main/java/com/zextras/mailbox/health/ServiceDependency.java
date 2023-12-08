// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.health;

public abstract class ServiceDependency implements HealthStatus {

  private final String name;
  private final ServiceType type;


  protected ServiceDependency(String name,
      ServiceType type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public ServiceType getType() {
    return type;
  }

  public enum ServiceType {
    OPTIONAL,
    REQUIRED
  }
}


