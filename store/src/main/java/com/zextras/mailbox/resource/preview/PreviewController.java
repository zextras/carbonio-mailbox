// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.resource.preview;

import static com.zextras.mailbox.filter.AuthorizationFilter.CTX_AUTH_TOKEN;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import com.zextras.carbonio.preview.queries.Query;
import com.zextras.mailbox.resource.PreviewApi;
import com.zimbra.cs.account.AuthToken;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.message.internal.EntityInputStream;

public class PreviewController implements PreviewApi {

  private final PreviewService previewService;

  @Context private HttpServletRequest servletRequest;

  @Inject
  public PreviewController(PreviewService previewService) {
    this.previewService = previewService;
  }

  /**
   * This method gets the attachment and ask preview service for a preview. It assumes the same
   * logic for all attachment types.
   *
   * @param accountUuidMessageId message id of email, can be accountUuid:id or just id of email.
   * @param partNumber part number of mime attachme
   * @return
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
    return previewService
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
   * Builds a {@link Query} for preview
   *
   * @param previewType
   * @param messageId
   * @param part
   * @param outputFormat
   * @param firstPage
   * @param lastPage
   * @param crop
   * @param quality
   * @param shape
   * @param disposition
   * @return
   */
  private Response doPostPreview(
      PreviewType previewType,
      String messageId,
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
    return this.doPostPreview(previewType, messageId, part, disposition, query);
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
