// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Bug: CO-690
 */
public class MailSenderIT extends MailboxTestSuite {

 @Test
 void shouldSaveSentEmailInDelegatedAccountWithReadStatus() throws Exception {

  // create delegated account
  Account delegatedAccount = createAccount().create();
  Map<String, Object> delegatedAttrs = new HashMap<>();
  delegatedAttrs.put(Provisioning.A_zimbraPrefAllowAddressForDelegatedSender, delegatedAccount.getName());
  delegatedAccount.modify(delegatedAttrs);

  // create user account
  Account userAccount = createAccount().create();
  Map<String, Object> userAttrs = new HashMap<>();
  userAttrs.put(Provisioning.A_zimbraPrefAllowAddressForDelegatedSender, userAccount.getName());
  userAccount.modify(userAttrs);

  final Mailbox delegatedMailbox = MailboxManager.getInstance().getMailboxByAccount(delegatedAccount);

  final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));

  // spy MailSender to avoid sending real email
  final MailSender mailSender = spy(MailSender.class);
  final String userEmail = userAccount.getName();
  final String delegatedEmail = delegatedAccount.getName();
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
  assertEquals(0, delegatedSent.getUnreadCount());
  assertEquals(1, delegatedSent.getSize());
  // Check email is in user sent and read
  final Folder userSent = userMailbox.getFolderByPath(operationContext, "sent");
  assertEquals(0, userSent.getUnreadCount());
  assertEquals(0, userSent.getSize());
 }

}
