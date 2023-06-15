// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning.FeatureResetPasswordStatus;
import com.zimbra.common.account.ZAttrProvisioning.PrefPasswordRecoveryAddressStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.MailboxData;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.mail.RecoverAccount;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.cs.service.util.ResetPasswordUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.RecoverAccountRequest;
import com.zimbra.soap.mail.message.RecoverAccountResponse;
import com.zimbra.soap.mail.type.RecoverAccountOperation;
import com.zimbra.soap.type.Channel;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class RecoverAccountTest {

    public static String zimbraServerDir = "";

    
    public String testName;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();

        Map<String, Object> attrs = Maps.newHashMap();

        prov.createDomain("zimbra.com", attrs);

        attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        prov.createAccount("testRecovery@zimbra.com", "secret", attrs);

        attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        attrs.put(Provisioning.A_zimbraPrefPasswordRecoveryAddress, "testRecovery@zimbra.com");
        attrs.put(Provisioning.A_zimbraPrefPasswordRecoveryAddressStatus, "verified");
        prov.createAccount("test4798@zimbra.com", "secret", attrs);
        attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        attrs.put(Provisioning.A_zimbraPrefPasswordRecoveryAddress, "testRecovery@zimbra.com");
        attrs.put(Provisioning.A_zimbraPrefPasswordRecoveryAddressStatus, "verified");
        prov.createAccount("test4798a@zimbra.com", "secret", attrs);

        MailboxManager.setInstance(new DirectInsertionMailboxManager());

        L10nUtil.setMsgClassLoader("../store-conf/conf/msgs");
    }

    public static class DirectInsertionMailboxManager extends MailboxManager {

        public DirectInsertionMailboxManager() throws ServiceException {
            super();
        }

        @Override
        protected Mailbox instantiateMailbox(MailboxData data) {
            return new Mailbox(data) {

                @Override
                public MailSender getMailSender() {
                    return new MailSender() {

                        @Override
                        protected Collection<Address> sendMessage(Mailbox mbox, MimeMessage mm,
                            Collection<RollbackData> rollbacks) {
                            List<Address> successes = new ArrayList<Address>();
                            Address[] addresses;
                            try {
                                addresses = getRecipients(mm);
                            } catch (Exception e) {
                                addresses = new Address[0];
                            }
                            DeliveryOptions dopt = new DeliveryOptions()
                                .setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
                            for (Address addr : addresses) {
                                try {
                                    Account acct = Provisioning.getInstance()
                                        .getAccountByName(((InternetAddress) addr).getAddress());
                                    if (acct != null) {
                                        Mailbox target = MailboxManager.getInstance()
                                            .getMailboxByAccount(acct);
                                        target.addMessage(null, new ParsedMessage(mm, false), dopt,
                                            null);
                                        successes.add(addr);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace(System.out);
                                }
                            }
                            if (successes.isEmpty() && !isSendPartial()) {
                                for (RollbackData rdata : rollbacks) {
                                    if (rdata != null) {
                                        rdata.rollback();
                                    }
                                }
                            }
                            return successes;
                        }
                    };
                }
            };
        }
    }

 @BeforeEach
 public void setUp(TestInfo testInfo) throws Exception {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
  System.out.println( testName);
  MailboxTestUtil.clearData();
 }

    @AfterAll
    public static void tearDown() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void testGetRecoveryEmail() throws Exception {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "test4798@zimbra.com");
  acct1.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
  assertEquals("testRecovery@zimbra.com", acct1.getPrefPasswordRecoveryAddress());
  assertEquals(PrefPasswordRecoveryAddressStatus.verified, acct1.getPrefPasswordRecoveryAddressStatus());
  RecoverAccountRequest request = new RecoverAccountRequest();
  request.setOp(RecoverAccountOperation.GET_RECOVERY_ACCOUNT);
  request.setEmail("test4798@zimbra.com");
  request.setChannel(Channel.EMAIL);
  Element req = JaxbUtil.jaxbToElement(request);
  Element response = new RecoverAccount().handle(req, ServiceTestUtil.getRequestContext(acct1));
  RecoverAccountResponse resp = JaxbUtil.elementToJaxb(response);
  assertEquals(StringUtil.maskEmail("testRecovery@zimbra.com"), resp.getRecoveryAccount());
 }

 @Test
 @Disabled("Fix me. Assertions fails. Exception message is different.")
 void testGetRecoveryEmail_Negative() throws Exception {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "test4798@zimbra.com");
  acct1.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
  acct1.setPrefPasswordRecoveryAddressStatus(PrefPasswordRecoveryAddressStatus.pending);
  assertEquals("testRecovery@zimbra.com", acct1.getPrefPasswordRecoveryAddress());
  RecoverAccountRequest request = new RecoverAccountRequest();
  request.setOp(RecoverAccountOperation.GET_RECOVERY_ACCOUNT);
  request.setEmail("test4798@zimbra.com");
  request.setChannel(Channel.EMAIL);
  Element req = JaxbUtil.jaxbToElement(request);
  try {
   new RecoverAccount().handle(req, ServiceTestUtil.getRequestContext(acct1));
  } catch (ServiceException se) {
   assertEquals("service exception: Something went wrong. Please contact your administrator.", se.getMessage());
  }
 }

 @Test
 void testSendRecoveryCode() throws Exception {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "test4798@zimbra.com");
  acct1.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
  acct1.setPrefPasswordRecoveryAddressStatus(PrefPasswordRecoveryAddressStatus.verified);
  assertEquals("testRecovery@zimbra.com", acct1.getPrefPasswordRecoveryAddress());
  RecoverAccountRequest request = new RecoverAccountRequest();
  request.setOp(RecoverAccountOperation.SEND_RECOVERY_CODE);
  request.setEmail("test4798@zimbra.com");
  request.setChannel(Channel.EMAIL);
  Element req = JaxbUtil.jaxbToElement(request);
  try {
   new RecoverAccount().handle(req, ServiceTestUtil.getRequestContext(acct1));
  } catch (ServiceException se) {
   fail("ServiceException should not be thrown.");
  }
  Account recoveryAccount = Provisioning.getInstance().get(Key.AccountBy.name, "testRecovery@zimbra.com");
  Mailbox recoveryMailbox = MailboxManager.getInstance().getMailboxByAccount(recoveryAccount);
  Message msg = (Message) recoveryMailbox.getItemList(null, MailItem.Type.MESSAGE).get(0);
  assertEquals("Reset your zimbra.com password", msg.getSubject());
 }

 @Test
 void testResendRecoveryCodePositiveAndNegativeScenarios() throws Exception {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "test4798a@zimbra.com");
  acct1.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
  acct1.setPrefPasswordRecoveryAddressStatus(PrefPasswordRecoveryAddressStatus.verified);
  acct1.setPasswordRecoveryMaxAttempts(3);
  acct1.setFeatureResetPasswordSuspensionTime("10s");
  assertEquals("testRecovery@zimbra.com", acct1.getPrefPasswordRecoveryAddress());
  RecoverAccountRequest request = new RecoverAccountRequest();
  request.setOp(RecoverAccountOperation.SEND_RECOVERY_CODE);
  request.setEmail("test4798a@zimbra.com");
  request.setChannel(Channel.EMAIL);
  Element req = JaxbUtil.jaxbToElement(request);
  try {
   new RecoverAccount().handle(req, ServiceTestUtil.getRequestContext(acct1)); // resend count = 0
  } catch (ServiceException se) {
   fail("ServiceException should not be thrown.");
  }

  try {
   new RecoverAccount().handle(req, ServiceTestUtil.getRequestContext(acct1));// resend count = 1
   new RecoverAccount().handle(req, ServiceTestUtil.getRequestContext(acct1));// resend count = 2
   new RecoverAccount().handle(req, ServiceTestUtil.getRequestContext(acct1));// resend count = 3
  } catch (ServiceException se) {
   fail("ServiceException should not be thrown.");
  }
  try {
   new RecoverAccount().handle(req, ServiceTestUtil.getRequestContext(acct1));// resend count = 4, and it should fail
   fail("ServiceException should be thrown.");
  } catch (ServiceException se) {
   assertEquals("service exception: Max re-send attempts reached, feature is suspended.", se.getMessage());
  }

  try {
   ResetPasswordUtil.validateFeatureResetPasswordStatus(acct1);
   fail("ServiceException should be thrown.");
  } catch (ServiceException e) {
   assertEquals("service exception: Password reset feature is suspended.", e.getMessage());
  }

  Thread.sleep(10000);
  try {
   ResetPasswordUtil.validateFeatureResetPasswordStatus(acct1);
  } catch (ServiceException e) {
   fail("ServiceException should not be thrown as suspension time is over.");
  }
 }

 @Test
 void testResetPasswordUtilFeatureDisabled() throws ServiceException {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "test4798a@zimbra.com");
  acct1.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.disabled);
  try {
   ResetPasswordUtil.validateFeatureResetPasswordStatus(acct1);
   fail("ServiceException should be thrown.");
  } catch (ServiceException e) {
   assertEquals("service exception: Password reset feature is disabled.", e.getMessage());
  }
 }

 @Test
 void testRecoverAccount_MissingChannel() throws Exception {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "test4798@zimbra.com");
  acct1.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
  acct1.setPrefPasswordRecoveryAddressStatus(PrefPasswordRecoveryAddressStatus.verified);
  assertEquals("testRecovery@zimbra.com", acct1.getPrefPasswordRecoveryAddress());
  RecoverAccountRequest request = new RecoverAccountRequest();
  request.setOp(RecoverAccountOperation.GET_RECOVERY_ACCOUNT);
  request.setEmail("test4798@zimbra.com");
  Element req = JaxbUtil.jaxbToElement(request);
  try {
   new RecoverAccount().handle(req, ServiceTestUtil.getRequestContext(acct1));// resend count = 3
  } catch (ServiceException se) {
   fail("Exception should not be thrown");
  }
 }
}
