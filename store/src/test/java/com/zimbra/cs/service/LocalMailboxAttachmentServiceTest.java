// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service;

import static com.zimbra.common.mime.MimeConstants.CT_MULTIPART_MIXED;
import static org.apache.http.entity.mime.MIME.CONTENT_TYPE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.service.MailboxManagerFactory;
import com.zextras.mailbox.service.OperationContextFactory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.ZimbraAuthTokenEncoded;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import io.vavr.control.Try;
import java.util.UUID;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocalMailboxAttachmentServiceTest {

  /**
   * Creates a mocked message with an attachment for testing purposes
   *
   * @return a message
   * @throws Exception
   */
  private Message createMockMessage(MimeBodyPart attachment) throws Exception {
    final Message message = mock(Message.class);
    final MimeMessage mimeMessage = new MimeMessage((Session) null);
    Multipart multiPart = new MimeMultipart();
    multiPart.addBodyPart(attachment);
    mimeMessage.setContent(multiPart);
    mimeMessage.setSubject("Hello!");
    mimeMessage.setHeader(CONTENT_TYPE, CT_MULTIPART_MIXED);
    when(message.getMimeMessage()).thenReturn(mimeMessage);
    return message;
  }

  @Test
  void shouldReturnAttachment() throws Exception {

    final MimeBodyPart expectedAttachment = new MimeBodyPart();
    expectedAttachment.attachFile(this.getClass().getResource("attachment.txt").getFile());

    final Message message = this.createMockMessage(expectedAttachment);

    final String accountId = UUID.randomUUID().toString();
    final AuthToken authToken = new ZimbraAuthTokenEncoded("test");
    final int messageId = 123;
    final String part = "1";

    final Mailbox mailbox = mock(Mailbox.class);
    final MailboxManager mailboxManager = mock(MailboxManager.class);
    final DummyMailboxManagerFactory dummyMailboxManagerFactory =
        new DummyMailboxManagerFactory(mailboxManager);

    final OperationContext operationContext = mock(OperationContext.class);
    final DummyOperationContextFactory dummyOperationContextFactory =
        new DummyOperationContextFactory(operationContext);

    final LocalMailboxAttachmentService localMailboxAttachmentService =
        new LocalMailboxAttachmentService(dummyOperationContextFactory, dummyMailboxManagerFactory);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(mailbox);
    when(mailbox.getMessageById(operationContext, messageId)).thenReturn(message);

    final Try<MimePart> attachment =
        localMailboxAttachmentService.getAttachment(accountId, authToken, messageId, part);
    Assertions.assertEquals(expectedAttachment, attachment.get());
  }

  class DummyMailboxManagerFactory extends MailboxManagerFactory {
    private final MailboxManager mailboxManager;

    @Override
    public MailboxManager getInstance() {
      return mailboxManager;
    }

    public DummyMailboxManagerFactory(MailboxManager mailboxManager) {
      this.mailboxManager = mailboxManager;
    }
  }

  class DummyOperationContextFactory extends OperationContextFactory {

    public DummyOperationContextFactory(OperationContext operationContext) {
      this.operationContext = operationContext;
    }

    private final OperationContext operationContext;

    @Override
    public OperationContext getOpContext(AuthToken authToken) throws ServiceException {
      return operationContext;
    }
  }
}
