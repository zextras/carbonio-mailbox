package com.zimbra.cs.service.servlet.preview;

import static com.zimbra.cs.service.servlet.preview.PreviewServlet.SERVLET_PATH;
import static com.zimbra.cs.service.servlet.preview.Utils.REQUEST_ID_KEY;
import static com.zimbra.cs.service.servlet.preview.Utils.REQUEST_PARAM_DISP;
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

public class PreviewHandler {

  public static final String PART_NUMBER_REGEXP = "([0-9.]+(?:\\.[0-9.]+)?)";
  public static final String MESSAGE_ID_REGEXP = "([a-zA-Z\\-:0-9]+|[0-9]+)/";
  public static final String THUMBNAIL_REGEXP =
      MESSAGE_ID_REGEXP + PART_NUMBER_REGEXP + "/([0-9]*x[0-9]*)/thumbnail/?\\??(.*)";
  public static final String PDF_THUMBNAIL_REGEX = SERVLET_PATH + "/pdf/" + THUMBNAIL_REGEXP;
  public static final String IMG_THUMBNAIL_REGEX = SERVLET_PATH + "/image/" + THUMBNAIL_REGEXP;
  public static final String DOC_THUMBNAIL_REGEX = SERVLET_PATH + "/document/" + THUMBNAIL_REGEXP;
  public static final String PDF_PREVIEW_REGEX =
      SERVLET_PATH
          + "/pdf/"
          + MESSAGE_ID_REGEXP
          + PART_NUMBER_REGEXP
          + "/?((?=(?!thumbnail))(?=([^/ ]*)))";
  public static final String IMG_PREVIEW_REGEX =
      SERVLET_PATH
          + "/image/"
          + MESSAGE_ID_REGEXP
          + PART_NUMBER_REGEXP
          + "/([0-9]*x[0-9]*)/?((?=(?!thumbnail))(?=([^/"
          + " ]*)))";
  public static final String DOC_PREVIEW_REGEX =
      SERVLET_PATH
          + "/document/"
          + MESSAGE_ID_REGEXP
          + PART_NUMBER_REGEXP
          + "/?((?=(?!thumbnail))(?=([^/ ]*)))";
  public static final int STATUS_UNPROCESSABLE_ENTITY = 422;
  private static final Log LOG = LogFactory.getLog(PreviewHandler.class);
  private static final Pattern requiredQueryParametersPattern = Pattern.compile(
      SERVLET_PATH + "/([a-zA-Z]+)/" + MESSAGE_ID_REGEXP + PART_NUMBER_REGEXP);
  private final PreviewClient previewClient;
  private final AttachmentService attachmentService;
  private final ItemIdFactory itemIdFactory;

  public PreviewHandler(PreviewClient previewClient,
      AttachmentService attachmentService, ItemIdFactory itemIdFactory) {
    this.previewClient = previewClient;
    this.attachmentService = attachmentService;
    this.itemIdFactory = itemIdFactory;
  }

