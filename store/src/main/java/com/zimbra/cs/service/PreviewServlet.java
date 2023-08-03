package com.zimbra.cs.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.carbonio.preview.exceptions.BadRequest;
import com.zextras.carbonio.preview.exceptions.ItemNotFound;
import com.zextras.carbonio.preview.exceptions.ValidationError;
import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zextras.carbonio.preview.queries.Query;
import com.zextras.carbonio.preview.queries.Query.QueryBuilder;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.mime.ContentDisposition;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.L10nUtil.MsgKey;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.httpclient.HttpProxyUtil;
import com.zimbra.cs.servlet.ZimbraServlet;
import com.zimbra.cs.util.AccountUtil;
import io.vavr.control.Try;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.mail.internet.MimePart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.eclipse.jetty.http.HttpStatus;

/**
 * The preview service servlet - serves preview for requested mail attachments using Carbonio
 * previewer service
 *
 * <pre>
 *
 *   Based on Carbonio Preview SDK 0.2.4
 *
 *   The API is the almost same as of preview service(https://zextras.atlassian.net/wiki/spaces/SW/pages/2353430753/Preview+API)
 *   with few modification that let us make it use as preview service for mailbox attachments.
 *
 *   itemId, partNo, disposition(disp) are notable new parameters, their usage can be found in the URL given below:
 *
 *   https://nbm-s01.demo.zextras.io/service/preview/{format}/{itemId}/{partNo}/{area}/{thumbnail}/?[{query-params}]
 *
 *          Path parameters:
 *                    format  =  image | pdf | document
 *                    itemId  =  mail-item-id
 *                    partNo  =  mail-item-part-number
 *                      area  =  width of the output image (>=0) x height of the output image (>=0),
 *                               width x height => 100x200. The first is width, the latter height, the order is important!
 *                 thumbnail  =  omit for full preview type
 *                               'thumbnail' if requesting the preview type
 *                                thumbnail
 *
 *          Query parameters:
 *                      disp  =  attachment(a) | inline(i)
 *                                  Default value : inline(i)
 *                     shape  =  rounded | rectangular
 *                                  Default value : rectangular
 *                   quality  =  lowest | low | medium | high | highest
 *                                  Default value : medium
 *             output_format  =  jpeg | png | gif; default inline(jpeg)
 *                                  Default value : jpeg
 *                      crop  =  True will crop the picture starting from the borders
 *                               This option will lose information, leaving it False will scale and have borders to fill the requested size.
 *                                  Default value : false
 *                first_page  =  integer value of first page to preview (n>=1)
 *                                  Default value : 1
 *                 last_page  =  integer value of last page to preview (0 = last of the pdf/document)
 *                                  Default value : 0
 *
 *            Authentication  =  expects ZM_AUTH_TOKEN cookie passed in the request
 *                               headers
 *
 * </pre>
 *
 * @author keshavbhatt
 */
public class PreviewServlet extends ZimbraServlet {

  public static final String SERVLET_PATH = "/preview";
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
  private static final long serialVersionUID = -4834966842520538743L;
  private static final Log LOG = LogFactory.getLog(PreviewServlet.class);
  private static final String PREVIEW_SERVICE_BASE_URL = "http://127.78.0.7:20001/";
  private static final PreviewClient previewClient = PreviewClient.atURL(PREVIEW_SERVICE_BASE_URL);

  /**
   * removes the disposition query parameter from the url
   *
   * @param requestUrl the requestUrl ({@link String})
   * @param dispositionType disposition type ({@link String})
   * @return Request Url for Preview ({@link String})
   */
  static String getRequestUrlForPreview(String requestUrl, String dispositionType) {
    final String dispQueryParam = "\\?disp=" + dispositionType;
    final List<String> possibleDisposition =
        Arrays.asList(dispQueryParam, "\\&disp=" + dispositionType);

    return possibleDisposition.stream()
        .reduce(
            requestUrl,
            (str, toRem) ->
                str.replaceAll(
                        toRem.contains("\\?") ? dispQueryParam + "&" : toRem,
                        toRem.contains("\\?") ? "\\?" : "")
                    .replaceAll(toRem.contains("\\?") ? dispQueryParam : toRem, ""));
  }

