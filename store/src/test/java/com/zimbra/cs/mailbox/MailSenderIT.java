// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.mailbox;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Bug: CO-690
 */
public class MailSenderIT {

  private static Provisioning prov;

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    prov = Provisioning.getInstance();
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  public void shouldSaveSentEmailInDelegatedAccountWithReadStatus() throws Exception {

    String delegatedId = UUID.randomUUID().toString();
    String delegatedEmail = delegatedId + "@test.com";

    String userId = UUID.randomUUID().toString();
    String userEmail = userId + "@test.com";
    // create delegated account
    Map<String, Object> delegatedAttrs = new HashMap<>();
    delegatedAttrs.put(Provisioning.A_zimbraPrefAllowAddressForDelegatedSender, userEmail);
    delegatedAttrs.put(Provisioning.A_zimbraId, delegatedId);
    Account delegatedAccount = prov.createAccount(delegatedEmail, "secret", delegatedAttrs);

    // create user account
    Map<String, Object> userAttrs = new HashMap<>();
    userAttrs.put(Provisioning.A_zimbraPrefAllowAddressForDelegatedSender, userEmail);
    userAttrs.put(Provisioning.A_zimbraId, userId);
    Account userAccount = prov.createAccount(userEmail, "secret", userAttrs);

    final Mailbox delegatedMailbox = MailboxManager.getInstance().getMailboxByAccount(delegatedAccount);

    final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));

    // spy MailSender to avoid sending real email
    final MailSender mailSender = spy(MailSender.class);
    Address[] recipients = new Address[]{new InternetAddress(userEmail)};
    doReturn(List.of(recipients)).when(mailSender).sendMessage(Mockito.any(Mailbox.class), Mockito.any(MimeMessage.class), Mockito.any(
        java.util.Collection.class));
    final OperationContext operationContext = new OperationContext(userAccount);
    mimeMessage.setFrom(new InternetAddress(delegatedEmail));
    mimeMessage.setRecipients(RecipientType.TO, recipients);
    mimeMessage.setSubject("Test email");
    mimeMessage.setText("Hello there!");
    mimeMessage.setSender(new InternetAddress(userEmail));
    // send email logged in as user, using delegated account
    mailSender.sendMimeMessage(operationContext, delegatedMailbox, mimeMessage);

    final Mailbox userMailbox = MailboxManager.getInstance().getMailboxByAccount(userAccount);
    final OperationContext delegatedAccountOpCtx = new OperationContext(delegatedAccount);
    // Check email is in delegated sent and read
    final Folder delegatedSent = delegatedMailbox.getFolderByPath(delegatedAccountOpCtx, "sent");
    Assert.assertEquals(0, delegatedSent.getUnreadCount());
    Assert.assertEquals(1, delegatedSent.getSize());
    // Check email is in user sent and read
    final Folder userSent = userMailbox.getFolderByPath(operationContext, "sent");
    Assert.assertEquals(0, userSent.getUnreadCount());
    Assert.assertEquals(0, userSent.getSize());
  }

}
