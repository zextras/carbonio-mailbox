package com.zextras.mailbox.midlewarepojo;

/**
 * DTO class to transfer data from WEB layer to service layer.
 *
 * @author Yuliya Aheeva, Davide Polonio, Dima Dymkovets
 * @since 23.10.0
 */
public class GrantInput {
  private String zid;
  private byte granteeType;
  private short rights;
  private long grantExpiry;
  private String display;
  private String accessKey;
  private String secretArgs;
  private String password;

  public GrantInput(
      final String zid,
      final byte granteeType,
      final short rights,
      final long grantExpiry,
      final String display,
      final String accessKey,
      final String secretArgs,
      final String password) {
    this.zid = zid;
    this.granteeType = granteeType;
    this.rights = rights;
    this.grantExpiry = grantExpiry;
    this.display = display;
    this.accessKey = accessKey;
    this.secretArgs = secretArgs;
    this.password = password;
  }

  public String getZid() {
    return zid;
  }

  public void setZid(final String zid) {
    this.zid = zid;
  }

  public byte getGranteeType() {
    return granteeType;
  }

  public void setGranteeType(final byte granteeType) {
    this.granteeType = granteeType;
  }

  public short getRights() {
    return rights;
  }

  public void setRights(final short rights) {
    this.rights = rights;
  }

  public long getGrantExpiry() {
    return grantExpiry;
  }

  public void setGrantExpiry(final long grantExpiry) {
    this.grantExpiry = grantExpiry;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(final String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretArgs() {
    return secretArgs;
  }

  public void setSecretArgs(final String secretArgs) {
    this.secretArgs = secretArgs;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public String getDisplay() {
    return display;
  }

  public void setDisplay(final String display) {
    this.display = display;
  }

  /**
   * Builder class to build {@link GrantInput}.
   *
   * @author Dima Dymkovets
   * @since 23.10.0
   */
  public static final class Builder {
    private String zid;
    private byte granteeType;
    private short rights;
    private long grantExpiry;
    private String display;
    private String accessKey;
    private String secretArgs;
    private String password;

    public Builder setZimbraId(final String zid) {
      this.zid = zid;
      return this;
    }

    public Builder setGranteeType(final byte granteeType) {
      this.granteeType = granteeType;
      return this;
    }

    public Builder setRights(final short rights) {
      this.rights = rights;
      return this;
    }

    public Builder setGrantExpiry(final long grantExpiry) {
      this.grantExpiry = grantExpiry;
      return this;
    }

    public Builder setDisplayName(final String display) {
      this.display = display;
      return this;
    }

    public Builder setAccessKey(final String accessKey) {
      this.accessKey = accessKey;
      return this;
    }

    public Builder setSecretArgs(final String secretArgs) {
      this.secretArgs = secretArgs;
      return this;
    }

    public Builder setPassword(final String password) {
      this.password = password;
      return this;
    }

    public GrantInput build() {
      return new GrantInput(
          zid, granteeType, rights, grantExpiry, display, accessKey, secretArgs, password);
    }
  }
}
