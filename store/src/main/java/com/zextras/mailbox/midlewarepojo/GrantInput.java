package com.zextras.mailbox.midlewarepojo;

/** */
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
}
