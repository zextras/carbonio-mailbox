package com.zimbra.cs.service;

import com.zimbra.common.service.ServiceException;
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
        .mapTry( mailbox ->
            mailbox.getMessageById(new OperationContext(token), messageId)
        )
        .recoverWith( ex ->
            Try.failure(ServiceException.NOT_FOUND("File not found.", ex))
        )
        .flatMap(message ->
            Try.of(() -> Mime.getMimePart(message.getMimeMessage(), part))
                .recoverWith( (e) ->
                    Try.failure(ServiceException.PARSE_ERROR("Error parsing mime message", e))
                )
                .flatMap( mimePart ->
                    mimePart == null ? Try.failure(ServiceException.NOT_FOUND(String.format("Missing part %s", part))) : Try.success(mimePart)
                )
        );
  }
}
