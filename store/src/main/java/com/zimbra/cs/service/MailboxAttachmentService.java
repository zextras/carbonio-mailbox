// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service;

import static io.vavr.API.Case;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.API.Match.Pattern0;
import io.vavr.control.Try;
import java.util.Optional;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;

/**
 * Mailbox attachment provider
 */
public class MailboxAttachmentService implements AttachmentService {

  private final OperationContextFactory contextFactory;

  public MailboxAttachmentService(OperationContextFactory contextFactory) {
    this.contextFactory = contextFactory;
  }

  public MailboxAttachmentService() {
    this.contextFactory = new OperationContextFactory();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Try<MimePart> getAttachment(String accountId, AuthToken authToken, int messageId, String part) {

    return Try.of(() -> MailboxManager.getInstance().getMailboxByAccountId(accountId))
        .mapTry(mailbox ->
            mailbox.getMessageById(contextFactory.createOperationContext(authToken), messageId)
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

  public Try<MimePart> getAttachmentByItemId(String accountId, AuthToken authToken, ItemId itemId, String part) {
    return Try.of(() -> MailboxManager.getInstance().getMailboxByAccountId(accountId))
        .flatMap(mailbox -> Try.of(() -> {
          var context = contextFactory.createOperationContext(authToken);
          MailItem mailItem = itemId.hasSubpart() ?
              mailbox.getAppointmentById(context, itemId.getId()) :
              mailbox.getMessageById(context, itemId.getId());

          if (mailItem == null) {
            throw new MessageNotFound(null);
          }
          return mailItem;
        }))
        .flatMap(mailItem -> Try.of(() -> {
          MimePart mimePart;
          if (mailItem instanceof Message message) {
            mimePart = Mime.getMimePart(message.getMimeMessage(), part);
          } else {
            mimePart = extractMimePartFromCalendarItem((CalendarItem) mailItem, itemId, part).getOrNull();
          }
          return Optional.ofNullable(mimePart)
              .orElseThrow(() -> new AttachmentNotFound(part));
        }));
  }

  private Try<MimePart> extractMimePartFromCalendarItem(CalendarItem calItem, ItemId itemId, String part) {
    return Try.of(() -> {
      MimeMessage mimeMessage = itemId.hasSubpart()
          ? calItem.getSubpartMessage(itemId.getSubpartId())
          : calItem.getMimeMessage();

      return Mime.getMimePart(mimeMessage, part);
    });
  }

  public static class MessageNotFound extends Exception {

    public MessageNotFound(Throwable cause) {
      super("Message not found.", cause);
    }
  }

  public static class AttachmentNotFound extends Exception {

    public AttachmentNotFound(String part) {
      super(String.format("Missing part %s.", part));
    }
  }

  public static class OperationContextFactory {

    public OperationContext createOperationContext(AuthToken authToken) throws ServiceException {
      return new OperationContext(authToken);
    }
  }
}
