package com.zimbra.cs.service;

import com.zextras.mailbox.service.MailboxManagerFactory;
import com.zextras.mailbox.service.OperationContextFactory;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mime.Mime;
import io.vavr.control.Try;
import javax.mail.internet.MimePart;

/**
 * Mailbox attachment service. Retrieves attachment using local mailbox instance
 *
 * @author davidefrison
 * @since 4.0.7
 */
public class LocalMailboxAttachmentService implements AttachmentService {

  private final OperationContextFactory operationContextProvider;
  private final MailboxManagerFactory mailboxManagerFactory;

  public LocalMailboxAttachmentService(
      OperationContextFactory operationContextProvider,
      MailboxManagerFactory mailboxManagerFactory) {
    this.operationContextProvider = operationContextProvider;
    this.mailboxManagerFactory = mailboxManagerFactory;
  }

  public Try<MimePart> getAttachment(
      String accountId, AuthToken token, int messageId, String part) {
    return Try.of(() -> mailboxManagerFactory.getInstance().getMailboxByAccountId(accountId))
        .andThenTry(mailbox -> operationContextProvider.getOpContext(token))
        .mapTry(
            mailbox ->
                mailbox.getMessageById(operationContextProvider.getOpContext(token), messageId))
        .mapTry(message -> Mime.getMimePart(message.getMimeMessage(), part));
  }
}
