package com.zimbra.cs.service;

import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.Mime;
import io.vavr.control.Try;
import javax.mail.internet.MimePart;

/**
 * Mailbox attachment provider
 *
 * @author davidefrison
 * @since 4.0.7
 */
public class MailboxAttachmentService implements AttachmentService {

  public Try<MimePart> getAttachment(
      String accountId, AuthToken token, int messageId, String part) {
    return Try.of(() -> MailboxManager.getInstance().getMailboxByAccountId(accountId))
        .mapTry(mailbox -> mailbox.getMessageById(new OperationContext(token), messageId))
        .mapTry(message -> Mime.getMimePart(message.getMimeMessage(), part));
  }
}
