// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.zextras.mailbox.client.MailboxHttpClient;
import com.zextras.mailbox.client.UserServletRequest;
import com.zimbra.cs.account.AuthToken;
import io.vavr.control.Try;
import java.io.InputStream;
import java.util.function.Function;
import javax.activation.DataHandler;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePart;
import javax.mail.util.ByteArrayDataSource;

/**
 * Class to manage attachments in the mailbox This class is and adapter of {@link MailboxHttpClient}
 */
public class MailboxHttpAttachmentService implements AttachmentService {

  private final MailboxHttpClient mailboxHttpClient;

  public MailboxHttpAttachmentService(MailboxHttpClient mailboxHttpClient) {
    this.mailboxHttpClient = mailboxHttpClient;
  }

  @Override
  public Try<MimePart> getAttachment(
      String accountId, AuthToken token, int messageId, String part) {
    return Try.of(() -> UserServletRequest.buildRequest("co", String.valueOf(messageId), part))
        .mapTry(
            userServletRequest ->
                mailboxHttpClient.callUserServlet(token, accountId, userServletRequest))
        .flatMap(Function.identity())
        .mapTry(
            userServletResponse -> {
              final String contentType = userServletResponse.getContentType();
              final InputStream content = userServletResponse.getContent();
              final MimePart attachment = new MimeBodyPart();
              ByteArrayDataSource bds = new ByteArrayDataSource(content, contentType);
              attachment.setDataHandler(new DataHandler(bds));
              attachment.setFileName(userServletResponse.getFileName());
              attachment.setHeader(CONTENT_TYPE, contentType);
              return attachment;
            });
  }
}
