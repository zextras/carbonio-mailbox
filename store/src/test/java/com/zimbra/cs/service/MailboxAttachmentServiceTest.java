// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service;

import com.zextras.mailbox.util.MailMessageBuilder;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.ParsedMessage;
import io.vavr.control.Try;
import javax.mail.internet.MimePart;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MailboxAttachmentServiceTest {

  private static AccountCreator.Factory accountCreatorFactory;
  private static AccountAction.Factory accountActionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    MailboxTestUtil.setUp();
    accountCreatorFactory = AccountCreator.Factory.getDefault();
    accountActionFactory = AccountAction.Factory.getDefault();
  }

  @AfterAll
  static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @Test
  void shouldThrowMessageNotFoundWhenNoSuchMessage() throws Exception {
    final Account account = accountCreatorFactory.get().create();

    final Try<MimePart> attachment = new MailboxAttachmentService()
        .getAttachment(account.getId(), new ZimbraAuthToken(account), 1, "123");

    Assertions.assertTrue(attachment.isFailure());
    Assertions.assertEquals("Message not found.", attachment.getCause().getMessage());
  }

  @Test
  void shouldThrowAttachmentNotFoundWhenNoAttachmentPart() throws Exception {
    final Account account = accountCreatorFactory.get().create();
    final ParsedMessage message = new MailMessageBuilder().build();
    final Message draft = accountActionFactory.forAccount(account).saveDraft(message);

    final Try<MimePart> attachment = new MailboxAttachmentService()
        .getAttachment(account.getId(), new ZimbraAuthToken(account), draft.getId(), "123");

    Assertions.assertTrue(attachment.isFailure());
    Assertions.assertEquals("Missing part 123.", attachment.getCause().getMessage());
  }

  @Test
  void shouldReturnAttachment() throws Exception {
    final Account account = accountCreatorFactory.get().create();
    final String attachmentContent = "Hello there. This is a PDF.";
    final String fileName = "hello_there.pdf";
    final String contentType = "application/pdf";
    final ParsedMessage message = new MailMessageBuilder()
        .addAttachment(attachmentContent, fileName, contentType)
        .build();
    final Message draft = accountActionFactory.forAccount(account).saveDraft(message);

    final Try<MimePart> attachment = new MailboxAttachmentService()
        .getAttachment(account.getId(), new ZimbraAuthToken(account), draft.getId(), "2");

    Assertions.assertTrue(attachment.isSuccess());
    final MimePart mimePart = attachment.get();
    Assertions.assertEquals(contentType + "; name=" + fileName, mimePart.getContentType());
    Assertions.assertEquals(fileName, mimePart.getFileName());
    Assertions.assertEquals(attachmentContent, new String(mimePart.getInputStream().readAllBytes()));
  }

}