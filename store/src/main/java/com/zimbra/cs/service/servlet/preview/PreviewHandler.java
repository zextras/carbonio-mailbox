package com.zimbra.cs.service.servlet.preview;

import static com.zimbra.cs.servlet.ZimbraServlet.proxyServletRequest;

import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.exceptions.BadRequest;
import com.zextras.carbonio.preview.exceptions.ItemNotFound;
import com.zextras.carbonio.preview.exceptions.ValidationError;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.servlet.ZimbraServlet;
import io.vavr.control.Try;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.protocol.HTTP;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Handles preview and thumbnail operations for attachments.
 * <p>
 * This class is responsible for processing HTTP requests related to attachment previews and thumbnails. It performs
 * several tasks such as checking the health of the preview client, validating authentication tokens, extracting and
 * validating query parameters, and handling requests for local or remote attachments.
 * </p>
 */
public class PreviewHandler {

  private static final Log LOG = LogFactory.getLog(PreviewHandler.class);
  private final PreviewClient previewClient;
  private final AttachmentService attachmentService;
  private final ItemIdFactory itemIdFactory;

  /**
   * Constructs a new {@link PreviewHandler} with the specified dependencies.
   *
   * @param previewClient     the {@link PreviewClient} used to interact with the remote/local preview service
   * @param attachmentService the {@link AttachmentService} used to retrieve attachments from local mailbox
   * @param itemIdFactory     the {@link ItemIdFactory} used to create {@link ItemId} instances
   */
  public PreviewHandler(PreviewClient previewClient,
      AttachmentService attachmentService, ItemIdFactory itemIdFactory) {
    this.previewClient = previewClient;
    this.attachmentService = attachmentService;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * Processes an HTTP request to handle preview or thumbnail operations for attachments.
   * <p>
   * This method performs several tasks:
   * <ul>
   *     <li>Checks if the preview client service is available. If not, it responds with an error.</li>
   *     <li>Extracts and validates the authentication token from the request. If missing or invalid, it responds with an unauthorized error.</li>
   *     <li>Extracts and validates the required query parameters from the request URL. If parameters are missing or invalid, it responds with a bad request error.</li>
   *     <li>Retrieves the {@link ItemId} associated with the given {@code messageId}. If the retrieval fails, it responds with a bad request error.</li>
   *     <li>Based on whether the item is local or remote, either handles the local attachment or proxies the request to the target mail host.</li>
   * </ul>
   * </p>
   *
   * @param request  the {@link HttpServletRequest} object containing the request details
   * @param response the {@link HttpServletResponse} object used to send the response
   */
  public void handle(HttpServletRequest request, HttpServletResponse response) {
    if (!previewClient.healthReady()) {
      respondWithError(request, response, Constants.STATUS_UNPROCESSABLE_ENTITY,
          "Preview service is down or not available to take request");
      return;
    }

    var authTokenTry = Try.of(() -> ZimbraServlet.getAuthTokenFromCookie(request, response));
    if (authTokenTry.isFailure() || authTokenTry.get() == null) {
      respondWithError(request, response, HttpServletResponse.SC_UNAUTHORIZED,
          "Authentication required. Request missing authentication token.");
      return;
    }

    var queryParametersTry = Utils.extractRequiredQueryParameters(request);
    if (queryParametersTry.isFailure()) {
      respondWithError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
      return;
    }

    var messageId = queryParametersTry.get()[0];
    var partId = queryParametersTry.get()[1];

    var itemIdTry = Try.of(() -> Utils.getItemIdFromMessageId(itemIdFactory, messageId, authTokenTry.get()));
    if (itemIdTry.isFailure() || itemIdTry.get() == null) {
      var message = itemIdTry.getCause().getMessage();
      if (message == null || message.trim().isEmpty() || message.toLowerCase().contains("account")) {
        message = "Error processing requested attachment. Ensure message ID or account are correct.";
      }
      respondWithError(request, response, HttpServletResponse.SC_BAD_REQUEST, message);
      return;
    }

    Try.of(itemIdTry::get)
        .flatMap(itemId ->
            Try.of(itemId::isLocal)
                .flatMap(isLocal ->
                    Try.run(() -> {
                      if (Boolean.TRUE.equals(isLocal)) {
                        handleLocalAttachment(request, response, authTokenTry.get(), itemId, partId);
                      } else {
                        proxyRequestToTargetMailHost(request, response, itemId.getAccountId());
                      }
                    })
                )
        )
        .onFailure(
            ex -> respondWithError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage()));
  }

