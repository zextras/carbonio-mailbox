package com.zimbra.cs.service.servlet.preview;

import static com.zimbra.cs.service.servlet.preview.PreviewServlet.SERVLET_PATH;
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
import com.zimbra.common.soap.MailConstants;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

  private static final Log LOG = LogFactory.getLog(PreviewHandler.class);
  private static final Pattern requiredQueryParametersPattern = Pattern.compile(
      SERVLET_PATH + "/([a-zA-Z]+)/" + MESSAGE_ID_REGEXP + PART_NUMBER_REGEXP);
  private static final int STATUS_UNPROCESSABLE_ENTITY = 422;
  private final PreviewClient previewClient;
  private final AttachmentService attachmentService;

  public PreviewHandler(PreviewClient previewClient,
      AttachmentService attachmentService) {
    this.previewClient = previewClient;
    this.attachmentService = attachmentService;
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

    var itemIdTry = Try.of(() -> new ItemId(messageId, (String) null));
    if (itemIdTry.isFailure() || itemIdTry.get() == null) {
      respondWithError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Invalid MessageId.");
      return;
    }

    try {
      var itemId = itemIdTry.get();
      if (itemId.isLocal()) {
        handleLocalAttachment(request, response, authTokenTry.get(), messageId, partId);
      } else {
        proxyServletRequest(request, response, itemId.getAccountId());
      }
    } catch (ServiceException | HttpException | IOException e) {
      respondWithError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private void handleLocalAttachment(HttpServletRequest request, HttpServletResponse response,
      AuthToken authToken, String messageId, String partNo) {
    var attachmentTry = getAttachment(authToken, messageId, partNo);
    if (attachmentTry.isFailure() || attachmentTry.get() == null) {
      respondWithError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          attachmentTry.failed().get().getMessage());
      return;
    }

    attachmentTry.flatMap(attachment -> Try.of(() -> getAttachmentPreview(request, attachment)))
        .onSuccess(preview -> respondWithSuccess(response, request, preview.get()))
        .onFailure(BadRequest.class,
            ex -> respondWithError(request, response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage()))
        .onFailure(ItemNotFound.class,
            ex -> respondWithError(request, response, HttpServletResponse.SC_NOT_FOUND, ex.getMessage()))
        .onFailure(ValidationError.class,
            ex -> respondWithError(request, response, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage()))
        .onFailure(ex -> respondWithError(request, response, STATUS_UNPROCESSABLE_ENTITY, ex.getMessage()));
  }

  /**
   * This method is used to get the preview of passed attachment from the preview service based on the requestUrl,
   * calling different endpoints of preview service
   *
   * @param request the {@link HttpServletRequest} object
   * @return the {@link BlobResponseStore} object
   */
  private Try<BlobResponseStore> getAttachmentPreview(
      HttpServletRequest request, BlobResponseStore attachmentMimePart) {

    var requestId = Utils.getRequestIDFromRequest(request);
    var requestUrl = Utils.getFullURLFromRequest(request);
    var dispositionType = Utils.getDispositionTypeFromQueryParams(requestUrl);
    var requestUrlForPreview = Utils.getRequestUrlForPreview(requestUrl, dispositionType);
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
                  mapResponseToBlobResponseStore(
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
                  mapResponseToBlobResponseStore(
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
                  mapResponseToBlobResponseStore(
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
                  mapResponseToBlobResponseStore(
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
                  mapResponseToBlobResponseStore(
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
                  mapResponseToBlobResponseStore(
                      previewOfDocument.get(), attachmentFileName, dispositionType));
    }
    // End Preview API Controller=================================================================

    return Try.failure(ServiceException.INVALID_REQUEST("[" + requestId + "] Cannot handle request", null));
  }

  /**
   * This method is used to map the preview service's {@link BlobResponse} to our {@link BlobResponseStore} object
   *
   * @param response        preview service's {@link BlobResponse}
   * @param fileName        filename that we want to assign to our {@link BlobResponseStore} object
   * @param dispositionType disposition will be: attachment or inline(default)
   * @return mapped {@link BlobResponseStore} object
   */
  private Try<BlobResponseStore> mapResponseToBlobResponseStore(
      BlobResponse response, String fileName, String dispositionType) {
    return Try.of(() -> new BlobResponseStore(
        response.getContent(),
        fileName,
        response.getLength(),
        response.getMimeType(),
        dispositionType));
  }

  /**
   * This method is used to retrieve the attachment from mailbox
   *
   * @param authToken the {@link AuthToken} passed in request
   * @param messageId {@link String} the messageId that we want to get attachment from
   * @param partId    {@link String} the part number of the attachment in email
   * @return the {@link MimePart} object
   */
  private Try<BlobResponseStore> getAttachment(AuthToken authToken, String messageId, String partId) {
    return Try.of(() -> {
      var accountId = authToken.getAccount().getId();

      final var uuidMsgId = messageId.split(":");
      var targetAccountId = accountId;
      var msgId = messageId;

      if (uuidMsgId.length == 2) {
        targetAccountId = uuidMsgId[0];
        msgId = uuidMsgId[1];
      }

      int parsedMsgId;
      try {
        parsedMsgId = Integer.parseInt(msgId);
      } catch (NumberFormatException ex) {
        LOG.error(ex.getMessage());
        throw ServiceException.PARSE_ERROR(MailConstants.A_MESSAGE_ID + " must be an integer.", ex);
      }

      var mimePartTry = attemptToGetAttachment(targetAccountId, parsedMsgId, partId, authToken);
      if (mimePartTry.isFailure() && !targetAccountId.equals(accountId)) {
        mimePartTry = attemptToGetAttachment(accountId, parsedMsgId, partId, authToken);
      }
      if (mimePartTry.isFailure()) {
        throw mimePartTry.getCause();
      }

      var mimePart = mimePartTry.get();
      return new BlobResponseStore(
          mimePart.getInputStream(),
          mimePart.getFileName(),
          (long) mimePart.getSize(),
          mimePart.getContentType(),
          "inline"
      );
    });
  }

  private Try<MimePart> attemptToGetAttachment(String accId, int msgId, String partId, AuthToken authToken) {
    return Try.of(() -> attachmentService.getAttachment(accId, authToken, msgId, partId)
        .getOrElseThrow(e -> {
          if (e instanceof ServiceException) {
            return (ServiceException) e;
          } else {
            return ServiceException.FAILURE(e.getMessage());
          }
        }));
  }

  /**
   * This method is used to send error response for {@link HttpServletRequest}
   *
   * @param request  the {@link HttpServletRequest} object
   * @param response the {@link HttpServletResponse} object
   * @param errCode  error code for {@link HttpServletResponse}
   * @param reason   message string for {@link HttpServletResponse}
   */
  private void respondWithError(HttpServletRequest request, HttpServletResponse response,
      int errCode, String reason) {
    var requestId = Utils.getRequestIDFromRequest(request);
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
      var requestId = Utils.getRequestIDFromRequest(request);
      LOG.warn("[" + requestId + "] Failed to send error response. Reason: " + e.getMessage() + ", Exception: " + e);
    }
  }
}
