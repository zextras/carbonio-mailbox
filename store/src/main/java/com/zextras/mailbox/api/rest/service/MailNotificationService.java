/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zextras.mailbox.api.rest.resource.dto.SendNotificationRequest;
import com.zimbra.common.mime.shim.JavaMailInternetAddress;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.zmime.ZMimeMessage;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.util.JMSession;
import io.vavr.control.Try;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class MailNotificationService {

  private static final String CONTENT_TYPE_HTML_UTF8 = "text/html; charset=UTF-8";
  private static final String CHARSET_UTF8 = "UTF-8";

  private final Supplier<Provisioning> provisioningSupplier;
  private final Supplier<MailboxManager> mailboxManagerSupplier;
  private final Supplier<SoapProvisioning> soapProvisioningSupplier;

  public MailNotificationService(Supplier<Provisioning> provisioningSupplier,
                                 Supplier<MailboxManager> mailboxManagerSupplier,
                                 Supplier<SoapProvisioning> soapProvisioningSupplier) {
    this.provisioningSupplier = provisioningSupplier;
    this.mailboxManagerSupplier = mailboxManagerSupplier;
    this.soapProvisioningSupplier = soapProvisioningSupplier;
  }

  public Try<Integer> send(SendNotificationRequest request) {
    return Try.of(() -> {
      final List<String> recipients = recipientsOf(request);
      for (String accountId : recipients) {
        deliverTo(accountId, request.subject(), request.body());
      }
      return recipients.size();
    });
  }

  private void deliverTo(String accountId, String subject, String body) {
    try {
      final Account account = provisioningSupplier.get().getAccountById(accountId);
      if (account == null) {
        ZimbraLog.mailbox.warn("Notification skipped: account not found id=%s", accountId);
        return;
      }
      if (account.isOnLocalServer()) {
        deliverLocally(account, subject, body);
      } else {
        deliverRemotely(account, subject, body);
      }
    } catch (Exception e) {
      ZimbraLog.mailbox.warn("Notification delivery failed for accountId=%s", accountId, e);
    }
  }

  private void deliverLocally(Account account, String subject, String body)
      throws ServiceException, MessagingException, IOException {
    final MimeMessage message = buildMessage(account, subject, body);
    final ParsedMessage parsed = new ParsedMessage(message, System.currentTimeMillis(), false);
    final DeliveryOptions options = new DeliveryOptions()
        .setFolderId(Mailbox.ID_FOLDER_INBOX)
        .setFlags(Flag.BITMASK_UNREAD | Flag.BITMASK_HIGH_PRIORITY);
    mailboxManagerSupplier.get().getMailboxByAccount(account).addMessage(null, parsed, options, null);
  }

  private void deliverRemotely(Account account, String subject, String body) {
    ZimbraLog.mailbox.warn("Notification skipped: remote delivery not yet implemented for accountId=%s",
        account.getId());
  }

  private static MimeMessage buildMessage(Account account, String subject, String body)
      throws MessagingException {
    final MimeMessage message = new ZMimeMessage(JMSession.getSession());
    message.setFrom(new JavaMailInternetAddress("postmaster@" + account.getDomainName()));
    message.setRecipient(RecipientType.TO, new JavaMailInternetAddress(account.getName()));
    message.setSubject(subject, CHARSET_UTF8);
    message.setSentDate(new Date());
    message.setContent(body, CONTENT_TYPE_HTML_UTF8);
    return message;
  }

  private static List<String> recipientsOf(SendNotificationRequest request) {
    final List<String> recipients = request.recipients();
    return recipients == null ? List.of() : recipients;
  }
}
