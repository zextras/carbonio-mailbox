// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning.FeatureAddressVerificationStatus;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExternalUserProvServletTest extends MailboxTestSuite {


  @BeforeEach
  public void setUp() throws Exception {
    clearData();
    initData();
  }

  @Test
  void testHandleAddressVerificationExpired() throws Exception {
    Provisioning.getInstance().createDomain("zimbra.com", new HashMap<>());
    Account acct1 =
        createAccount()
            .withDomain("zimbra.com")
            .withUsername("test")
            .withPassword("secret")
            .create();
    HashMap<String, String> headers = new HashMap<>();
    HttpServletRequest req =
        new MockHttpServletRequest(null, null, null, 123, "127.0.0.1", headers);
    MockHttpServletResponse resp = new MockHttpServletResponse();

    ExternalUserProvServlet servlet = new ExternalUserProvServlet();
    servlet.handleAddressVerification(req, resp, acct1.getId(), "test2@zimbra.com", true);
    assertNull(acct1.getPrefMailForwardingAddress());
    assertEquals(
        FeatureAddressVerificationStatus.expired, acct1.getFeatureAddressVerificationStatus());
  }

  @Test
  void testHandleAddressVerificationSuccess() throws Exception {
    Provisioning.getInstance().createDomain("zimbra.com", new HashMap<>());
    Account acct1 =
        createAccount()
            .withDomain("zimbra.com")
            .withUsername("test")
            .withPassword("secret")
            .create();
    HashMap<String, String> headers = new HashMap<String, String>();
    HttpServletRequest req =
        new MockHttpServletRequest(null, null, null, 123, "127.0.0.1", headers);
    MockHttpServletResponse resp = new MockHttpServletResponse();
    ExternalUserProvServlet servlet = new ExternalUserProvServlet();
    servlet.handleAddressVerification(req, resp, acct1.getId(), "test2@zimbra.com", false);
    assertEquals("test2@zimbra.com", acct1.getPrefMailForwardingAddress());
    assertEquals(
        FeatureAddressVerificationStatus.verified, acct1.getFeatureAddressVerificationStatus());
  }
}
