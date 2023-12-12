// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zextras.mailbox.health.DependencyHealthResult;
import com.zextras.mailbox.health.ServiceDependency;
import com.zextras.mailbox.health.ServiceDependency.ServiceType;
import java.util.ArrayList;
import java.util.List;

/** Represents response body of {@link HealthServlet} */
public class HealthResponse {

  @SuppressWarnings("unused")
  @JsonProperty("ready")
  private final boolean ready;

  @SuppressWarnings("unused")
  @JsonProperty("dependencies")
  private final List<Dependency> dependencies;

  public HealthResponse(boolean ready, List<Dependency> dependencies) {
    this.ready = ready;
    this.dependencies = dependencies;
  }

  /**
   * Fluent Builder for {@link HealthResponse} class
   *
   * <p>Usage:
   *
   * <pre>{@code
   * HealthResponse response = new HealthResponseBuilder.newInstance().
   * ...
   * ...
   * .build();
   * }</pre>
   */
  static class HealthResponseBuilder {

    private final List<Dependency> dependencies = new ArrayList<>();
    private boolean ready = false;

    private HealthResponseBuilder() {
      // to enforce the use of the static factory method newInstance
    }

    public static HealthResponseBuilder newInstance() {
      return new HealthResponseBuilder();
    }

    public HealthResponse build() {
      return new HealthResponse(this.ready, this.dependencies);
    }

    public HealthResponseBuilder withReadiness(boolean isReady) {
      this.ready = isReady;
      return this;
    }

    public HealthResponseBuilder withDependencies(List<DependencyHealthResult> dependenciesHealth) {
      dependencies.clear();
      dependenciesHealth.stream()
          .map(x -> new Dependency(x.getName(), x.getType(), x.isReady(), x.isLive()))
          .forEach(dependencies::add);
      return this;
    }
  }

  /** Mapper class for {@link ServiceDependency} implementations */
  @SuppressWarnings("unused")
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
