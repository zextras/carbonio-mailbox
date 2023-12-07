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


