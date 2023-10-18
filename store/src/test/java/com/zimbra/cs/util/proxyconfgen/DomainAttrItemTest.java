package com.zimbra.cs.util.proxyconfgen;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DomainAttrItemTest {

  @Test
  void builderShouldCorrectlyBuildDomainAttrItem() {
    final String[] rspHeadersArray = {"rspHeaders1", "rspHeaders2"};

    final DomainAttrItem domainAttrItem =
        DomainAttrItem.builder()
            .withDomainName("test.com")
            .withVirtualHostname("mail.test.com")
            .withVirtualIPAddress("192.168.1.1")
            .withSslCertificate("cert")
            .withSslPrivateKey("privateKey")
            .withClientCertMode("on")
            .withClientCertCa("certCA")
            .withRspHeaders(rspHeadersArray)
            .withCspHeader("cspRspHeader")
            .withWebUiLoginUrl("login.test.com")
            .withWebUiLogoutUrl("login.test.com")
            .withAdminUiLoginUrl("admin-login.test.com")
            .withAdminUiLogoutUrl("admin-login.test.com")
            .build();

    assertEquals("test.com", domainAttrItem.getDomainName());
    assertEquals("mail.test.com", domainAttrItem.getVirtualHostname());
    assertEquals("192.168.1.1", domainAttrItem.getVirtualIPAddress());
    assertEquals("cert", domainAttrItem.getSslCertificate());
    assertEquals("privateKey", domainAttrItem.getSslPrivateKey());
    assertEquals("on", domainAttrItem.getClientCertMode());
    assertEquals("certCA", domainAttrItem.getClientCertCa());
    assertArrayEquals(rspHeadersArray, domainAttrItem.getRspHeaders());
    assertEquals("cspRspHeader", domainAttrItem.getCspHeader());
    assertEquals("login.test.com", domainAttrItem.getWebUiLoginUrl());
    assertEquals("login.test.com", domainAttrItem.getWebUiLogoutUrl());
    assertEquals("admin-login.test.com", domainAttrItem.getAdminUiLoginUrl());
    assertEquals("admin-login.test.com", domainAttrItem.getAdminUiLogoutUrl());
  }
}
