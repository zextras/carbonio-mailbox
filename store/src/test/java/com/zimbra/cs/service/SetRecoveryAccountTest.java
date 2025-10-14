// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning.FeatureResetPasswordStatus;
import com.zimbra.common.account.ZAttrProvisioning.PrefPasswordRecoveryAddressStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.service.mail.DirectInsertionMailboxManager;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.cs.service.mail.SetRecoveryAccount;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.SetRecoveryAccountRequest;
import com.zimbra.soap.type.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


public class SetRecoveryAccountTest extends MailboxTestSuite {

  private Account testAccount4797;
  private Account recoveryAccount;
  private Account testAccount5035;

  @BeforeEach
  public void setUp() throws Exception {
    clearData();
    initData();

    MailboxManager.setInstance(new DirectInsertionMailboxManager());

    testAccount4797 =
        createAccount()
            .withUsername("test4797")
            .withDomain(DEFAULT_DOMAIN_NAME)
            .withPassword("secret")
            .withAttribute(
                Provisioning.A_zimbraFeatureResetPasswordStatus,
                FeatureResetPasswordStatus.enabled.toString())
            .create();

    recoveryAccount =
        createAccount()
            .withUsername("testRecovery")
            .withDomain(DEFAULT_DOMAIN_NAME)
            .withPassword("secret")
            .create();

    testAccount5035 =
        createAccount()
            .withUsername("test5035")
            .withDomain(DEFAULT_DOMAIN_NAME)
            .withPassword("secret")
            .withAttribute(
                Provisioning.A_zimbraFeatureResetPasswordStatus,
                FeatureResetPasswordStatus.enabled.toString())
            .create();
  }

  @Test
  void testMissingChannel() throws Exception {
    testAccount5035.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
    SetRecoveryAccountRequest request = new SetRecoveryAccountRequest();
    request.setOp(SetRecoveryAccountRequest.Op.sendCode);
    request.setRecoveryAccount(recoveryAccount.getName());
    Element req = JaxbUtil.jaxbToElement(request);
    try {
      new SetRecoveryAccount().handle(req, ServiceTestUtil.getRequestContext(testAccount5035));
    } catch (ServiceException e) {
      fail("Exception should not be thrown\n" + e.getMessage());
    }
  }

  @Test
  void test4797() throws Exception {
    Mailbox recoveryMailbox = MailboxManager.getInstance().getMailboxByAccount(recoveryAccount);
    testAccount4797.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
    assertNull(testAccount4797.getPrefPasswordRecoveryAddress());
    assertNull(testAccount4797.getPrefPasswordRecoveryAddressStatus());

    SetRecoveryAccountRequest request = new SetRecoveryAccountRequest();
    request.setOp(SetRecoveryAccountRequest.Op.sendCode);
    request.setRecoveryAccount(recoveryAccount.getName());
    request.setChannel(Channel.EMAIL);
    Element req = JaxbUtil.jaxbToElement(request);

    new SetRecoveryAccount().handle(req, ServiceTestUtil.getRequestContext(testAccount4797));

    // Refresh account to get updated attributes
    testAccount4797 = Provisioning.getInstance().getAccountById(testAccount4797.getId());

    // Verify that the recovery email address is updated into ldap and
    // status is set to pending
    assertEquals(recoveryAccount.getName(), testAccount4797.getPrefPasswordRecoveryAddress());
    assertEquals(
        PrefPasswordRecoveryAddressStatus.pending.toString(),
        testAccount4797.getAttrs().get(Provisioning.A_zimbraPrefPasswordRecoveryAddressStatus));

    // Verify that recovery email address received the verification email
    Message msg = (Message) recoveryMailbox.getItemList(null, MailItem.Type.MESSAGE).get(0);
    assertEquals(
        "Request for recovery email address verification by " + testAccount4797.getName(),
        msg.getSubject());

    // Test that sending code again throws exception
    ServiceException exception =
        assertThrows(
            ServiceException.class,
            () -> {
              new SetRecoveryAccount()
                  .handle(req, ServiceTestUtil.getRequestContext(testAccount4797));
            });
    assertEquals(
        "service exception: Verification code already sent to this recovery email.",
        exception.getMessage());
  }

  @Test
  void test5035() throws Exception {
    testAccount5035.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
    SetRecoveryAccountRequest request = new SetRecoveryAccountRequest();
    request.setOp(SetRecoveryAccountRequest.Op.sendCode);
    request.setRecoveryAccount(testAccount5035.getName()); // Same as primary account
    request.setChannel(Channel.EMAIL);
    Element req = JaxbUtil.jaxbToElement(request);

    ServiceException exception =
        assertThrows(
            ServiceException.class,
            () -> {
              new SetRecoveryAccount()
                  .handle(req, ServiceTestUtil.getRequestContext(testAccount5035));
            });
    assertEquals(
        "service exception: Recovery address should not be same as primary/alias email address.",
        exception.getMessage());
  }
}
