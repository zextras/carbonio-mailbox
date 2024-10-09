// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mailbox.Appointment;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.service.MailboxAttachmentService.AttachmentNotFound;
import com.zimbra.cs.service.MailboxAttachmentService.MessageNotFound;
import com.zimbra.cs.service.MailboxAttachmentService.OperationContextFactory;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class MailboxAttachmentServiceTest {

  private MailboxAttachmentService mailboxAttachmentService;

  @Mock
  private MailboxManager mailboxManager;
  @Mock
  private Mailbox mailbox;
  @Mock
  private Message message;
  @Mock
  private Appointment appointment;
  @Mock
  private AuthToken authToken;
  @Mock
  private MimeMessage mimeMessage;
  @Mock
  private MimePart mimePart;
  @Mock
  private OperationContextFactory operationContextFactory;
  @Mock
  private OperationContext context;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mailboxAttachmentService = new MailboxAttachmentService(operationContextFactory);
  }

  @Test
  void getAttachment_should_return_success_when_message_and_part_are_found() throws Exception {
    try (MockedStatic<MailboxManager> mockedStatic = mockStatic(MailboxManager.class);
        MockedStatic<Mime> mockedMime = mockStatic(Mime.class)) {

      String accountId = "testAccountId";
      int messageId = 1;
      String part = "1";

      when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(mailbox);
      mockedStatic.when(MailboxManager::getInstance).thenReturn(mailboxManager);

      when(operationContextFactory.createOperationContext(authToken)).thenReturn(context);
      when(mailbox.getMessageById(any(OperationContext.class), eq(messageId))).thenReturn(message);
      when(message.getMimeMessage()).thenReturn(mimeMessage);
      mockedMime.when(() -> Mime.getMimePart(mimeMessage, part)).thenReturn(mimePart);

      Try<MimePart> result = mailboxAttachmentService.getAttachment(accountId, authToken, messageId, part);

      assertTrue(result.isSuccess());
      assertEquals(mimePart, result.get());
    }
  }

  @Test
  void getAttachment_should_return_failure_when_message_is_not_found() throws Exception {
    try (MockedStatic<MailboxManager> mockedStatic = mockStatic(MailboxManager.class)) {

      String accountId = "testAccountId";
      int messageId = 1;
      String part = "1";

      when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(mailbox);
      mockedStatic.when(MailboxManager::getInstance).thenReturn(mailboxManager);

      when(operationContextFactory.createOperationContext(authToken)).thenReturn(context);
      when(mailbox.getMessageById(any(OperationContext.class), eq(messageId)))
          .thenThrow(new RuntimeException());

      Try<MimePart> result = mailboxAttachmentService.getAttachment(accountId, authToken, messageId, part);

      assertTrue(result.isFailure());
      assertTrue(result.getCause() instanceof MessageNotFound);
    }
  }

  @Test
  void getAttachment_should_return_failure_when_mime_part_is_not_found() throws Exception {
    try (MockedStatic<MailboxManager> mockedStatic = mockStatic(MailboxManager.class);
        MockedStatic<Mime> mockedMime = mockStatic(Mime.class)) {

      String accountId = "testAccountId";
      int messageId = 1;
      String part = "1";

      when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(mailbox);
      mockedStatic.when(MailboxManager::getInstance).thenReturn(mailboxManager);

      when(operationContextFactory.createOperationContext(authToken)).thenReturn(context);
      when(mailbox.getMessageById(any(OperationContext.class), eq(messageId))).thenReturn(message);
      when(message.getMimeMessage()).thenReturn(mimeMessage);
      mockedMime.when(() -> Mime.getMimePart(mimeMessage, part)).thenReturn(null);

      Try<MimePart> result = mailboxAttachmentService.getAttachment(accountId, authToken, messageId, part);

      assertTrue(result.isFailure());
      assertTrue(result.getCause() instanceof MailboxAttachmentService.AttachmentNotFound);
    }
  }

  @Test
  void getAttachment_should_return_failure_when_mime_message_is_null() throws Exception {
    try (MockedStatic<MailboxManager> mockedStatic = mockStatic(MailboxManager.class)) {

      String accountId = "testAccountId";
      int messageId = 1;
      String part = "1";

      when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(mailbox);
      mockedStatic.when(MailboxManager::getInstance).thenReturn(mailboxManager);

      when(operationContextFactory.createOperationContext(authToken)).thenReturn(context);
      when(mailbox.getMessageById(any(OperationContext.class), eq(messageId))).thenReturn(message);
      when(message.getMimeMessage()).thenReturn(null);

      Try<MimePart> result = mailboxAttachmentService.getAttachment(accountId, authToken, messageId, part);

      assertTrue(result.isFailure());
      assertTrue(result.getCause() instanceof AttachmentNotFound);
    }
  }

  @Test
  void getAttachmentByItemId_should_return_attachment_when_getting_message_by_itemId_successfully() throws Exception {
    try (MockedStatic<MailboxManager> mockedStatic = mockStatic(MailboxManager.class);
        MockedStatic<Mime> mockedMime = mockStatic(Mime.class)) {

      ItemId itemId = mock(ItemId.class);
      String accountId = "testAccountId";
      String part = "1";

      when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(mailbox);
      mockedStatic.when(MailboxManager::getInstance).thenReturn(mailboxManager);
      when(itemId.getId()).thenReturn(1);
      when(operationContextFactory.createOperationContext(authToken)).thenReturn(context);
      when(mailbox.getMessageById(any(OperationContext.class), eq(1))).thenReturn(message);
      when(message.getMimeMessage()).thenReturn(mimeMessage);
      mockedMime.when(() -> Mime.getMimePart(mimeMessage, part)).thenReturn(mimePart);

      Try<MimePart> result = mailboxAttachmentService.getAttachmentByItemId(accountId, authToken, itemId, part);

      assertTrue(result.isSuccess());
      assertEquals(mimePart, result.get());
    }
  }

  @Test
  void getAttachmentByItemId_should_fail_when_message_by_itemId_is_not_found() throws Exception {
    try (MockedStatic<MailboxManager> mockedStatic = mockStatic(MailboxManager.class)) {
      ItemId itemId = mock(ItemId.class);
      String accountId = "testAccountId";
      String part = "1";

      when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(mailbox);
      mockedStatic.when(MailboxManager::getInstance).thenReturn(mailboxManager);
      when(itemId.getId()).thenReturn(1);
      when(operationContextFactory.createOperationContext(authToken)).thenReturn(context);
      when(mailbox.getMessageById(any(OperationContext.class), eq(1))).thenReturn(null);

      Try<MimePart> result = mailboxAttachmentService.getAttachmentByItemId(accountId, authToken, itemId, part);

      assertTrue(result.isFailure());
      assertTrue(result.getCause() instanceof MessageNotFound);
    }
  }

  @Test
  void getAttachmentByItemId_should_fail_when_mimePart_is_null() throws Exception {
    try (MockedStatic<MailboxManager> mockedStatic = mockStatic(MailboxManager.class);
        MockedStatic<Mime> mockedMime = mockStatic(Mime.class)) {

      ItemId itemId = mock(ItemId.class);
      String accountId = "testAccountId";
      String part = "1";

      when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(mailbox);
      mockedStatic.when(MailboxManager::getInstance).thenReturn(mailboxManager);
      when(itemId.getId()).thenReturn(1);
      when(operationContextFactory.createOperationContext(authToken)).thenReturn(context);
      when(mailbox.getMessageById(any(OperationContext.class), eq(1))).thenReturn(message);
      when(message.getMimeMessage()).thenReturn(mimeMessage);
      mockedMime.when(() -> Mime.getMimePart(mimeMessage, part)).thenReturn(null);

      Try<MimePart> result = mailboxAttachmentService.getAttachmentByItemId(accountId, authToken, itemId, part);

      assertTrue(result.isFailure());
      assertTrue(result.getCause() instanceof MailboxAttachmentService.AttachmentNotFound);
    }
  }

  @Test
  void getAttachmentByItemId_should_return_attachment_when_getting_calendar_item_by_itemId_successfully()
      throws Exception {
    try (MockedStatic<MailboxManager> mockedStatic = mockStatic(MailboxManager.class);
        MockedStatic<Mime> mockedMime = mockStatic(Mime.class)) {

      ItemId itemId = mock(ItemId.class);
      String accountId = "testAccountId";
      String part = "1";

      when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(mailbox);
      mockedStatic.when(MailboxManager::getInstance).thenReturn(mailboxManager);
      when(itemId.getId()).thenReturn(1);
      when(itemId.hasSubpart()).thenReturn(true);
      when(operationContextFactory.createOperationContext(authToken)).thenReturn(context);
      when(mailbox.getAppointmentById(any(OperationContext.class), eq(1))).thenReturn(appointment);
      when(appointment.getMimeMessage()).thenReturn(mimeMessage);
      when(appointment.getSubpartMessage(itemId.getSubpartId())).thenReturn(mimeMessage);
      mockedMime.when(() -> Mime.getMimePart(mimeMessage, part)).thenReturn(mimePart);

      Try<MimePart> result = mailboxAttachmentService.getAttachmentByItemId(accountId, authToken, itemId, part);

      assertTrue(result.isSuccess());
      assertEquals(mimePart, result.get());
    }
  }

  @Test
  void getAttachmentByItemId_should_fail_when_attachment_not_found_in_calendar_item() throws Exception {
    try (MockedStatic<MailboxManager> mockedStatic = mockStatic(MailboxManager.class);
        MockedStatic<Mime> mockedMime = mockStatic(Mime.class)) {

      ItemId itemId = mock(ItemId.class);
      String accountId = "testAccountId";
      String part = "1";

      when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(mailbox);
      mockedStatic.when(MailboxManager::getInstance).thenReturn(mailboxManager);
      when(itemId.getId()).thenReturn(1);
      when(itemId.hasSubpart()).thenReturn(true);
      when(operationContextFactory.createOperationContext(authToken)).thenReturn(context);
      when(mailbox.getAppointmentById(any(OperationContext.class), eq(1))).thenReturn(appointment);
      when(appointment.getMimeMessage()).thenReturn(mimeMessage);
      when(appointment.getSubpartMessage(itemId.getSubpartId())).thenReturn(mimeMessage);
      mockedMime.when(() -> Mime.getMimePart(mimeMessage, part)).thenReturn(null);

      Try<MimePart> result = mailboxAttachmentService.getAttachmentByItemId(accountId, authToken, itemId, part);

      assertTrue(result.isFailure());
      assertTrue(result.getCause() instanceof AttachmentNotFound);
    }
  }
}