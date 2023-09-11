// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.preview.resource;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.carbonio.preview.queries.Query;
import com.zextras.carbonio.preview.queries.Query.QueryBuilder;
import com.zextras.mailbox.preview.usecase.PreviewType;
import com.zextras.mailbox.preview.usecase.PreviewUseCase;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.ZimbraAuthTokenEncoded;
import com.zimbra.cs.service.AttachmentService;
import io.vavr.Function4;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Stream;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePart;
import org.apache.http.entity.InputStreamEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PreviewUseCaseTest {

  private PreviewUseCase previewUseCase;
  ;
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

    when(attachmentService.getAttachment(accountId, authToken, messageId, partNumber))
        .thenReturn(Try.of(() -> attachment));
    when(previewMethod.apply(previewClient, any(), eq(query), eq(fileName)))
        .thenReturn(Try.of(() -> previewResponse));

    final Tuple2<MimePart, BlobResponse> attachmentAndPreview =
        previewUseCase
            .getAttachmentAndPreview(
                accountId, authToken, previewType, messageId, partNumber, query)
            .get();

    assertEquals(attachment, attachmentAndPreview._1());
    assertEquals(previewResponse, attachmentAndPreview._2());
  }
}
