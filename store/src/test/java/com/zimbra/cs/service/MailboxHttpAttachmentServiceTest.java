// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.client.MailboxHttpClient;
import com.zextras.mailbox.client.UserServletRequest;
import com.zextras.mailbox.client.UserServletResponse;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.ZimbraAuthTokenEncoded;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.mail.internet.MimePart;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MailboxHttpAttachmentServiceTest {

  @Test
  void shouldGetAttachment() throws Exception {
    final MailboxHttpClient mailboxHttpClient = mock(MailboxHttpClient.class);
    final MailboxHttpAttachmentService attachmentService =
        new MailboxHttpAttachmentService(mailboxHttpClient);
    final String accountUuid = UUID.randomUUID().toString();
    final AuthToken token = new ZimbraAuthTokenEncoded("token");
    final int messageId = 1;
    final String part = "2";
    final UserServletRequest userServletRequest =
        UserServletRequest.buildRequest("co", String.valueOf(messageId), part);
    final String contentType = ContentType.TEXT_PLAIN.getMimeType();
    final String fileName = "hello.txt";
    final InputStream content =
        new ByteArrayInputStream("hello.txt".getBytes(StandardCharsets.UTF_8));
    final InputStream expectedContent =
        new ByteArrayInputStream("hello.txt".getBytes(StandardCharsets.UTF_8));
    when(mailboxHttpClient.callUserServlet(token, accountUuid, userServletRequest))
        .thenReturn(Try.of(() -> new UserServletResponse(contentType, fileName, content)));
    final MimePart attachment =
        attachmentService.getAttachment(accountUuid, token, messageId, part).get();
    Assertions.assertEquals(contentType, attachment.getContentType());
    Assertions.assertEquals(fileName, attachment.getFileName());
    Assertions.assertArrayEquals(
        expectedContent.readAllBytes(), attachment.getInputStream().readAllBytes());
  }

  @Test
  void shouldReturnFailureWhenMailboxClientFails() throws Exception {
    final MailboxHttpClient mailboxHttpClient = mock(MailboxHttpClient.class);
    final MailboxHttpAttachmentService attachmentService =
        new MailboxHttpAttachmentService(mailboxHttpClient);
    when(mailboxHttpClient.callUserServlet(any(), anyString(), any()))
        .thenThrow(new RuntimeException("failed!"));
    final Try<MimePart> attachmentTry =
        attachmentService.getAttachment(
            UUID.randomUUID().toString(), new ZimbraAuthTokenEncoded("hello"), 1, "2");
    assertTrue(attachmentTry.isFailure());
  }
}
