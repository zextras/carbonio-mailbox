// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.resource.preview;

import static com.zextras.mailbox.filter.AuthorizationFilter.CTX_AUTH_TOKEN;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import com.zextras.carbonio.preview.queries.Query;
import com.zimbra.cs.account.AuthToken;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.message.internal.EntityInputStream;

@Path("/")
public class PreviewController {

  private static final String PREVIEW_REGEXP =
      "{messageId:([a-zA-Z\\-:0-9]+|[0-9]+)}/{partNumber:([0-9.]+(?:\\.[0-9.]+)?)}/{previewArea:([0-9]*x[0-9]*)}";
  private static final String THUMBNAIL_REGEXP = PREVIEW_REGEXP + "/thumbnail";

  private final PreviewService previewService;

  @Context private HttpServletRequest servletRequest;

  @Inject
  public PreviewController(PreviewService previewService) {
    this.previewService = previewService;
  }

  // TODO: support display attachment

  @GET
  @Path("/doc/" + PREVIEW_REGEXP)
  public Response getDocPreview(
      @PathParam("messageId") String messageId,
      @PathParam("partNumber") String partNumber,
      @PathParam("previewArea") String previewArea) {
    return this.doPostPreview(messageId, partNumber, previewArea, PreviewType.DOC);
  }

  @GET
  @Path("/doc/" + THUMBNAIL_REGEXP)
  public Response getDocThumbnail(
      @PathParam("messageId") String messageId,
      @PathParam("partNumber") String partNumber,
      @PathParam("previewArea") String previewArea) {
    return this.doPostPreview(messageId, partNumber, previewArea, PreviewType.DOC_THUMBNAIL);
  }

  @GET
  @Path("/image/" + PREVIEW_REGEXP)
  public Response getImagePreview(
      @PathParam("messageId") String messageId,
      @PathParam("partNumber") String partNumber,
      @PathParam("previewArea") String previewArea) {
    return this.doPostPreview(messageId, partNumber, previewArea, PreviewType.IMAGE);
  }

  @GET
  @Path("/image/" + THUMBNAIL_REGEXP)
  public Response getImageThumbnail(
      @PathParam("messageId") String messageId,
      @PathParam("partNumber") String partNumber,
      @PathParam("previewArea") String previewArea) {
    return this.doPostPreview(messageId, partNumber, previewArea, PreviewType.IMAGE_THUMBNAIL);
  }

  @GET
  @Path("/pdf/" + PREVIEW_REGEXP)
  public Response getPdfPreview(
      @PathParam("messageId") String messageId,
      @PathParam("partNumber") String partNumber,
      @PathParam("previewArea") String previewArea) {
    return this.doPostPreview(messageId, partNumber, previewArea, PreviewType.PDF);
  }

  @GET
  @Path("/pdf/" + THUMBNAIL_REGEXP)
  public Response getPdfThumbnail(
      @PathParam("messageId") String messageId,
      @PathParam("partNumber") String partNumber,
      @PathParam("previewArea") String previewArea) {
    return this.doPostPreview(messageId, partNumber, previewArea, PreviewType.PDF_THUMBNAIL);
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
      String accountUuidMessageId, String partNumber, String area, PreviewType previewType) {

    final AuthToken authToken = (AuthToken) servletRequest.getAttribute(CTX_AUTH_TOKEN);
    final Query query =
        PreviewQueryUtil.getPreviewQuery(this.servletRequest.getQueryString(), area);
    final String dispositionParameter = this.servletRequest.getParameter("disp");
    AtomicReference<String> disposition = new AtomicReference<>("inline");
    if (Objects.equals("attachment", dispositionParameter)) {
      disposition.set("attachment");
    }

    // TODO: refactor this code along with same logic in CopyToFiles
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
                        disposition.get()
                            + "; filename*=UTF-8''"
                            + URLEncoder.encode(
                                attachmentAndPreview._1().getFileName(), StandardCharsets.UTF_8))
                    .header(CONTENT_TYPE, attachmentAndPreview._1().getContentType())
                    .build())
        .getOrElseGet((Throwable error) -> Response.serverError().build());
  }
}
