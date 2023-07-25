// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Maps;
import com.zimbra.common.account.ZAttrProvisioning.FeatureResetPasswordStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.MailboxData;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.account.ResetPassword;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.ResetPasswordRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class ResetPasswordTest {

    static final String USER_NAME = "test4802@zimbra.com";
    static final String PASSWORD = "old_secret";
    static final String NEW_PASSWORD = "new_secret";

    
    public String testName;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();

        Map<String, Object> attrs = Maps.newHashMap();

        prov.createDomain("zimbra.com", attrs);

        attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        attrs.put(Provisioning.A_mail, USER_NAME);
        prov.createAccount(USER_NAME, PASSWORD, attrs);

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
 void testResetPassword() throws Exception {
  Provisioning prov = Provisioning.getInstance();
  Account acct1 = prov.getAccount(USER_NAME);
  acct1.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
  ResetPassword resetPassword = new TestResetPassword();
  Map<String, Object> ctxt = ServiceTestUtil.getRequestContext(acct1);

  ResetPasswordRequest resetReq = new ResetPasswordRequest();
  resetReq.setPassword(NEW_PASSWORD);
  Element request = JaxbUtil.jaxbToElement(resetReq);
  try {
   resetPassword.handle(request, ctxt);
  } catch (ServiceException se) {
   fail("This should not happen");
  }
 }

 @Test
 void testResetPassword_FeatureDisabled() throws Exception {
  Provisioning prov = Provisioning.getInstance();
  Account acct1 = prov.getAccount(USER_NAME);
  acct1.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
  ResetPassword resetPassword = new TestResetPassword();
  Map<String, Object> ctxt = ServiceTestUtil.getRequestContext(acct1);

  ResetPasswordRequest resetReq = new ResetPasswordRequest();
  resetReq.setPassword(NEW_PASSWORD);
  Element request = JaxbUtil.jaxbToElement(resetReq);
  try {
   resetPassword.handle(request, ctxt);
  } catch (ServiceException se) {
   assertEquals("permission denied: Reset password feature is disabled", se.getMessage());
  }
 }
}

class TestResetPassword extends ResetPassword {
    @Override
    protected void setPasswordAndPurgeAuthTokens(Provisioning prov, Account acct, String newPassword, boolean dryRun)
            throws ServiceException {
        // do nothing
    }

    @Override
    protected void checkPasswordStrength(Provisioning prov, Account acct, String newPassword) throws ServiceException {
       // do nothing
    }
}
