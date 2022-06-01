package com.zimbra.cs.util.proxyconfgen;

/**
 * A simple class of Triple<VirtualHostName, VirtualIPAddress, DomainName>. Uses this only for
 * convenient and HashMap can't guarantee order
 *
 * @author jiankuan
 */
class DomainAttrItem {

  public String domainName;
  public String virtualHostname;
  public String virtualIPAddress;
  public String sslCertificate;
  public String sslPrivateKey;
  public Boolean useDomainServerCert;
  public Boolean useDomainClientCert;
  public String clientCertMode;
  public String clientCertCa;
  public String[] rspHeaders;

  public DomainAttrItem(
      String dn,
      String vhn,
      String vip,
      String scrt,
      String spk,
      String ccm,
      String cca,
      String[] rhdr) {
    this.domainName = dn;
    this.virtualHostname = vhn;
    this.virtualIPAddress = vip;
    this.sslCertificate = scrt;
    this.sslPrivateKey = spk;
    this.clientCertMode = ccm;
    this.clientCertCa = cca;
    this.rspHeaders = rhdr;
  }
}
