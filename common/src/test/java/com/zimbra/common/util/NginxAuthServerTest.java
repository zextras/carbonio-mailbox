// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.zimbra.common.util.ngxlookup.NginxAuthServer;

public class NginxAuthServerTest {

  @Test
  void testIpV4() {
    NginxAuthServer server = new NginxAuthServer("10.11.12.13", "8080", "user1");
    assertNotNull(server);
    assertNotNull(server.getNginxAuthServer());
    assertEquals("10.11.12.13:8080", server.getNginxAuthServer());
  }

  @Test
  void testIpV6() {
    NginxAuthServer server = new NginxAuthServer("2a02:1800:1b3:3:0:0:f00:576", "443", "user1");
    assertNotNull(server);
    assertNotNull(server.getNginxAuthServer());
    assertEquals("[2a02:1800:1b3:3:0:0:f00:576]:443", server.getNginxAuthServer());
  }
}
