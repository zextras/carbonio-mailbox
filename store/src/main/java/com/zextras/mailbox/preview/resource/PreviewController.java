// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.preview.resource;

import static com.zextras.mailbox.filter.AuthorizationFilter.CTX_AUTH_TOKEN;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import com.zextras.carbonio.preview.queries.Query;
import com.zextras.mailbox.preview.usecase.PreviewType;
import com.zextras.mailbox.preview.usecase.PreviewUseCase;
import com.zimbra.cs.account.AuthToken;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.message.internal.EntityInputStream;

public class PreviewController implements PreviewApi {

  private final PreviewUseCase previewUseCase;

  @Context private HttpServletRequest servletRequest;

  @Inject
  public PreviewController(PreviewUseCase previewUseCase) {
    this.previewUseCase = previewUseCase;
  }

  /**
   * Ask attachment and preview from {@link PreviewUseCase} by providing needed args. Builds the
   * final {@link Response} from returned answer.
   *
   * @param previewType type of preview to ask
   * @param accountUuidMessageId parameter with accountUuid and MessageId or just MessageId
   * @param partNumber part of attachment
   * @param disposition disposition to return
   * @param query query for {@link com.zextras.carbonio.preview.PreviewClient}
   * @return {@link Response} for controller
   */
  private Response doPostPreview(
      PreviewType previewType,
      String accountUuidMessageId,
      String partNumber,
      String disposition,
      Query query) {

    final AuthToken authToken = (AuthToken) servletRequest.getAttribute(CTX_AUTH_TOKEN);

    int messageId;
    String accountUuid = authToken.getAccountId();
    final String[] accountUuidAndMessageId = accountUuidMessageId.split(":");
    if (accountUuidAndMessageId.length > 1) {
      accountUuid = accountUuidAndMessageId[0];
      messageId = Integer.parseInt(accountUuidAndMessageId[1]);
    } else {
      messageId = Integer.parseInt(accountUuidAndMessageId[0]);
    }
    return previewUseCase
        .getAttachmentAndPreview(accountUuid, authToken, previewType, messageId, partNumber, query)
        .mapTry(
            attachmentAndPreview ->
                Response.ok()
                    .entity(new EntityInputStream(attachmentAndPreview._2().getContent()))
                    .header(
                        CONTENT_DISPOSITION,
                        disposition
                            + "; filename*=UTF-8''"
                            + URLEncoder.encode(
                                attachmentAndPreview._1().getFileName(), StandardCharsets.UTF_8))
                    .header(CONTENT_TYPE, attachmentAndPreview._2().getMimeType())
                    .build())
        .getOrElseGet((Throwable error) -> Response.serverError().build());
  }

  /**
   * Builds a {@link Query} for preview and executes the business logic.
   *
   * @param previewType type of preview
   * @param accountUuidAndMessageId
   * @param part part number
   * @param outputFormat output format for preview
   * @param firstPage first page for preview
   * @param lastPage last page for preview
   * @param crop if crop preview
   * @param quality quality of preview
   * @param shape shape of preview
   * @param disposition disposition to return in response
   * @return {@link Response} with attachment preview
   */
  private Response doPostPreview(
      PreviewType previewType,
      String accountUuidAndMessageId,
      String part,
      String area,
      String outputFormat,
      String firstPage,
      String lastPage,
      Boolean crop,
      String quality,
      String shape,
      String disposition) {
    final Query query =
        PreviewQueryUtil.getPreviewQuery(
            area, outputFormat, firstPage, lastPage, crop, quality, shape);
    return this.doPostPreview(previewType, accountUuidAndMessageId, part, disposition, query);
  }

  @Override
  public Response previewDocument(
      String messageId,
      String part,
      String outputFormat,
      String firstPage,
      String lastPage,
      Boolean crop,
      String quality,
      String shape,
      String disp) {
    return this.doPostPreview(
        PreviewType.DOC,
        messageId,
        part,
        null,
        outputFormat,
        firstPage,
        lastPage,
        crop,
        quality,
        shape,
        disp);
  }

  @Override
  public Response previewDocumentThumbnail(
      String messageId,
      String part,
      String outputFormat,
      String firstPage,
      String lastPage,
      Boolean crop,
      String quality,
      String shape,
      String disp) {
    return doPostPreview(
        PreviewType.DOC_THUMBNAIL,
        messageId,
        part,
        null,
        outputFormat,
        firstPage,
        lastPage,
        crop,
        quality,
        shape,
        disp);
  }

  @Override
  public Response previewImage(
      String messageId,
      String part,
      String area,
      String outputFormat,
      String firstPage,
      String lastPage,
      Boolean crop,
      String quality,
      String shape,
      String disp) {
    return doPostPreview(
        PreviewType.IMAGE,
        messageId,
        part,
        area,
        outputFormat,
        firstPage,
        lastPage,
        crop,
        quality,
        shape,
        disp);
  }

  @Override
  public Response previewImageThumbnail(
      String messageId,
      String part,
      String area,
      String outputFormat,
      String firstPage,
      String lastPage,
      Boolean crop,
      String quality,
      String shape,
      String disp) {
    return doPostPreview(
        PreviewType.IMAGE_THUMBNAIL,
        messageId,
        part,
        area,
        outputFormat,
        firstPage,
        lastPage,
        crop,
        quality,
        shape,
        disp);
  }

  @Override
  public Response previewPdf(
      String messageId,
      String part,
      String outputFormat,
      String firstPage,
      String lastPage,
      Boolean crop,
      String quality,
      String shape,
      String disp) {
    return doPostPreview(
        PreviewType.PDF,
        messageId,
        part,
        null,
        outputFormat,
        firstPage,
        lastPage,
        crop,
        quality,
        shape,
        disp);
  }

  @Override
  public Response previewPdfThumbnail(
      String messageId,
      String part,
      String outputFormat,
      String firstPage,
      String lastPage,
      Boolean crop,
      String quality,
      String shape,
      String disp) {
    return doPostPreview(
        PreviewType.PDF_THUMBNAIL,
        messageId,
        part,
        null,
        outputFormat,
        firstPage,
        lastPage,
        crop,
        quality,
        shape,
        disp);
  }
}
