package com.zimbra.cs.util.proxyconfgen;

/**
 * A simple class of Triple<VirtualHostName, VirtualIPAddress, DomainName>. Uses this only for
 * convenient and HashMap can't guarantee order
 *
 * @author jiankuan
 */
class DomainAttrItem {

  String domainName;
  String virtualHostname;
  String virtualIPAddress;
  String sslCertificate;
  String sslPrivateKey;
  String clientCertMode;
  String clientCertCa;
  String[] rspHeaders;

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
