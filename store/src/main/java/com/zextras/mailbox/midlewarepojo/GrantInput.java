package com.zextras.mailbox.midlewarepojo;

public class GrantInput {
  private String zid;
  private String gtype;
  private String rights;
  private String grantExpiry;
  private String accessKey;
  private String secretArgs;
  private String password;

  public GrantInput(
      final String zid,
      final String gtype,
      final String rights,
      final String grantExpiry,
      final String accessKey,
      final String secretArgs,
      final String password) {
    this.zid = zid;
    this.gtype = gtype;
    this.rights = rights;
    this.grantExpiry = grantExpiry;
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

  public String getGtype() {
    return gtype;
  }

  public void setGtype(final String gtype) {
    this.gtype = gtype;
  }

  public String getRights() {
    return rights;
  }

  public void setRights(final String rights) {
    this.rights = rights;
  }

  public String getGrantExpiry() {
    return grantExpiry;
  }

  public void setGrantExpiry(final String grantExpiry) {
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
}