  /**
   * Returns the value of disposition type requested in URL's query parameter
   *
   * @param requestUrl the request URL
   * @return disposition value if found else the default "i"(inline)
   */
  static String getDispositionType(String requestUrl) {
    if (requestUrl.split("\\?").length > 1) {
      return Stream.of(requestUrl.split("\\?")[1].split("&"))
          .map(kv -> kv.split("="))
          .filter(kv -> "disp".equalsIgnoreCase(kv[0]))
          .map(kv -> kv[1])
          .findFirst()
          .orElse("i");
    } else {
      return "i";
    }
  }

  /**
   * Return the rest resource Url for content servlet
   *
   * @param authToken the {@link AuthToken} passed in request
   * @param messageId {@link String} the messageId that we want to get attachment from
   * @param part {@link String} the part number of the attachment in email
   * @return Try of attachment's URL {@link String} for content servlet
   */
  static String getContentServletResourceUrl(AuthToken authToken, String messageId, String part) {

    return Try.of(
            () -> {
              final Account account = authToken.getAccount();
              final String baseUrl = AccountUtil.getBaseUri(authToken.getAccount());
              final String restUrl =
                  UserServlet.getRestUrl(account) + "?auth=co&id=" + messageId + "&part=" + part;
              return baseUrl == null
                  ? restUrl
                  : baseUrl + UserServlet.SERVLET_PATH + restUrl.split(UserServlet.SERVLET_PATH)[1];
            })
        .get();
  }

