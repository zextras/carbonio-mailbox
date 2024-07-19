package com.zimbra.cs.service.servlet.preview;

import static com.zimbra.cs.service.servlet.preview.PreviewServlet.SERVLET_PATH;
import static com.zimbra.cs.service.servlet.preview.Utils.REQUEST_ID_KEY;
import static com.zimbra.cs.service.servlet.preview.Utils.REQUEST_PARAM_DISP;
import static com.zimbra.cs.servlet.ZimbraServlet.proxyServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.exceptions.BadRequest;
import com.zextras.carbonio.preview.exceptions.ItemNotFound;
import com.zextras.carbonio.preview.exceptions.ValidationError;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.carbonio.preview.queries.Query;
import com.zextras.carbonio.preview.queries.Query.QueryBuilder;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import javax.mail.internet.MimePart;
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

  /**
   * This method generates the final query {@link Query} from passed Optional area string and {@link
   * PreviewQueryParameters}
   *
   * @param optArea         the optional area {@link String} parameter
   * @param queryParameters the {@link PreviewQueryParameters} object
   * @return {@link Query}
   */
  private static Query generateQuery(String optArea, PreviewQueryParameters queryParameters) {
    var parameterBuilder = new QueryBuilder();
    if (optArea != null) {
      parameterBuilder.setPreviewArea(optArea);
    }
    queryParameters.getQuality().ifPresent(parameterBuilder::setQuality);
    queryParameters.getOutputFormat().ifPresent(parameterBuilder::setOutputFormat);
    queryParameters.getCrop().ifPresent(parameterBuilder::setCrop);
    queryParameters.getShape().ifPresent(parameterBuilder::setShape);
    queryParameters.getFirstPage().ifPresent(parameterBuilder::setFirstPage);
    queryParameters.getLastPage().ifPresent(parameterBuilder::setLastPage);
    return parameterBuilder.build();
  }


  /**
   * This method parses the passed queryParamString into {@link PreviewQueryParameters} object
   *
   * @param queryParameters {@link String}
   * @return {@link PreviewQueryParameters}
   */
  private static PreviewQueryParameters parseQueryParameters(String queryParameters) {
    var parameters =
        Arrays.stream(queryParameters.replace("?", "").split("&"))
            .map(parameter -> parameter.split("="))
            .filter(parameter -> parameter.length == 2)
            .collect(Collectors.toMap(parameter -> parameter[0], parameter -> parameter[1]));
    return new ObjectMapper().convertValue(parameters, PreviewQueryParameters.class);
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

  void proxyRequestToTargetMailHost(HttpServletRequest request, HttpServletResponse response, String targetAccountId)
      throws IOException, ServiceException, HttpException {
    var requestId = Utils.getRequestIdFromRequest(request);
    LOG.info("[" + requestId + "] Proxying request to target account(" + targetAccountId + ")'s mail host.");
    proxyServletRequest(request, response, targetAccountId,
        Map.of(REQUEST_ID_KEY, requestId != null ? requestId : "N/A"));
  }

  private void handleLocalAttachment(HttpServletRequest request, HttpServletResponse response,
      AuthToken authToken, ItemId itemId, String partId) {
    var mimePartTry = attachmentService.getAttachment(itemId.getAccountId(), authToken, itemId.getId(), partId);
    if (mimePartTry.isFailure() || mimePartTry.get() == null) {
      respondWithError(request, response, HttpServletResponse.SC_NOT_FOUND,
          mimePartTry.getCause().getMessage());
      return;
    }

    mimePartTry.mapTry(this::mapMimePartToBlobRequestStore)
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

  private BlobRequestStore mapMimePartToBlobRequestStore(MimePart mimePart) throws MessagingException, IOException {
    return new BlobRequestStore(
        mimePart.getInputStream(),
        mimePart.getFileName(),
        (long) mimePart.getSize(),
        mimePart.getContentType(),
        "inline"
    );
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
          parseQueryParameters(imageThumbnailMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postThumbnailOfImage(
                      attachmentMimePartInputStream,
                      generateQuery(previewArea, queryParameters),
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
          parseQueryParameters(pdfThumbnailMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postThumbnailOfPdf(
                      attachmentMimePartInputStream,
                      generateQuery(previewArea, queryParameters),
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
          parseQueryParameters(documentThumbnailMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postThumbnailOfDocument(
                      attachmentMimePartInputStream,
                      generateQuery(previewArea, queryParameters),
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
          parseQueryParameters(imagePreviewMatcher.group(5));
      return Try.of(
              () ->
                  previewClient.postPreviewOfImage(
                      attachmentMimePartInputStream,
                      generateQuery(previewArea, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              previewOfImage ->
                  Utils.mapResponseToBlobResponseStore(
                      previewOfImage.get(), attachmentFileName, dispositionType));
    }

    // Handle Preview PDF request
    if (pdfPreviewMatcher.find()) {
      var queryParameters =
          parseQueryParameters(pdfPreviewMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postPreviewOfPdf(
                      attachmentMimePartInputStream,
                      generateQuery(null, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              previewOfPdf ->
                  Utils.mapResponseToBlobResponseStore(
                      previewOfPdf.get(), attachmentFileName, dispositionType));
    }

    // Handle Preview Document request
    if (documentPreviewMatcher.find()) {
      var queryParameters =
          parseQueryParameters(documentPreviewMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postPreviewOfDocument(
                      attachmentMimePartInputStream,
                      generateQuery(null, queryParameters),
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
