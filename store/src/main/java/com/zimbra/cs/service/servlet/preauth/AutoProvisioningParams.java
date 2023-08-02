package com.zimbra.cs.service.servlet.preauth;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.cs.account.Provisioning;
import java.util.Map;

/**
 * This class represents the parameters for auto-provisioning of an account. It encapsulates the
 * necessary information required to auto-provision an account based on the provided input.
 *
 * @author Keshav Bhatt
 * @since 23.9.0
 */
class AutoProvisioningParams {

  private final String accountIdentifier;
  private final AccountBy accountBy;
  private final Provisioning provisioning;
  private final boolean isAdmin;
  private final long timestamp;
  private final long expires;
  private final String preAuth;
  private final Map<String, Object> authContext;

  private AutoProvisioningParams(
      String accountIdentifier,
      AccountBy accountBy,
      Provisioning provisioning,
      boolean isAdmin,
      long timestamp,
      long expires,
      String preAuth,
      Map<String, Object> authContext) {
    this.accountIdentifier = accountIdentifier;
    this.accountBy = accountBy;
    this.provisioning = provisioning;
    this.isAdmin = isAdmin;
    this.timestamp = timestamp;
    this.expires = expires;
    this.preAuth = preAuth;
    this.authContext = authContext;
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

  // Getter methods ...

  static class AutoProvisioningParamsBuilder {

    private String accountIdentifier;
    private AccountBy accountBy;
    private Provisioning provisioning;
    private boolean isAdmin;
    private long timestamp;
    private long expires;
    private String preAuth;
    private Map<String, Object> authContext;

    AutoProvisioningParamsBuilder() {}

    AutoProvisioningParamsBuilder withAccountIdentifier(String accountIdentifier) {
      this.accountIdentifier = accountIdentifier;
      return this;
    }

    AutoProvisioningParamsBuilder withAccountBy(AccountBy accountBy) {
      this.accountBy = accountBy;
      return this;
    }

    AutoProvisioningParamsBuilder withProvisioning(Provisioning provisioning) {
      this.provisioning = provisioning;
      return this;
    }

    AutoProvisioningParamsBuilder withIsAdmin(boolean isAdmin) {
      this.isAdmin = isAdmin;
      return this;
    }

    AutoProvisioningParamsBuilder withTimestamp(Long timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    AutoProvisioningParamsBuilder withExpires(Long expires) {
      this.expires = expires;
      return this;
    }

    AutoProvisioningParamsBuilder withPreAuth(String preAuth) {
      this.preAuth = preAuth;
      return this;
    }

    public AutoProvisioningParamsBuilder withAuthContext(Map<String, Object> authContext) {
      this.authContext = authContext;
      return this;
    }

    public AutoProvisioningParams build() {
      return new AutoProvisioningParams(
          accountIdentifier,
          accountBy,
          provisioning,
          isAdmin,
          timestamp,
          expires,
          preAuth,
          authContext);
    }
  }
}
