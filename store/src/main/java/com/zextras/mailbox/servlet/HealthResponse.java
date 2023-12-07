// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zextras.mailbox.health.ServiceDependency;
import com.zextras.mailbox.health.ServiceDependency.ServiceType;
import java.util.ArrayList;
import java.util.List;

public class HealthResponse {

  public HealthResponse(boolean ready,
      List<Dependency> dependencies) {
    this.ready = ready;
    this.dependencies = dependencies;
  }

  static class Builder {
    private boolean ready = false;
    private final List<Dependency> dependencies = new ArrayList<>();

    Builder withReadiness(boolean isReady) {
      this.ready = isReady;
      return this;
    }

    Builder withDependency(Dependency dependency) {
      this.dependencies.add(dependency);
      return this;
    }

    Builder withDependency(ServiceDependency serviceDependency) {
      final Dependency dependency = new Dependency(serviceDependency.getName(),
          serviceDependency.getType(), serviceDependency.isReady(), serviceDependency.isLive());
      this.dependencies.add(dependency);
      return this;
    }

    public HealthResponse build() {
      return new HealthResponse(this.ready, this.dependencies);
    }

  }

  @JsonProperty("ready")
  private final boolean ready;

  @JsonProperty("dependencies")
  private final List<Dependency> dependencies;

  public static class Dependency {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("type")
    private final ServiceType type;

    @JsonProperty("ready")
    private final boolean ready;

    @JsonProperty("live")
    private final boolean live;

    public Dependency(String name, ServiceType type, boolean ready, boolean live) {
      this.name = name;
      this.type = type;
      this.ready = ready;
      this.live = live;
    }
  }


}