  public void handle(HttpServletRequest request, HttpServletResponse response) {
    if (!previewClient.healthReady()) {
      respondWithError(request, response, STATUS_UNPROCESSABLE_ENTITY,
          "Preview service is down or not available to take request");
      return;
    }

    var authTokenTry = Try.of(() -> ZimbraServlet.getAuthTokenFromCookie(request, response));
    if (authTokenTry.isFailure() || authTokenTry.get() == null) {
      respondWithError(request, response, HttpServletResponse.SC_UNAUTHORIZED,
          "Authentication required. Request missing authentication token.");
      return;
    }

    var requiredQueryParametersMatcher = requiredQueryParametersPattern.matcher(Utils.getFullURLFromRequest(request));
    if (!requiredQueryParametersMatcher.find() || requiredQueryParametersMatcher.groupCount() != 3) {
      respondWithError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
      return;
    }

    var messageId = requiredQueryParametersMatcher.group(2);
    var partId = requiredQueryParametersMatcher.group(3);

    var itemIdTry = Try.of(() -> Utils.getItemIdFromMessageId(itemIdFactory, messageId, authTokenTry.get()));
    if (itemIdTry.isFailure() || itemIdTry.get() == null) {
      respondWithError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Invalid MessageId.");
      return;
    }

    try {
      var itemId = itemIdTry.get();
      if (itemId.isLocal()) {
        handleLocalAttachment(request, response, authTokenTry.get(), itemId, partId);
      } else {
        proxyRequestToTargetMailHost(request, response, itemId.getAccountId());
      }
    } catch (ServiceException | HttpException | IOException e) {
      respondWithError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  /**
   * Handles the processing of a local attachment preview request.
   *
   * <p>This method retrieves an attachment based on the provided {@link ItemId} and {@code partId}.
   * If the attachment is found, it is mapped to a {@link BlobRequestStore} and further processed to generate an
   * attachment preview. The method then responds with the appropriate status and information based on the success or
   * failure of the processing.</p>
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
      respondWithError(request, response, HttpServletResponse.SC_NOT_FOUND,
          mimePartTry.getCause().getMessage());
      return;
    }

    mimePartTry.mapTry(Utils::mapMimePartToBlobRequestStore)
        .flatMapTry(blobRequestStore -> getAttachmentPreview(request, blobRequestStore))
        .onSuccess(blobPreviewResponseStore -> respondWithSuccess(response, request, blobPreviewResponseStore))
        .onFailure(BadRequest.class,
            ex -> respondWithError(request, response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage()))
        .onFailure(ItemNotFound.class,
            ex -> respondWithError(request, response, HttpServletResponse.SC_NOT_FOUND, ex.getMessage()))
        .onFailure(ValidationError.class,
            ex -> respondWithError(request, response, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage()))
        .onFailure(ex -> respondWithError(request, response, STATUS_UNPROCESSABLE_ENTITY, ex.getMessage()));
  }

  /**
   * Proxies an HTTP request to the mail host associated with the specified target account.
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
        Map.of(REQUEST_ID_KEY, requestId != null ? requestId : "N/A"));
  }

  /**
   * This method is used to get the preview of passed attachment from the preview service based on the requestUrl,
   * calling different endpoints of preview service
   *
   * @param request            the {@link HttpServletRequest} object
   * @param attachmentMimePart the {@link BlobRequestStore} object
   * @return the {@link BlobResponseStore} object
   */
  Try<BlobResponseStore> getAttachmentPreview(
      HttpServletRequest request, BlobRequestStore attachmentMimePart) {
    var requestId = Utils.getRequestIdFromRequest(request);
    var requestUrl = Utils.getFullURLFromRequest(request);
    var dispositionType = Utils.getDispositionTypeFromQueryParams(requestUrl);
    var requestUrlForPreview = Utils.removeQueryParams(requestUrl, List.of(REQUEST_PARAM_DISP, REQUEST_ID_KEY));
    var attachmentFileName = Try.of(attachmentMimePart::getFilename).getOrElse("unknown");
    var attachmentMimePartInputStream = attachmentMimePart.getBlobInputStream();

    // Start Preview API Controller=================================================================
    var imagePreviewMatcher =
        Pattern.compile(IMG_PREVIEW_REGEX).matcher(requestUrlForPreview);

    var imageThumbnailMatcher =
        Pattern.compile(IMG_THUMBNAIL_REGEX).matcher((requestUrlForPreview));

    var pdfPreviewMatcher =
        Pattern.compile(PDF_PREVIEW_REGEX).matcher((requestUrlForPreview));

    var pdfThumbnailMatcher =
        Pattern.compile(PDF_THUMBNAIL_REGEX).matcher((requestUrlForPreview));

    var documentPreviewMatcher =
        Pattern.compile(DOC_PREVIEW_REGEX).matcher(requestUrlForPreview);

    var documentThumbnailMatcher =
        Pattern.compile(DOC_THUMBNAIL_REGEX).matcher((requestUrlForPreview));

    // Handle Image thumbnail request
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
                  Utils.mapResponseToBlobResponseStore(
                      thumbnailOfImage.get(), attachmentFileName, dispositionType));
    }

    // Handle PDF thumbnail request
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
                  Utils.mapResponseToBlobResponseStore(
                      thumbnailOfPdf.get(), attachmentFileName, dispositionType));
    }

    // Handle Document thumbnail request
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
                  Utils.mapResponseToBlobResponseStore(
                      thumbnailOfDocument.get(), attachmentFileName, dispositionType));
    }

    // Handle Preview Image request
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
                  Utils.mapResponseToBlobResponseStore(
                      previewOfImage.get(), attachmentFileName, dispositionType));
    }

    // Handle Preview PDF request
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
                  Utils.mapResponseToBlobResponseStore(
                      previewOfPdf.get(), attachmentFileName, dispositionType));
    }

    // Handle Preview Document request
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
                  Utils.mapResponseToBlobResponseStore(
                      previewOfDocument.get(), attachmentFileName, dispositionType));
    }
    // End Preview API Controller=================================================================

    return Try.failure(ServiceException.INVALID_REQUEST("[" + requestId + "] Cannot handle request", null));
  }


  /**
   * This method is used to send error response for {@link HttpServletRequest}
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
        response.sendError(STATUS_UNPROCESSABLE_ENTITY, reason);
      } else {
        response.sendError(errCode, reason);
      }
    } catch (IOException e) {
      LOG.warn("[" + requestId + "] Failed to send error response.", e);
    }
  }

  /**
   * This method is used to send success response for {@link HttpServletRequest} with the blob we got from preview
   * service
   *
   * @param response          the {@link HttpServletResponse} object
   * @param request           the {@link HttpServletRequest} object
   * @param blobResponseStore the {@link BlobResponseStore} object
   */
  void respondWithSuccess(HttpServletResponse response, HttpServletRequest request,
      BlobResponseStore blobResponseStore) {
    response.addHeader(HttpHeaders.CONNECTION, HTTP.CONN_CLOSE);
    response.addHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(blobResponseStore.getSize()));
    response.addHeader(HttpHeaders.CONTENT_TYPE, blobResponseStore.getMimeType());
    var dispositionType = blobResponseStore.getDispositionType();
    var attachmentFilename = blobResponseStore.getFilename();
    try {
      response.addHeader(
          "content-disposition",
          (dispositionType.startsWith("a") ? "attachment;" : "inline;")
              + " filename*=UTF-8''"
              + URLEncoder.encode(attachmentFilename, StandardCharsets.UTF_8));
      ByteUtil.copy(blobResponseStore.getBlobInputStream(), true, response.getOutputStream(), false);
    } catch (Exception e) {
      var requestId = Utils.getRequestIdFromRequest(request);
      LOG.warn("[" + requestId + "] Failed to send error response. Reason: " + e.getMessage() + ", Exception: " + e);
    }
  }
}
