// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Maps;
import com.zimbra.common.account.ZAttrProvisioning.FeatureAddressVerificationStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.MailboxData;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.account.ModifyPrefs;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.ModifyPrefsRequest;
import com.zimbra.soap.account.type.Pref;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ModifyPrefsTest {

  public static final String TEST_DOMAIN = "test.com";

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> attrs = Maps.newHashMap();
    prov.createDomain(TEST_DOMAIN, attrs);

    MailboxManager.setInstance(new MailboxManager() {
      @Override
      protected Mailbox instantiateMailbox(MailboxData data) {
        return new Mailbox(data) {

          @Override
          public MailSender getMailSender() {
            return new MailSender() {

              @Override
              protected Collection<Address> sendMessage(Mailbox mbox, MimeMessage mm,
                  Collection<RollbackData> rollbacks) {
                try {
                  return Arrays.asList(getRecipients(mm));
                } catch (Exception e) {
                  return Collections.emptyList();
                }
              }
            };
          }
        };
      }
    });

    L10nUtil.setMsgClassLoader("../store-conf/conf/msgs");
  }


  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  private Account createGetRandomAccount() throws ServiceException {
    return Provisioning.getInstance()
        .createAccount(UUID.randomUUID() + "@" + TEST_DOMAIN, "secret", Maps.newHashMap());
  }

  @Test
  void testMsgMaxAttr() throws Exception {
    Account account = createGetRandomAccount();
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
    account.setFeatureMailForwardingEnabled(true);
    account.setFeatureAddressVerificationEnabled(true);
    assertNull(account.getPrefMailForwardingAddress());
    assertNull(account.getFeatureAddressUnderVerification());
    ModifyPrefsRequest request = new ModifyPrefsRequest();
    Pref pref = new Pref(Provisioning.A_zimbraPrefMailForwardingAddress,
        "test1@somedomain.com");
    request.addPref(pref);
    Element req = JaxbUtil.jaxbToElement(request);
    new ModifyPrefs().handle(req, ServiceTestUtil.getRequestContext(mbox.getAccount()));
    /*
     * Verify that the forwarding address is not directly stored into
     * 'zimbraPrefMailForwardingAddress' Instead, it is stored in
     * 'zimbraFeatureAddressUnderVerification' till the time it
     * gets verification
     */
    assertNull(account.getPrefMailForwardingAddress());
    assertEquals("test1@somedomain.com",
        account.getFeatureAddressUnderVerification());
    /*
     * disable the verification feature and check that the forwarding
     * address is directly stored into 'zimbraPrefMailForwardingAddress'
     */
    account.setPrefMailForwardingAddress(null);
    account.setFeatureAddressUnderVerification(null);
    account.setFeatureAddressVerificationEnabled(false);
    new ModifyPrefs().handle(req, ServiceTestUtil.getRequestContext(mbox.getAccount()));

    assertNull(account.getFeatureAddressUnderVerification());
    assertEquals("test1@somedomain.com", account.getPrefMailForwardingAddress());
    assertEquals(FeatureAddressVerificationStatus.pending, account.getFeatureAddressVerificationStatus());
  }

  @Test
  void testPrefCalendarInitialViewYear() throws Exception {
    Account account = createGetRandomAccount();
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
    ModifyPrefsRequest request = new ModifyPrefsRequest();

    request.addPref(new Pref(Provisioning.A_zimbraPrefCalendarInitialView, "year"));
    Element req = JaxbUtil.jaxbToElement(request);
    new ModifyPrefs().handle(req, ServiceTestUtil.getRequestContext(mbox.getAccount()));

    assertFalse(account.getPrefCalendarInitialView().isDay());
    assertTrue(account.getPrefCalendarInitialView().isYear());
  }

  @Test
  void shouldModifyPrefWhenAsked() throws Exception {
    Account account = createGetRandomAccount();
    account.setCarbonioPrefWebUiDarkMode(false);
    assertFalse(account.isCarbonioPrefWebUiDarkMode());

    ModifyPrefsRequest request = new ModifyPrefsRequest();
    request.addPref(new Pref(Provisioning.A_carbonioPrefWebUiDarkMode, "TRUE"));
    Element req = JaxbUtil.jaxbToElement(request);
    new ModifyPrefs().handle(req, ServiceTestUtil.getRequestContext(account));

    assertTrue(account.isCarbonioPrefWebUiDarkMode());
  }

  @Test
  void shouldThrowServiceExceptionWhenUnknownAttributeIsPassed() throws Exception {
    Account account = createGetRandomAccount();

    String carbonioPrefUnknownAttribute = "carbonioPrefUnknownAttribute";
    ModifyPrefsRequest request = new ModifyPrefsRequest();
    request.addPref(new Pref(carbonioPrefUnknownAttribute, "google.com"));
    Element req = JaxbUtil.jaxbToElement(request);

    ServiceException serviceException = assertThrows(ServiceException.class,
        () -> new ModifyPrefs().handle(req, ServiceTestUtil.getRequestContext(account)));
    assertEquals(ServiceException.INVALID_REQUEST, serviceException.getCode());
    assertEquals("invalid request: no such attribute: " + carbonioPrefUnknownAttribute
        , serviceException.getMessage());
  }

  @Test
  void shouldThrowServiceExceptionWhenNonPrefAttributeIsPassed() throws Exception {
    Account account = createGetRandomAccount();

    account.setCarbonioPrefWebUiDarkMode(false);
    assertFalse(account.isCarbonioPrefWebUiDarkMode());

    ModifyPrefsRequest request = new ModifyPrefsRequest();
    request.addPref(new Pref("unknownAttribute", "TRUE"));
    Element req = JaxbUtil.jaxbToElement(request);

    ServiceException serviceException = assertThrows(ServiceException.class,
        () -> new ModifyPrefs().handle(req, ServiceTestUtil.getRequestContext(account)));
    assertEquals(ServiceException.INVALID_REQUEST, serviceException.getCode());
    assertEquals("invalid request: pref name must start with zimbraPref or carbonioPref"
        , serviceException.getMessage());
  }
}
