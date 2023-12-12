package com.zextras.mailbox.health;

public class DependencyHealthResult {
  private final String name;
  private final ServiceDependency.ServiceType type;
  private final boolean ready;
  private final boolean live;

  public DependencyHealthResult(
      String name, ServiceDependency.ServiceType type, boolean ready, boolean live) {
    this.name = name;
    this.type = type;
    this.ready = ready;
    this.live = live;
  }

  public String getName() {
    return name;
  }

  public ServiceDependency.ServiceType getType() {
    return type;
  }

  public boolean isReady() {
    return ready;
  }

  public boolean isLive() {
    return live;
  }
}
