package com.zimbra.cs.service;

import static io.vavr.API.Case;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.Mime;
import io.vavr.API.Match.Pattern0;
import io.vavr.control.Try;
import javax.mail.internet.MimePart;

/**
 * Mailbox attachment provider
 *
 * @author davidefrison
 * @since 4.0.7
 */
public class MailboxAttachmentService implements AttachmentService {

  //TODO: try to move {AttachmentService and related classes and exceptions outside)
  public static class MessageNotFound extends Exception {

    private static final long serialVersionUID = 703424514631475550L;

    public MessageNotFound(Throwable cause) {
      super("Message not found.", cause);
    }
  }

  public static class AttachmentNotFound extends Exception {

    private static final long serialVersionUID = 1138735883340739827L;

    public AttachmentNotFound(String part) {
      super(String.format("Missing part %s.", part));
    }
  }

  public Try<MimePart> getAttachment(
      String accountId, AuthToken token, int messageId, String part) {
    return Try.of(() -> MailboxManager.getInstance().getMailboxByAccountId(accountId))
        .mapTry(mailbox ->
            mailbox.getMessageById(new OperationContext(token), messageId)
        )
        .mapFailure(
            Case(
                Pattern0.of(Exception.class),
                MessageNotFound::new
            )
        )
        .flatMap(message ->
            Try.of(() -> Mime.getMimePart(message.getMimeMessage(), part))
                .mapFailure(
                    Case(
                        Pattern0.of(Exception.class),
                        ex -> ServiceException.PARSE_ERROR("Error parsing mime message", ex)
                    )
                )
                .flatMap(mimePart ->
                    mimePart == null ?
                        Try.failure(new AttachmentNotFound(part))
                        : Try.success(mimePart)
                )
        );
  }
}
