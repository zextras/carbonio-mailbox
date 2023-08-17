package com.zimbra.cs.util.proxyconfgen;

/**
 * Represents attributes for a domain configuration.
 *
 * @author jiankuan
 * @author Keshav Bhatt
 */
class DomainAttrItem {

  private final String domainName;
  private final String virtualHostname;
  private final String virtualIPAddress;
  private String clientCertMode;
  private String clientCertCa;
  private String[] rspHeaders;
  private String cspHeader;
  private String webUiLoginUrl;
  private String webUiLogoutUrl;
  private String adminUiLoginUrl;
  private String adminUiLogoutUrl;
  private String sslCertificate;
  private String sslPrivateKey;

  protected DomainAttrItem(String domainName, String virtualHostname, String virtualIPAddress) {
    this.domainName = domainName;
    this.virtualHostname = virtualHostname;
    this.virtualIPAddress = virtualIPAddress;
  }

  public static DomainNameStep builder() {
    return new Builder();
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

  public String getSslCertificate() {
    return sslCertificate;
  }

  public void setSslCertificate(String certificate) {
    this.sslCertificate = certificate;
  }

  public String getSslPrivateKey() {
    return sslPrivateKey;
  }

  public void setSslPrivateKey(String privateKey) {
    this.sslPrivateKey = privateKey;
  }

  interface DomainNameStep {

    VirtualHostnameStep withDomainName(String domainName);
  }

  interface VirtualHostnameStep {

    VirtualIPAddressStep withVirtualHostname(String virtualHostname);
  }

  interface VirtualIPAddressStep {

    FinalStep withVirtualIPAddress(String virtualIPAddress);
  }

  interface FinalStep {

    DomainAttrItem build();

    FinalStep withClientCertMode(String clientCertMode);

    FinalStep withClientCertCa(String clientCertCa);

    FinalStep withRspHeaders(String[] rspHeaders);

    FinalStep withCspHeader(String cspHeader);

    FinalStep withWebUiLoginUrl(String webUiLoginUrl);

    FinalStep withWebUiLogoutUrl(String webUiLogoutUrl);

    FinalStep withAdminUiLoginUrl(String adminUiLoginUrl);

    FinalStep withAdminUiLogoutUrl(String adminUiLogoutUrl);

    FinalStep withSslCertificate(String sslCertificate);

    FinalStep withSslPrivateKey(String sslPrivateKey);
  }

  private static final class Builder
      implements DomainNameStep, VirtualHostnameStep, VirtualIPAddressStep, FinalStep {

    private String domainName;
    private String virtualHostname;
    private String virtualIPAddress;
    private String clientCertMode;
    private String clientCertCa;
    private String[] rspHeaders;
    private String cspHeader;
    private String webUiLoginUrl;
    private String webUiLogoutUrl;
    private String adminUiLoginUrl;
    private String adminUiLogoutUrl;
    private String sslCertificate;
    private String sslPrivateKey;

    public VirtualHostnameStep withDomainName(String domainName) {
      this.domainName = domainName;
      return this;
    }

    public VirtualIPAddressStep withVirtualHostname(String virtualHostname) {
      this.virtualHostname = virtualHostname;
      return this;
    }

    public FinalStep withVirtualIPAddress(String virtualIPAddress) {
      this.virtualIPAddress = virtualIPAddress;
      return this;
    }

    public FinalStep withClientCertMode(String clientCertMode) {
      this.clientCertMode = clientCertMode;
      return this;
    }

    public FinalStep withClientCertCa(String clientCertCa) {
      this.clientCertCa = clientCertCa;
      return this;
    }

    public FinalStep withRspHeaders(String[] rspHeaders) {
      this.rspHeaders = rspHeaders;
      return this;
    }

    public FinalStep withCspHeader(String cspHeader) {
      this.cspHeader = cspHeader;
      return this;
    }

    public FinalStep withWebUiLoginUrl(String webUiLoginUrl) {
      this.webUiLoginUrl = webUiLoginUrl;
      return this;
    }

    public FinalStep withWebUiLogoutUrl(String webUiLogoutUrl) {
      this.webUiLogoutUrl = webUiLogoutUrl;
      return this;
    }

    public FinalStep withAdminUiLoginUrl(String adminUiLoginUrl) {
      this.adminUiLoginUrl = adminUiLoginUrl;
      return this;
    }

    public FinalStep withAdminUiLogoutUrl(String adminUiLogoutUrl) {
      this.adminUiLogoutUrl = adminUiLogoutUrl;
      return this;
    }

    public FinalStep withSslCertificate(String sslCertificate) {
      this.sslCertificate = sslCertificate;
      return this;
    }

    public FinalStep withSslPrivateKey(String sslPrivateKey) {
      this.sslPrivateKey = sslPrivateKey;
      return this;
    }

    public DomainAttrItem build() {
      DomainAttrItem domainAttrItem =
          new DomainAttrItem(domainName, virtualHostname, virtualIPAddress);
      domainAttrItem.clientCertMode = clientCertMode;
      domainAttrItem.clientCertCa = clientCertCa;
      domainAttrItem.rspHeaders = rspHeaders;
      domainAttrItem.cspHeader = cspHeader;
      domainAttrItem.webUiLoginUrl = webUiLoginUrl;
      domainAttrItem.webUiLogoutUrl = webUiLogoutUrl;
      domainAttrItem.adminUiLoginUrl = adminUiLoginUrl;
      domainAttrItem.adminUiLogoutUrl = adminUiLogoutUrl;
      domainAttrItem.sslCertificate = sslCertificate;
      domainAttrItem.sslPrivateKey = sslPrivateKey;
      return domainAttrItem;
    }
  }
}