  /**
   * This method generates the final query {@link Query} from passed Optional area string and {@link
   * PreviewQueryParameters}
   *
   * @param optArea the optional area {@link String} parameter
   * @param queryParameters the {@link PreviewQueryParameters} object
   * @return {@link Query}
   */
  static Query generateQuery(String optArea, PreviewQueryParameters queryParameters) {
    final QueryBuilder parameterBuilder = new QueryBuilder();
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
  static PreviewQueryParameters parseQueryParameters(String queryParameters) {
    final Map<String, String> parameters =
        Arrays.stream(queryParameters.replace("?", "").split("&"))
            .map(parameter -> parameter.split("="))
            .filter(parameter -> parameter.length == 2)
            .collect(Collectors.toMap(parameter -> parameter[0], parameter -> parameter[1]));
    return new ObjectMapper().convertValue(parameters, PreviewQueryParameters.class);
  }

  /**
   * @param authToken {@link AuthToken} authToken object
   * @return the mailHostUrl {@link String}
   */
  private String getMailHostUrl(AuthToken authToken) {
    return Try.of(authToken::getAccount)
        .mapTry(account -> account.getAttr(ZAttrProvisioning.A_zimbraMailHost))
        .get();
  }

  /**
   * This method is used to retrieve the attachment from mailbox
   *
   * @param authToken the {@link AuthToken} passed in request
   * @param messageId {@link String} the messageId that we want to get attachment from
   * @param part {@link String} the part number of the attachment in email
   * @return the {@link MimePart} object
   */
  private Try<BlobResponseStore> getAttachment(AuthToken authToken, String messageId, String part) {
    return Try.of(
        () -> {
          HttpClientBuilder clientBuilder =
              ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient();
          HttpProxyUtil.configureProxy(clientBuilder);
          HttpGet getRequest =
              new HttpGet(getContentServletResourceUrl(authToken, messageId, part));
          HttpClient client =
              encodeClientBuilderRequest(authToken, clientBuilder, getRequest).get();
          HttpResponse httpResp = HttpClientUtil.executeMethod(client, getRequest);
          int statusCode = httpResp.getStatusLine().getStatusCode();
          if (statusCode != org.apache.http.HttpStatus.SC_OK) {
            throw ServiceException.FAILURE("Cannot get attachment", null);
          } else {
            Header ctHeader = httpResp.getFirstHeader("Content-Type");
            String contentType = ctHeader == null ? "text/plain" : ctHeader.getValue();
            Header cdHeader = httpResp.getFirstHeader("Content-Disposition");
            String filename =
                cdHeader == null
                    ? "unknown"
                    : new ContentDisposition(cdHeader.getValue()).getParameter("filename");

            return new BlobResponseStore(
                httpResp.getEntity().getContent(),
                filename,
                httpResp.getEntity().getContentLength(),
                contentType,
                "inline");
          }
        });
  }

  /**
   * Adds request configuration to client builder
   *
   * @param authToken the {@link AuthToken} passed in request
   * @param clientBuilder the {@link HttpClientBuilder} object
   * @param getRequest the {@link HttpServletRequest} that has to be encoded
   * @return Try of encoded {@link CloseableHttpClient} object
   */
  private Try<CloseableHttpClient> encodeClientBuilderRequest(
      AuthToken authToken, HttpClientBuilder clientBuilder, HttpGet getRequest) {

    return Try.of(
        () -> {
          authToken.encode(clientBuilder, getRequest, false, getMailHostUrl(authToken));
          return clientBuilder.build();
        });
  }

  /**
   * This method is used to get the preview of passed attachment from the preview service based on
   * the requestUrl, calling different endpoints of preview service
   *
   * @param requestUrl the {@link String} requestUrl
   * @return the {@link BlobResponseStore} object
   */
  private Try<BlobResponseStore> getAttachmentPreview(
      String requestUrl, BlobResponseStore attachmentMimePart) {

    // get disposition type query parameter from url
    final String dispositionType = getDispositionType(requestUrl);

    // get clean requestUrl for preview
    final String requestUrlForPreview = getRequestUrlForPreview(requestUrl, dispositionType);

    // get attachment filename from attachment MimePart
    final String attachmentFileName = Try.of(attachmentMimePart::getFilename).getOrElse("unknown");

    // get attachment inputStream
    final Try<InputStream> attachmentMimePartInputStream =
        Try.of(attachmentMimePart::getBlobStream);

    // break if attachmentMimePartInputStream has encountered a failure
    if (attachmentMimePartInputStream.isFailure()) {
      return Try.failure(
          ServiceException.FAILURE(
              "Cannot process attachment", attachmentMimePartInputStream.getCause()));
    }

    // Start Preview API Controller=================================================================

    final Matcher imagePreviewMatcher =
        Pattern.compile(IMG_PREVIEW_REGEX).matcher(requestUrlForPreview);

    final Matcher imageThumbnailMatcher =
        Pattern.compile(IMG_THUMBNAIL_REGEX).matcher((requestUrlForPreview));

    final Matcher pdfPreviewMatcher =
        Pattern.compile(PDF_PREVIEW_REGEX).matcher((requestUrlForPreview));

    final Matcher pdfThumbnailMatcher =
        Pattern.compile(PDF_THUMBNAIL_REGEX).matcher((requestUrlForPreview));

    final Matcher documentPreviewMatcher =
        Pattern.compile(DOC_PREVIEW_REGEX).matcher(requestUrlForPreview);

    final Matcher documentThumbnailMatcher =
        Pattern.compile(DOC_THUMBNAIL_REGEX).matcher((requestUrlForPreview));

    // Handle Image thumbnail request
    if (imageThumbnailMatcher.find()) {
      final String previewArea = imageThumbnailMatcher.group(3);
      final PreviewQueryParameters queryParameters =
          parseQueryParameters(imageThumbnailMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postThumbnailOfImage(
                      attachmentMimePartInputStream.get(),
                      generateQuery(previewArea, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              thumbnailOfImage ->
                  mapResponseToBlobResponseStore(
                      thumbnailOfImage.get(), attachmentFileName, dispositionType));
    }

    // Handle PDF thumbnail request
    if (pdfThumbnailMatcher.find()) {
      final String previewArea = pdfThumbnailMatcher.group(3);
      final PreviewQueryParameters queryParameters =
          parseQueryParameters(pdfThumbnailMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postThumbnailOfPdf(
                      attachmentMimePartInputStream.get(),
                      generateQuery(previewArea, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              thumbnailOfPdf ->
                  mapResponseToBlobResponseStore(
                      thumbnailOfPdf.get(), attachmentFileName, dispositionType));
    }

    // Handle Document thumbnail request
    if (documentThumbnailMatcher.find()) {
      final String previewArea = documentThumbnailMatcher.group(3);
      final PreviewQueryParameters queryParameters =
          parseQueryParameters(documentThumbnailMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postThumbnailOfDocument(
                      attachmentMimePartInputStream.get(),
                      generateQuery(previewArea, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              thumbnailOfDocument ->
                  mapResponseToBlobResponseStore(
                      thumbnailOfDocument.get(), attachmentFileName, dispositionType));
    }

    // Handle Preview Image request
    if (imagePreviewMatcher.find()) {
      final String previewArea = imagePreviewMatcher.group(3);
      final PreviewQueryParameters queryParameters =
          parseQueryParameters(imagePreviewMatcher.group(5));
      return Try.of(
              () ->
                  previewClient.postPreviewOfImage(
                      attachmentMimePartInputStream.get(),
                      generateQuery(previewArea, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              previewOfImage ->
                  mapResponseToBlobResponseStore(
                      previewOfImage.get(), attachmentFileName, dispositionType));
    }

    // Handle Preview PDF request
    if (pdfPreviewMatcher.find()) {
      final PreviewQueryParameters queryParameters =
          parseQueryParameters(pdfPreviewMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postPreviewOfPdf(
                      attachmentMimePartInputStream.get(),
                      generateQuery(null, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              previewOfPdf ->
                  mapResponseToBlobResponseStore(
                      previewOfPdf.get(), attachmentFileName, dispositionType));
    }

    // Handle Preview Document request
    if (documentPreviewMatcher.find()) {
      final PreviewQueryParameters queryParameters =
          parseQueryParameters(documentPreviewMatcher.group(4));
      return Try.of(
              () ->
                  previewClient.postPreviewOfDocument(
                      attachmentMimePartInputStream.get(),
                      generateQuery(null, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              previewOfDocument ->
                  mapResponseToBlobResponseStore(
                      previewOfDocument.get(), attachmentFileName, dispositionType));
    }
    // End Preview API Controller=================================================================

    // return failure if controller reached the end
    return Try.failure(ServiceException.INVALID_REQUEST("Cannot handle request", null));
  }

  /**
   * This method is used to map the preview service's {@link BlobResponse} to our {@link
   * BlobResponseStore} object
   *
   * @param response preview service's {@link BlobResponse}
   * @param fileName filename that we want to assign to our {@link BlobResponseStore} object
   * @param dispositionType disposition will be: attachment or inline(default)
   * @return mapped {@link BlobResponseStore} object
   */
  private Try<BlobResponseStore> mapResponseToBlobResponseStore(
      BlobResponse response, String fileName, String dispositionType) {
    return Try.success(
        new BlobResponseStore(
            response.getContent(),
            fileName,
            response.getLength(),
            response.getMimeType(),
            dispositionType));
  }

  /**
   * This method is used to reconstruct the URL from {@link HttpServletRequest}, complete with query
   * string (if any)
   *
   * @param request the {@link HttpServletRequest} object
   * @return {@link String} complete URL (
   *     <pre> protocol + servername + port + path + query </pre>
   *     )
   */
  String getUrlWithQueryParams(final HttpServletRequest request) {
    StringBuffer url = request.getRequestURL();
    final String queryString = request.getQueryString();
    if (queryString != null) {
      url.append("?").append(queryString);
    }
    return url.toString();
  }

  /**
   * This method is used to send success response for {@link HttpServletRequest} with the blob we
   * got from preview service
   *
   * @param resp the {@link HttpServletResponse} object
   * @param blobResponseStore the {@link BlobResponseStore} object
   */
  void respondWithSuccess(HttpServletResponse resp, BlobResponseStore blobResponseStore) {
    resp.addHeader(HttpHeaders.CONNECTION, HTTP.CONN_CLOSE);
    resp.addHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(blobResponseStore.getSize()));
    resp.addHeader(HttpHeaders.CONTENT_TYPE, blobResponseStore.getMimeType());
    final String dispositionType = blobResponseStore.getDispositionType();
    final String attachmentFilename = blobResponseStore.getFilename();
    try {
      resp.addHeader(
          "content-disposition",
          (dispositionType.startsWith("a") ? "attachment;" : "inline;")
              + " filename*=UTF-8''"
              + URLEncoder.encode(attachmentFilename, StandardCharsets.UTF_8));
      ByteUtil.copy(blobResponseStore.getBlobStream(), true, resp.getOutputStream(), false);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * This method is used to send error response for {@link HttpServletRequest}
   *
   * @param resp the {@link HttpServletResponse} object
   * @param errCode error code for {@link HttpServletResponse}
   * @param reason message string for {@link HttpServletResponse}
   */
  void respondWithError(HttpServletResponse resp, int errCode, String reason) {
    resp.setContentType("text/html; charset=UTF-8");
    try {
      sendError(resp, errCode, reason);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * This override method is to proxy 5xx error with 404
   *
   * @param resp the {@link HttpServletResponse} object
   * @param errCode error code for {@link HttpServletResponse}
   * @param reason message string for {@link HttpServletResponse}
   * @throws IOException while sendError
   */
  void sendError(HttpServletResponse resp, int errCode, String reason) throws IOException {
    if (HttpStatus.isServerError(resp.getStatus()) || HttpStatus.isServerError(errCode)) {
      LOG.error("An internal server error occurred, user was responded with 404");
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, reason);
    } else {
      resp.sendError(errCode, reason);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    ZimbraLog.clearContext();
    addRemoteIpToLoggingContext(req);

    // respond with error instantly if preview service is not ready
    if (!previewClient.healthReady()) {
      respondWithError(
          resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Preview service is down/not ready");
    }

    final AuthToken authToken = getAuthTokenFromCookieOrRespondWithError(req, resp);
    if (authToken == null) {
      respondWithError(
          resp,
          HttpServletResponse.SC_UNAUTHORIZED,
          L10nUtil.getMessage(MsgKey.errMustAuthenticate, req));
    }

    final Pattern requiredQueryParametersPattern =
        Pattern.compile(SERVLET_PATH + "/([a-zA-Z]+)/" + MESSAGE_ID_REGEXP + PART_NUMBER_REGEXP);

    final Matcher requiredQueryParametersMatcher =
        requiredQueryParametersPattern.matcher(getUrlWithQueryParams(req));

    // check url for the presence of required parameters and query string
    // send error otherwise
    if (!requiredQueryParametersMatcher.find()
        || requiredQueryParametersMatcher.groupCount() != 3) {
      respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
    } else {
      final String messageId = requiredQueryParametersMatcher.group(2);
      final String partNo = requiredQueryParametersMatcher.group(3);

      // get attachment
      final Try<BlobResponseStore> attachmentMimePart = getAttachment(authToken, messageId, partNo);
      if (attachmentMimePart.isFailure() || attachmentMimePart.get() == null) {
        respondWithError(
            resp,
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            attachmentMimePart.failed().get().getMessage());
      }

      // send preview response or commit preview api error as response
      Try.of(() -> getAttachmentPreview(getUrlWithQueryParams(req), attachmentMimePart.get()))
          .onSuccess(previewOfAttachment -> respondWithSuccess(resp, previewOfAttachment.get()))
          .onFailure(
              BadRequest.class,
              ex -> respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage()))
          .onFailure(
              ItemNotFound.class,
              ex -> respondWithError(resp, HttpServletResponse.SC_NOT_FOUND, ex.getMessage()))
          .onFailure(
              ValidationError.class,
              ex -> respondWithError(resp, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage()))
          .onFailure(
              ex ->
                  respondWithError(
                      resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
  }

  /**
   * @param req the {@link HttpServletRequest} object that will be used to provide req metadata in
   *     error message
   * @param resp the {@link HttpServletResponse} to send the error response
   * @return the {@link AuthToken} AuthToken object extracted from req metadata
   */
  AuthToken getAuthTokenFromCookieOrRespondWithError(
      HttpServletRequest req, HttpServletResponse resp) {
    return Try.of(() -> getAuthTokenFromCookie(req, resp))
        .onFailure(
            ex ->
                respondWithError(
                    resp,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    L10nUtil.getMessage(MsgKey.errMustAuthenticate, req)))
        .get();
  }

  @Override
  public void init() throws ServletException {
    LOG.info("Servlet " + getServletName() + " starting up");
    super.init();
  }

  @Override
  public void destroy() {
    LOG.info("Servlet " + getServletName() + " shutting down");
    super.destroy();
  }
}

/**
 * {@link PreviewQueryParameters} class is used to map the url parameter as java object and form a
 * final {@link Query} object for preview service
 */
class PreviewQueryParameters {

  @JsonProperty("quality")
  private Quality quality;

  @JsonProperty("output_format")
  private Format outputFormat;

  @JsonProperty("crop")
  private Boolean crop;

  @JsonProperty("shape")
  private Shape shape;

  @JsonProperty("first_page")
  private Integer firstPage;

  @JsonProperty("last_page")
  private Integer lastPage;

  public PreviewQueryParameters(Quality quality, Format outputFormat, Shape shape) {
    this.quality = quality;
    this.outputFormat = outputFormat;
    this.shape = shape;
    this.crop = false;
    this.firstPage = 0;
    this.lastPage = 0;
  }

  @SuppressWarnings("unused") // unused but required for testing
  public PreviewQueryParameters() {}

  public Optional<String> getQuality() {
    return Optional.ofNullable(quality == null ? null : quality.name());
  }

  public Optional<String> getOutputFormat() {
    return Optional.ofNullable(outputFormat == null ? null : outputFormat.name());
  }

  public Optional<Boolean> getCrop() {
    return Optional.ofNullable(crop);
  }

  public Optional<String> getShape() {
    return Optional.ofNullable(shape == null ? null : shape.name());
  }

  public Optional<Integer> getFirstPage() {
    return Optional.ofNullable(firstPage);
  }

  public Optional<Integer> getLastPage() {
    return Optional.ofNullable(lastPage);
  }

  enum Quality {
    @JsonProperty("lowest")
    LOWEST,

    @JsonProperty("low")
    LOW,

    @JsonProperty("medium")
    MEDIUM,

    @JsonProperty("high")
    HIGH,

    @JsonProperty("highest")
    HIGHEST
  }

  enum Format {
    @JsonProperty("jpeg")
    JPEG,

    @JsonProperty("png")
    PNG,
    @JsonProperty("gif")
    GIF
  }

  enum Shape {
    @JsonProperty("rounded")
    ROUNDED,

    @JsonProperty("rectangular")
    RECTANGULAR
  }
}

/**
 * {@link BlobResponseStore} class is used to store responses we get from preview service It
 * basically act as intermediate object with all metadata we get from attachment and preview
 * service.
 */
class BlobResponseStore {

  private final String filename;
  private final Long size;
  private final String mimeType;
  private final InputStream blobStream;
  private final String dispositionType;

  public BlobResponseStore(
      InputStream blobStream, String filename, Long size, String mimeType, String disposition) {
    this.blobStream = blobStream;
    this.filename = filename;
    this.size = size;
    this.mimeType = mimeType;
    this.dispositionType = disposition;
  }

  public String getDispositionType() {
    return dispositionType;
  }

  public InputStream getBlobStream() {
    return blobStream;
  }

  public String getFilename() {
    return filename;
  }

  public Long getSize() {
    return size;
  }

  public String getMimeType() {
    return mimeType;
  }
}