  /**
   * Handles the processing of a local attachment(exists on the same mail host where the preview request arrived)
   * preview request.
   *
   * <p>This method retrieves an attachment based on the provided {@link ItemId} and {@code partId}.
   * If the attachment is found, it is mapped to a {@link DataBlob} and further processed to generate an attachment
   * preview. The method then responds with the appropriate status and information based on the success or failure of
   * the processing.</p>
   *
   * <p>If the attachment cannot be retrieved or processed, the method responds with an error message
   * and the corresponding HTTP status code.</p>
   *
   * @param request   the {@link HttpServletRequest} object containing the request details
   * @param response  the {@link HttpServletResponse} object used to send the response
   * @param authToken the {@link AuthToken} used for authentication and authorization
   * @param itemId    the {@link ItemId} identifying the attachment to be retrieved
   * @param partId    the ID of the specific part of the attachment to be retrieved
   */
  private void handleLocalAttachment(HttpServletRequest request, HttpServletResponse response,
      AuthToken authToken, ItemId itemId, String partId) {
    var mimePartTry = attachmentService.getAttachment(itemId.getAccountId(), authToken, itemId.getId(), partId);
    if (mimePartTry.isFailure() || mimePartTry.get() == null) {
      var message = mimePartTry.getCause().getMessage();
      if (message == null || message.trim().isEmpty() || message.toLowerCase().contains("account")) {
        message = "Something went wrong while accessing attachment.";
      }
      respondWithError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
      return;
    }

    mimePartTry.mapTry(Utils::mapMimePartResponseToDataBlob)
        .flatMapTry(attachmentDataBlob -> getAttachmentPreview(request, attachmentDataBlob))
        .onSuccess(previewDataBlob -> respondWithSuccess(response, request, previewDataBlob))
        .onFailure(BadRequest.class,
            ex -> respondWithError(request, response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage()))
        .onFailure(ItemNotFound.class,
            ex -> respondWithError(request, response, HttpServletResponse.SC_NOT_FOUND, ex.getMessage()))
        .onFailure(ValidationError.class,
            ex -> respondWithError(request, response, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage()))
        .onFailure(ex -> {
          var message = ex.getMessage();
          if (message == null || message.trim().isEmpty()) {
            message = "Something went wrong while processing preview of attachment.";
          }
          respondWithError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        });
  }

  /**
   * Proxies preview HTTP request to the mail host associated with the specified account ID.
   *
   * <p>This method extracts the request ID from the provided {@link HttpServletRequest} object,
   * logs the action of proxying the request along with the target account ID, and then invokes the {@link
   * ZimbraServlet#proxyServletRequest} method to handle the actual request forwarding.</p>
   *
   * @param request         the {@link HttpServletRequest} object containing the request to be proxied
   * @param response        the {@link HttpServletResponse} object used to send the response
   * @param targetAccountId the ID of the target account whose mail host will receive the request
   * @throws IOException      if an I/O error occurs during the processing of the request or response
   * @throws ServiceException if a service-related error occurs while proxying the request
   * @throws HttpException    if an HTTP error occurs during the proxying process
   */
  void proxyRequestToTargetMailHost(HttpServletRequest request, HttpServletResponse response, String targetAccountId)
      throws IOException, ServiceException, HttpException {
    var requestId = Utils.getRequestIdFromRequest(request);
    LOG.info("[" + requestId + "] Proxying request to target account(" + targetAccountId + ")'s mail host.");
    proxyServletRequest(request, response, targetAccountId,
        Map.of(Constants.REQUEST_ID_KEY, requestId != null ? requestId : "N/A"));
  }

