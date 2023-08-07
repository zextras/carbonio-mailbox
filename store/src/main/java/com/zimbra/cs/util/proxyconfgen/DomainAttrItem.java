package com.zimbra.cs.util.proxyconfgen;

/**
 * A simple class of Triple<VirtualHostName, VirtualIPAddress, DomainName>. Uses this only for
 * convenient and HashMap can't guarantee order
 *
 * @author jiankuan
 */
class DomainAttrItem {

  private final String domainName;
  private final String virtualHostname;
  private final String virtualIPAddress;
  private String sslCertificate;
  private String sslPrivateKey;
  private final String clientCertMode;
  private final String clientCertCa;
  private final String[] rspHeaders;
  private final String cspHeader;
  private final String webUiLoginUrl;
  private final String webUiLogoutUrl;
  private final String adminUiLoginUrl;
  private final String adminUiLogoutUrl;

  DomainAttrItem(Builder builder) {
    this.domainName = builder.domainName;
    this.virtualHostname = builder.virtualHostname;
    this.virtualIPAddress = builder.virtualIPAddress;
    this.sslCertificate = builder.sslCertificate;
    this.sslPrivateKey = builder.sslPrivateKey;
    this.clientCertMode = builder.clientCertMode;
    this.clientCertCa = builder.clientCertCa;
    this.rspHeaders = builder.rspHeaders;
    this.cspHeader = builder.cspHeader;
    this.webUiLoginUrl = builder.webUiLoginUrl;
    this.webUiLogoutUrl = builder.webUiLogoutUrl;
    this.adminUiLoginUrl = builder.adminUiLoginUrl;
    this.adminUiLogoutUrl = builder.adminUiLogoutUrl;
  }

  public String getDomainName() {
    return domainName;
  }

  public String getVirtualHostname() {
    return virtualHostname;
  }

  public String getVirtualIPAddress() {
    return virtualIPAddress;
  }

  public String getSslCertificate() {
    return sslCertificate;
  }

  public void setSslCertificate(String sslCertificate) {
    this.sslCertificate = sslCertificate;
  }

  public String getSslPrivateKey() {
    return sslPrivateKey;
  }

  public void setSslPrivateKey(String sslPrivateKey) {
    this.sslPrivateKey = sslPrivateKey;
  }

  public String getClientCertMode() {
    return clientCertMode;
  }

  public String getClientCertCa() {
    return clientCertCa;
  }

  public String[] getRspHeaders() {
    return rspHeaders;
  }

  public String getCspHeader() {
    return cspHeader;
  }

  public String getWebUiLoginUrl() {
    return webUiLoginUrl;
  }

  public String getWebUiLogoutUrl() {
    return webUiLogoutUrl;
  }

  public String getAdminUiLoginUrl() {
    return adminUiLoginUrl;
  }

  public String getAdminUiLogoutUrl() {
    return adminUiLogoutUrl;
  }

  public static class Builder {

    private final String domainName;
    private final String virtualHostname;
    private final String virtualIPAddress;
    private String sslCertificate;
    private String sslPrivateKey;
    private String clientCertMode;
    private String clientCertCa;
    private String[] rspHeaders;
    private String cspHeader;
    private String webUiLoginUrl;
    private String webUiLogoutUrl;
    private String adminUiLoginUrl;
    private String adminUiLogoutUrl;

    public Builder(String domainName, String virtualHostname, String virtualIPAddress) {
      this.domainName = domainName;
      this.virtualHostname = virtualHostname;
      this.virtualIPAddress = virtualIPAddress;
    }

    public Builder withSslCertificate(String sslCertificate) {
      this.sslCertificate = sslCertificate;
      return this;
    }

    public Builder withSslPrivateKey(String sslPrivateKey) {
      this.sslPrivateKey = sslPrivateKey;
      return this;
    }

    public Builder withClientCertMode(String clientCertMode) {
      this.clientCertMode = clientCertMode;
      return this;
    }

    public Builder withClientCertCa(String clientCertCa) {
      this.clientCertCa = clientCertCa;
      return this;
    }

    public Builder withRspHeaders(String[] rspHeaders) {
      this.rspHeaders = rspHeaders;
      return this;
    }

    public Builder withCspHeader(String cspHeader) {
      this.cspHeader = cspHeader;
      return this;
    }

    public Builder withWebUiLoginUrl(String webUiLoginUrl) {
      this.webUiLoginUrl = webUiLoginUrl;
      return this;
    }

    public Builder withWebUiLogoutUrl(String webUiLogoutUrl) {
      this.webUiLogoutUrl = webUiLogoutUrl;
      return this;
    }

    public Builder withAdminUiLoginUrl(String adminUiLoginUrl) {
      this.adminUiLoginUrl = adminUiLoginUrl;
      return this;
    }

    public Builder withAdminUiLogoutUrl(String adminUiLogoutUrl) {
      this.adminUiLogoutUrl = adminUiLogoutUrl;
      return this;
    }

    public DomainAttrItem build() {
      return new DomainAttrItem(this);
    }
  }
}
