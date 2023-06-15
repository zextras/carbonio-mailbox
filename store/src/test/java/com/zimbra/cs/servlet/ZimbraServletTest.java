// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.common.util.RemoteIP.TrustedIPs;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.MockHttpServletRequest;
import com.zimbra.cs.service.MockHttpServletResponse;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ZimbraServletTest {

  private static String uri = "/Briefcase/上的发生的发";

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    MailboxTestUtil.setMockitoProvisioning();
  }

 @Disabled("until bug 60345 is fixed")
 @Test
 void proxyTest() throws Exception {
  MockHttpServletRequest req =
    new MockHttpServletRequest(
      "test".getBytes("UTF-8"), new URL("http://localhost:7070/user1" + uri), "");
  MockHttpServletResponse resp = new MockHttpServletResponse();
  ZimbraServlet.proxyServletRequest(
    req, resp, Provisioning.getInstance().getLocalServer(), uri, null);
 }

 @Test
 void shouldTrustProxyIps() throws Exception {
  String[] proxyIps = new String[]{"192.168.0.10", "192.168.0.15"};
  String[] trustedIpsByAttr = new String[]{"192.168.0.1", "192.168.0.2", "192.168.0.3"};
  Server localServer = Mockito.mock(Server.class);
  Mockito.when(localServer.getMultiAttr(Provisioning.A_zimbraMailTrustedIP))
    .thenReturn(trustedIpsByAttr);
  // Mock proxy servers ips
  Server proxyServer1 = Mockito.mock(Server.class);
  Mockito.when(proxyServer1.getIPAddress()).thenReturn(proxyIps[0]);
  Server proxyServer2 = Mockito.mock(Server.class);
  Mockito.when(proxyServer2.getIPAddress()).thenReturn(proxyIps[1]);
  List<Server> proxyServers =
    new ArrayList<>() {
     {
      add(proxyServer1);
      add(proxyServer2);
     }
    };
  Provisioning mockProvisioning = Provisioning.getInstance();
  // mock provisioning to return mock local server + mock proxy servers
  Mockito.when(mockProvisioning.getLocalServer()).thenReturn(localServer);
  Mockito.when(mockProvisioning.getAllServers(Provisioning.SERVICE_PROXY))
    .thenReturn(proxyServers);
  TrustedIPs trustedIPs = ZimbraServlet.getTrustedIPs();
  Arrays.stream(proxyIps).forEach(ip -> assertTrue(trustedIPs.isIpTrusted(ip)));
 }
}
