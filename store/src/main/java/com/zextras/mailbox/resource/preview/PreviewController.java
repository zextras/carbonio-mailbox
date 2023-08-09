// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.resource.preview;

import static com.zextras.mailbox.filter.AuthorizationFilter.CTX_AUTH_TOKEN;

import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.carbonio.preview.queries.Query;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.AttachmentService;
import io.vavr.Function3;
import io.vavr.control.Try;
import java.io.InputStream;
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

  public static final String MESSAGE_ID_REGEXP = "{messageId:([a-zA-Z\\-:0-9]+|[0-9]+)}";
  public static final String PART_NUMBER_REGEXP = "{partNumber:([0-9.]+(?:\\.[0-9.]+)?)}";
  public static final String THUMBNAIL_REGEXP = "{previewArea:([0-9]*x[0-9]*)}/thumbnail";

  private final PreviewClient previewClient;
  private final AttachmentService attachmentService;

  @Context private HttpServletRequest servletRequest;

  @Inject
  public PreviewController(AttachmentService attachmentService, PreviewClient previewClient) {
    this.previewClient = previewClient;
    this.attachmentService = attachmentService;
  }

  @GET
  @Path("/doc/" + MESSAGE_ID_REGEXP + "/" + PART_NUMBER_REGEXP)
  public Response getDocPreview(
      @PathParam("messageId") int messageId, @PathParam("partNumber") String partNumber) {
    return this.doPostPreview(messageId, partNumber, "", previewClient::postPreviewOfDocument);
  }

  @GET
  @Path("/doc/" + MESSAGE_ID_REGEXP + "/" + PART_NUMBER_REGEXP + "/" + THUMBNAIL_REGEXP)
  public Response getDocThumbnail(
      @PathParam("messageId") int messageId,
      @PathParam("partNumber") String partNumber,
      @PathParam("previewArea") String previewArea) {
    return this.doPostPreview(
        messageId, partNumber, previewArea, previewClient::postThumbnailOfDocument);
  }

  @GET
  @Path("/image/" + MESSAGE_ID_REGEXP + "/" + PART_NUMBER_REGEXP)
  public Response getImagePreview(
      @PathParam("messageId") int messageId, @PathParam("partNumber") String partNumber) {
    return this.doPostPreview(messageId, partNumber, "", previewClient::postPreviewOfImage);
  }

  @GET
  @Path("/image/" + MESSAGE_ID_REGEXP + "/" + PART_NUMBER_REGEXP + "/" + THUMBNAIL_REGEXP)
  public Response getImageThumbnail(
      @PathParam("messageId") int messageId,
      @PathParam("partNumber") String partNumber,
      @PathParam("previewArea") String previewArea) {
    return this.doPostPreview(
        messageId, partNumber, previewArea, previewClient::postThumbnailOfImage);
  }

  @GET
  @Path("/pdf/" + MESSAGE_ID_REGEXP + "/" + PART_NUMBER_REGEXP)
  public Response getPdfPreview(
      @PathParam("messageId") int messageId, @PathParam("partNumber") String partNumber) {
    return this.doPostPreview(messageId, partNumber, "", previewClient::postPreviewOfPdf);
  }

  @GET
  @Path("/pdf/" + MESSAGE_ID_REGEXP + "/" + PART_NUMBER_REGEXP + "/" + THUMBNAIL_REGEXP)
  public Response getPdfThumbnail(
      @PathParam("messageId") int messageId,
      @PathParam("partNumber") String partNumber,
      @PathParam("previewArea") String previewArea) {
    return this.doPostPreview(
        messageId, partNumber, previewArea, previewClient::postThumbnailOfPdf);
  }

  /**
   * This method gets the attachment and ask preview service for a preview. It assumes the same
   * logic for all attachment types.
   *
   * @param messageId message id of email
   * @param partNumber part number of mime attachment
   * @param doPost preview post execution method
   * @return
   */
  private Response doPostPreview(
      int messageId,
      String partNumber,
      String area,
      Function3<InputStream, Query, String, Try<BlobResponse>> doPost) {
    final AuthToken authToken = (AuthToken) servletRequest.getAttribute(CTX_AUTH_TOKEN);
    final Try<BlobResponse> tryPreviewClientResponse =
        attachmentService
            .getAttachment(authToken.getAccountId(), authToken, messageId, partNumber)
            .flatMap(
                attachment ->
                    Try.withResources(attachment::getInputStream)
                        .of(
                            inputStream ->
                                doPost.apply(
                                    inputStream,
                                    PreviewQueryUtil.getPreviewQuery(
                                        this.servletRequest.getQueryString(), area),
                                    attachment.getFileName())))
            .flatMap(x -> x);
    if (tryPreviewClientResponse.isSuccess()) {
      final BlobResponse blobResponse = tryPreviewClientResponse.get();
      return Response.ok().entity(new EntityInputStream(blobResponse.getContent())).build();
    }
    return Response.serverError().build();
  }
}
