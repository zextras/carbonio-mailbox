// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.preview.resource;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.carbonio.preview.queries.Query;
import com.zextras.carbonio.preview.queries.Query.QueryBuilder;
import com.zextras.mailbox.client.MailboxHttpClientException;
import com.zextras.mailbox.preview.usecase.AttachmentNotFoundException;
import com.zextras.mailbox.preview.usecase.AttachmentPreview;
import com.zextras.mailbox.preview.usecase.PreviewError;
import com.zextras.mailbox.preview.usecase.PreviewNotHealthy;
import com.zextras.mailbox.preview.usecase.PreviewType;
import com.zextras.mailbox.preview.usecase.PreviewUseCase;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.ZimbraAuthTokenEncoded;
import com.zimbra.cs.service.AttachmentService;
import io.vavr.Function4;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Stream;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePart;
import org.apache.http.HttpStatus;
import org.apache.http.entity.InputStreamEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PreviewUseCaseTest {

  private PreviewUseCase previewUseCase;

  private AttachmentService attachmentService;
  private PreviewClient previewClient;

  @BeforeEach
  public void beforeEach() {
    attachmentService = mock(AttachmentService.class);
    previewClient = mock(PreviewClient.class);
    this.previewUseCase = new PreviewUseCase(previewClient, attachmentService);
  }

  private static Stream<Arguments> getPreviewTypeFunctions() {
    return Stream.of(
        Arguments.of(PreviewType.PDF, Function4.of(PreviewClient::postPreviewOfPdf)),
        Arguments.of(PreviewType.PDF_THUMBNAIL, Function4.of(PreviewClient::postThumbnailOfPdf)),
        Arguments.of(PreviewType.DOC, Function4.of(PreviewClient::postPreviewOfDocument)),
        Arguments.of(
            PreviewType.DOC_THUMBNAIL, Function4.of(PreviewClient::postThumbnailOfDocument)),
        Arguments.of(PreviewType.IMAGE, Function4.of(PreviewClient::postPreviewOfImage)),
        Arguments.of(
            PreviewType.IMAGE_THUMBNAIL, Function4.of(PreviewClient::postThumbnailOfImage)));
  }

  @ParameterizedTest
  @MethodSource("getPreviewTypeFunctions")
  void shouldReturnAttachmentAndPreviewWhenAskingForThem(
      PreviewType previewType, Function4 previewMethod) throws Exception {
    final String accountId = UUID.randomUUID().toString();
    final int messageId = 1;
    final String partNumber = "2";
    final String fileName = "TestIt";
    final AuthToken authToken = new ZimbraAuthTokenEncoded("");
    final byte[] byteContent = "Hello".getBytes(StandardCharsets.UTF_8);
    final String contentType = "whatever";
    final MimePart attachment = new MimeBodyPart(new ByteArrayInputStream(byteContent));
    final Query query = new QueryBuilder().build();
    attachment.setFileName(fileName);
    attachment.setHeader(CONTENT_TYPE, contentType);
    final InputStreamEntity inputStreamEntity = new InputStreamEntity(attachment.getInputStream());
    inputStreamEntity.setContentType(attachment.getContentType());
    final BlobResponse previewResponse = new BlobResponse(inputStreamEntity);
    when(previewClient.healthLive()).thenReturn(true);
    when(attachmentService.getAttachment(accountId, authToken, messageId, partNumber))
        .thenReturn(Try.of(() -> attachment));
    when(previewMethod.apply(previewClient, any(), eq(query), eq(fileName)))
        .thenReturn(Try.of(() -> previewResponse));

    final AttachmentPreview attachmentPreview =
        previewUseCase
            .getAttachmentAndPreview(
                accountId, authToken, previewType, messageId, partNumber, query)
            .get();

    assertEquals(fileName, attachmentPreview.getFileName());
    assertEquals(previewResponse.getContent(), attachmentPreview.getContent());
    assertEquals(previewResponse.getMimeType(), attachmentPreview.getMimeType());
  }

  private void setUpMockData() throws Exception {
    final String fileName = "TestIt";
    final byte[] byteContent = "Hello".getBytes(StandardCharsets.UTF_8);
    final String contentType = "whatever";
    final MimePart attachment = new MimeBodyPart(new ByteArrayInputStream(byteContent));
    attachment.setFileName(fileName);
    attachment.setHeader(CONTENT_TYPE, contentType);
    final InputStreamEntity inputStreamEntity = new InputStreamEntity(attachment.getInputStream());
    inputStreamEntity.setContentType(attachment.getContentType());

    when(previewClient.healthLive()).thenReturn(true);
    when(attachmentService.getAttachment(anyString(), any(), anyInt(), anyString()))
        .thenReturn(Try.of(() -> attachment));
  }

  @Test
  void shouldReturnFailureWhenPreviewServiceNotHealthy() throws Exception {
    setUpMockData();
    when(previewClient.healthLive()).thenReturn(false);
    final Try<AttachmentPreview> attachmentAndPreview =
        previewUseCase.getAttachmentAndPreview(
            UUID.randomUUID().toString(),
            new ZimbraAuthTokenEncoded(""),
            PreviewType.PDF,
            1,
            "2",
            null);
    Assertions.assertTrue(attachmentAndPreview.isFailure());
    Assertions.assertThrows(PreviewNotHealthy.class, attachmentAndPreview::get);
  }

  @Test
  void shouldReturnFailureWhenPreviewClientError() throws Exception {
    setUpMockData();
    final PreviewType previewType = mock(PreviewType.class);
    when(previewType.getFunction())
        .thenReturn(Function4.constant(Try.failure(new RuntimeException(""))));
    final Try<AttachmentPreview> attachmentAndPreview =
        previewUseCase.getAttachmentAndPreview(
            UUID.randomUUID().toString(),
            new ZimbraAuthTokenEncoded(""),
            previewType,
            1,
            "2",
            null);
    Assertions.assertTrue(attachmentAndPreview.isFailure());
    Assertions.assertThrows(PreviewError.class, attachmentAndPreview::get);
  }

  @Test
  void shouldReturnAttachmentNotFoundExceptionFailureWhenAttachmentNotFound() throws Exception {
    setUpMockData();
    final PreviewType previewType = mock(PreviewType.class);
    when(attachmentService.getAttachment(anyString(), any(), anyInt(), anyString()))
        .thenReturn(
            Try.failure(
                new MailboxHttpClientException(
                    HttpStatus.SC_NOT_FOUND, "Unable to find requested attachment")));
    final Try<AttachmentPreview> attachmentAndPreview =
        previewUseCase.getAttachmentAndPreview(
            UUID.randomUUID().toString(),
            new ZimbraAuthTokenEncoded(""),
            previewType,
            1,
            "2",
            null);
    Assertions.assertTrue(attachmentAndPreview.isFailure());
    Assertions.assertThrows(AttachmentNotFoundException.class, attachmentAndPreview::get);
  }
}
