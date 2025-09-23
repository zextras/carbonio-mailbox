// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning.FeatureAddressVerificationStatus;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExternalUserProvServletTest extends MailboxTestSuite {

  private Account testAccount;

  @BeforeEach
  public void setUp() throws Exception {
    clearData();
    initData();

    testAccount =
        createAccount()
            .withUsername("testuser")
            .withDomain(DEFAULT_DOMAIN_NAME)
            .withPassword("secret")
            .create();
  }

  @Test
  void testHandleAddressVerificationExpired() throws Exception {
    HashMap<String, String> headers = new HashMap<>();
    HttpServletRequest req =
        new MockHttpServletRequest(null, null, null, 123, "127.0.0.1", headers);
    MockHttpServletResponse resp = new MockHttpServletResponse();

    ExternalUserProvServlet servlet = new ExternalUserProvServlet();

    servlet.handleAddressVerification(
        req, resp, testAccount.getId(), "test2@" + DEFAULT_DOMAIN_NAME, true);

    testAccount = Provisioning.getInstance().getAccountById(testAccount.getId());

    assertNull(testAccount.getPrefMailForwardingAddress());

    assertEquals(
        FeatureAddressVerificationStatus.expired,
        testAccount.getFeatureAddressVerificationStatus());
  }

  @Test
  void testHandleAddressVerificationSuccess() throws Exception {
    HashMap<String, String> headers = new HashMap<>();
    HttpServletRequest req =
        new MockHttpServletRequest(null, null, null, 123, "127.0.0.1", headers);
    MockHttpServletResponse resp = new MockHttpServletResponse();

    ExternalUserProvServlet servlet = new ExternalUserProvServlet();

    String forwardingEmail = "test2@" + DEFAULT_DOMAIN_NAME;

    servlet.handleAddressVerification(req, resp, testAccount.getId(), forwardingEmail, false);

    testAccount = Provisioning.getInstance().getAccountById(testAccount.getId());

    assertEquals(forwardingEmail, testAccount.getPrefMailForwardingAddress());

    assertEquals(
        FeatureAddressVerificationStatus.verified,
        testAccount.getFeatureAddressVerificationStatus());
  }
}
