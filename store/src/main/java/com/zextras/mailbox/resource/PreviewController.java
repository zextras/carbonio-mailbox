// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.resource;

import static com.zextras.mailbox.filter.AuthorizationFilter.CTX_AUTH_TOKEN;

import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.carbonio.preview.queries.Query;
import com.zextras.carbonio.preview.queries.Query.QueryBuilder;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.AttachmentService;
import io.vavr.Function3;
import io.vavr.control.Try;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Objects;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;
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
   * Builds a {@link QueryBuilder} from received query String
   *
   * @param area comes from the url path and is optional
   * @return
   */
  private Query getPreviewQuery(String area) {
    String queryString = this.servletRequest.getQueryString();
    final Hashtable<String, String[]> parseQueryString = HttpUtils.parseQueryString(queryString);
    final QueryBuilder queryBuilder = new QueryBuilder();
    final String[] shapes = parseQueryString.get("shape");
    if (Objects.isNull(area) || Objects.equals(area, "")) {
      queryBuilder.setPreviewArea(area);
    }
    if (!Objects.isNull(shapes) && shapes.length > 0) {
      queryBuilder.setShape(shapes[0]);
    }
    final String[] firstPages = parseQueryString.get("first_page");
    if (!Objects.isNull(firstPages) && firstPages.length > 0) {
      final int firstPage = Integer.parseInt(firstPages[0]);
      queryBuilder.setFirstPage(firstPage);
    }
    final String[] lastPages = parseQueryString.get("last_page");
    if (!Objects.isNull(lastPages) && lastPages.length > 0) {
      final int lastPage = Integer.parseInt(lastPages[0]);
      queryBuilder.setLastPage(lastPage);
    }
    final String[] crops = parseQueryString.get("crop");
    if (!Objects.isNull(crops) && crops.length > 0) {
      final boolean crop = Boolean.parseBoolean(crops[0]);
      queryBuilder.setCrop(crop);
    }
    final String[] qualities = parseQueryString.get("quality");
    if (!Objects.isNull(qualities) && qualities.length > 0) {
      final String quality = qualities[0];
      queryBuilder.setQuality(quality);
    }
    return queryBuilder.build();
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
                                    inputStream, getPreviewQuery(area), attachment.getFileName())))
            .flatMap(x -> x);
    if (tryPreviewClientResponse.isSuccess()) {
      final BlobResponse blobResponse = tryPreviewClientResponse.get();
      return Response.ok().entity(new EntityInputStream(blobResponse.getContent())).build();
    }
    return Response.serverError().build();
  }
}