  /**
   * Gets the preview of passed attachment from the preview service based on the requestUrl, calling different endpoints
   * of preview service
   *
   * @param request            the {@link HttpServletRequest} object
   * @param attachmentMimePart the {@link DataBlob} object
   * @return the {@link DataBlob} object
   */
  Try<DataBlob> getAttachmentPreview(
      HttpServletRequest request, DataBlob attachmentMimePart) {
    var requestId = Utils.getRequestIdFromRequest(request);
    var requestUrl = Utils.getFullURLFromRequest(request);
    var dispositionType = Utils.getDispositionTypeFromQueryParams(requestUrl);
    var requestUrlForPreview = Utils.removeQueryParams(requestUrl,
        List.of(Constants.REQUEST_PARAM_DISP, Constants.REQUEST_ID_KEY));
    var attachmentFileName = Try.of(attachmentMimePart::getFilename).getOrElse("unknown");
    var attachmentMimePartInputStream = attachmentMimePart.getBlobInputStream();

    var imagePreviewMatcher =
        Pattern.compile(Constants.IMG_PREVIEW_REGEX).matcher(requestUrlForPreview);

    var imageThumbnailMatcher =
        Pattern.compile(Constants.IMG_THUMBNAIL_REGEX).matcher((requestUrlForPreview));

    var pdfPreviewMatcher =
        Pattern.compile(Constants.PDF_PREVIEW_REGEX).matcher((requestUrlForPreview));

    var pdfThumbnailMatcher =
        Pattern.compile(Constants.PDF_THUMBNAIL_REGEX).matcher((requestUrlForPreview));

    var documentPreviewMatcher =
        Pattern.compile(Constants.DOC_PREVIEW_REGEX).matcher(requestUrlForPreview);

    var documentThumbnailMatcher =
        Pattern.compile(Constants.DOC_THUMBNAIL_REGEX).matcher((requestUrlForPreview));

    if (imageThumbnailMatcher.find()) {
      var previewArea = imageThumbnailMatcher.group(3);
      var queryParameters =
          Utils.parseQueryParameters(imageThumbnailMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postThumbnailOfImage(
                      attachmentMimePartInputStream,
                      Utils.generateQuery(previewArea, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              thumbnailOfImage ->
                  Utils.mapPreviewResponseToDataBlob(
                      thumbnailOfImage.get(), attachmentFileName, dispositionType));
    }

    if (pdfThumbnailMatcher.find()) {
      var previewArea = pdfThumbnailMatcher.group(3);
      var queryParameters =
          Utils.parseQueryParameters(pdfThumbnailMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postThumbnailOfPdf(
                      attachmentMimePartInputStream,
                      Utils.generateQuery(previewArea, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              thumbnailOfPdf ->
                  Utils.mapPreviewResponseToDataBlob(
                      thumbnailOfPdf.get(), attachmentFileName, dispositionType));
    }

    if (documentThumbnailMatcher.find()) {
      var previewArea = documentThumbnailMatcher.group(3);
      var queryParameters =
          Utils.parseQueryParameters(documentThumbnailMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postThumbnailOfDocument(
                      attachmentMimePartInputStream,
                      Utils.generateQuery(previewArea, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              thumbnailOfDocument ->
                  Utils.mapPreviewResponseToDataBlob(
                      thumbnailOfDocument.get(), attachmentFileName, dispositionType));
    }

    if (imagePreviewMatcher.find()) {
      var previewArea = imagePreviewMatcher.group(3);
      var queryParameters =
          Utils.parseQueryParameters(imagePreviewMatcher.group(5));
      return Try.of(
              () ->
                  previewClient.postPreviewOfImage(
                      attachmentMimePartInputStream,
                      Utils.generateQuery(previewArea, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              previewOfImage ->
                  Utils.mapPreviewResponseToDataBlob(
                      previewOfImage.get(), attachmentFileName, dispositionType));
    }

    if (pdfPreviewMatcher.find()) {
      var queryParameters =
          Utils.parseQueryParameters(pdfPreviewMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postPreviewOfPdf(
                      attachmentMimePartInputStream,
                      Utils.generateQuery(null, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              previewOfPdf ->
                  Utils.mapPreviewResponseToDataBlob(
                      previewOfPdf.get(), attachmentFileName, dispositionType));
    }

    if (documentPreviewMatcher.find()) {
      var queryParameters =
          Utils.parseQueryParameters(documentPreviewMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postPreviewOfDocument(
                      attachmentMimePartInputStream,
                      Utils.generateQuery(null, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              previewOfDocument ->
                  Utils.mapPreviewResponseToDataBlob(
                      previewOfDocument.get(), attachmentFileName, dispositionType));
    }

    return Try.failure(ServiceException.INVALID_REQUEST("[" + requestId + "] Cannot handle request", null));
  }


  /**
   * Sends error response for {@link HttpServletRequest} Convert any ServerError or 5xx error to {@link
   * Constants#STATUS_UNPROCESSABLE_ENTITY}
   *
   * @param request  the {@link HttpServletRequest} object
   * @param response the {@link HttpServletResponse} object
   * @param errCode  error code for {@link HttpServletResponse}
   * @param reason   message string for {@link HttpServletResponse}
   */
  void respondWithError(HttpServletRequest request, HttpServletResponse response,
      int errCode, String reason) {
    var requestId = Utils.getRequestIdFromRequest(request);
    LOG.info("[" + requestId + "] Error: Code: " + errCode + ", Reason: " + reason);
    response.setContentType("text/html; charset=UTF-8");
    try {
      if (HttpStatus.isServerError(response.getStatus()) || HttpStatus.isServerError(errCode)) {
        response.sendError(Constants.STATUS_UNPROCESSABLE_ENTITY, reason);
      } else {
        response.sendError(errCode, reason);
      }
    } catch (IOException e) {
      LOG.warn("[" + requestId + "] Failed to send error response. Reason: " + e.getMessage() + ", Exception: " + e);
    }
  }

  /**
   * Sends success response for {@link HttpServletRequest} with the blob we got from preview service
   *
   * @param response the {@link HttpServletResponse} object
   * @param request  the {@link HttpServletRequest} object
   * @param dataBlob the {@link DataBlob} object
   */
  void respondWithSuccess(HttpServletResponse response, HttpServletRequest request, DataBlob dataBlob) {
    response.addHeader(HttpHeaders.CONNECTION, HTTP.CONN_CLOSE);
    response.addHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(dataBlob.getSize()));
    response.addHeader(HttpHeaders.CONTENT_TYPE, dataBlob.getMimeType());
    var dispositionType = dataBlob.getDispositionType();
    var attachmentFilename = dataBlob.getFilename();
    try {
      response.addHeader("content-disposition",
          (dispositionType.startsWith("a") ? "attachment;" : "inline;") + " filename*=UTF-8''" + URLEncoder.encode(
              attachmentFilename, StandardCharsets.UTF_8));
      ByteUtil.copy(dataBlob.getBlobInputStream(), true, response.getOutputStream(), false);
    } catch (Exception e) {
      var requestId = Utils.getRequestIdFromRequest(request);
      LOG.warn("[" + requestId + "] Failed to send success response. Reason: " + e.getMessage() + ", Exception: " + e);
    }
  }
}
