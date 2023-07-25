package com.zimbra.cs.service.servlet.preauth;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.cs.account.Provisioning;
import java.util.Map;

class AutoProvisioningParams {
  private final String accountIdentifier;
  private final AccountBy accountBy;
  private final Provisioning provisioning;
  private final boolean isAdmin;
  private final long timestamp;
  private final long expires;
  private final String preAuth;
  private final Map<String, Object> authContext;

  private AutoProvisioningParams(AutoProvisioningParamsBuilder builder) {
    this.accountIdentifier = builder.accountIdentifier;
    this.accountBy = builder.accountBy;
    this.provisioning = builder.provisioning;
    this.isAdmin = builder.isAdmin;
    this.timestamp = builder.timestamp;
    this.expires = builder.expires;
    this.preAuth = builder.preAuth;
    this.authContext = builder.authContext;
  }

  public String getAccountIdentifier() {
    return accountIdentifier;
  }

  public AccountBy getAccountBy() {
    return accountBy;
  }

  public Provisioning getProvisioning() {
    return provisioning;
  }

  public boolean isAdmin() {
    return isAdmin;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public long getExpires() {
    return expires;
  }

  public String getPreAuth() {
    return preAuth;
  }

  public Map<String, Object> getAuthContext() {
    return authContext;
  }

  static class AutoProvisioningParamsBuilder {

    private final String accountIdentifier;
    private final AccountBy accountBy;
    private final Provisioning provisioning;
    private final boolean isAdmin;
    private final long timestamp;
    private final long expires;
    private final String preAuth;
    private Map<String, Object> authContext;

    AutoProvisioningParamsBuilder(
        String accountIdentifier,
        AccountBy accountBy,
        Provisioning provisioning,
        boolean isAdmin,
        long timestamp,
        long expires,
        String preAuth) {
      this.accountIdentifier = accountIdentifier;
      this.accountBy = accountBy;
      this.provisioning = provisioning;
      this.isAdmin = isAdmin;
      this.timestamp = timestamp;
      this.expires = expires;
      this.preAuth = preAuth;
    }

    public AutoProvisioningParamsBuilder authContext(Map<String, Object> authContext) {
      this.authContext = authContext;
      return this;
    }

    public AutoProvisioningParams build() {
      return new AutoProvisioningParams(this);
    }
  }
}
