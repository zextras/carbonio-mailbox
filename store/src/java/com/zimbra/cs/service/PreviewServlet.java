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
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.L10nUtil.MsgKey;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.servlet.ZimbraServlet;
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
import org.apache.http.HttpHeaders;
import org.apache.http.protocol.HTTP;
import org.eclipse.jetty.http.HttpStatus;

/**
 * The preview service servlet - serves preview for requested mail attachments using Carbonio
 * previewer service
 *
 * <pre>
 *   The API is the almost same as of preview service(https://zextras.atlassian.net/wiki/spaces/SW/pages/2353430753/Preview+API)
 *   with few modification that let us make it use as preview service for mailbox attachments.
 *
 *   itemId, partNo, disposition(disp) are notable new parameters, their usage can be found in the URL given below:
 *
 *   https://nbm-s01.demo.zextras.io/service/preview/{format}/{itemId}/{partNo}/{area}/{thumbnail}/?[{query-params}]
 *
 *          Path parameters:
 *                    format  =  image | pdf
 *                    itemId  =  mail-item-id
 *                    partNo  =  mail-item-part-number
 *                      area  =  width of the output image (>=0) x height of the output image (>=0),
 *                               width x height => 100x200. The first is width, the latter height, the order is important!
 *                 thumbnail  =  omit for full preview type
 *                               'thumbnail' if requesting the preview type
 *                                thumbnail
 *
 *          Query parameters:
 *                      disp  =  attachment(a) | inline(i) ; default inline(i)
 *                     shape  =  rounded | rectangular
 *                   quality  =  lowest | low | medium | high | highest
 *             output_format  =  jpeg | png
 *                      crop  =  True will crop the picture starting from the borders.
 *                               This option will lose information, leaving it False will scale and have borders to fill the requested size.
 *                first_page  =  integer value of first page to preview (n>=1)
 *                 last_page  =  integer value of last page to preview (0 = last of the pdf)
 *
 *            Authentication  =  expects ZM_AUTH_TOKEN cookie passed in the request
 *                               headers
 *
 * </pre>
 *
 * @author keshavbhatt
 */
public class PreviewServlet extends ZimbraServlet {

  private static final String SERVLET_PATH = "/preview";
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
    List<String> possibleDisposition =
        Arrays.asList("\\?disp=" + dispositionType, "\\&disp=" + dispositionType);

    return possibleDisposition.stream()
        .reduce(
            requestUrl,
            (str, toRem) ->
                str.replaceAll(
                    toRem.contains("\\?") ? "\\?disp=" + dispositionType + "&" : toRem,
                    toRem.contains("\\?") ? "\\?" : ""));
  }

  /**
   * Returns the value of disposition type requested in URL's query parameter
   *
   * @param requestUrl the request URL
   * @return disposition value if found else the default "i"(inline)
   */
  static String getDispositionType(String requestUrl) {
    return Stream.of(requestUrl.split("\\?")[1].split("&"))
        .map(kv -> kv.split("="))
        .filter(kv -> "disp".equalsIgnoreCase(kv[0]))
        .map(kv -> kv[1])
        .findFirst()
        .orElse("i");
  }

  /**
   * This method is used to retrieve the attachment from mailbox
   *
   * @param authToken the {@link AuthToken} of user
   * @param messageId the messageId that we want to get attachment from
   * @param part the part number of the attachment in email
   * @return the {@link MimePart} object
   */
  private Try<MimePart> getAttachment(AuthToken authToken, int messageId, String part) {
    final String accountId = authToken.getAccountId();
    return Try.of(() -> MailboxManager.getInstance().getMailboxByAccountId(accountId))
        .mapTry(mailbox -> mailbox.getMessageById(new OperationContext(authToken), messageId))
        .mapTry(message -> Mime.getMimePart(message.getMimeMessage(), part));
  }

  /**
   * This method is used to get the preview of passed attachment from the preview service based on
   * the requestUrl, calling different endpoints of preview service
   *
   * @param requestUrl the {@link String} requestUrl
   * @return the {@link BlobResponseStore} object
   */
  private Try<BlobResponseStore> getAttachmentPreview(
      String requestUrl, MimePart attachmentMimePart) {

    // get disposition type query parameter from url
    final String dispositionType = getDispositionType(requestUrl);

    // get clean requestUrl for preview
    final String requestUrlForPreview = getRequestUrlForPreview(requestUrl, dispositionType);

    // get attachment filename from attachment MimePart
    final String attachmentFileName = Try.of(attachmentMimePart::getFileName).getOrElse("unknown");

    // get attachment inputStream
    final Try<InputStream> attachmentMimePartInputStream =
        Try.of(attachmentMimePart::getInputStream);

    // break if attachmentMimePartInputStream has encountered a failure
    if (attachmentMimePartInputStream.isFailure()) {
      return Try.failure(
          ServiceException.FAILURE(
              "Cannot process attachment", attachmentMimePartInputStream.getCause()));
    }

    // Start Preview API Controller=================================================================

    Matcher previewImage =
        Pattern.compile(
                SERVLET_PATH
                    + "/image/([0-9\\-]*)/([0-9]+)/([0-9]*x[0-9]*)/?((?=(?!thumbnail))(?=([^/\\n"
                    + " ]*)))")
            .matcher(requestUrlForPreview);

    Matcher thumbnailImage =
        Pattern.compile(
                SERVLET_PATH + "/image/([0-9\\-]*)/([0-9]+)/([0-9]*x[0-9]*)/thumbnail/?\\??(.*)")
            .matcher((requestUrlForPreview));

    Matcher previewPdf =
        Pattern.compile(
                SERVLET_PATH + "/pdf/([0-9\\-]*)/([0-9]+)/?((?=(?!thumbnail))(?=([^/\\n ]*)))")
            .matcher((requestUrlForPreview));

    Matcher thumbnailPdf =
        Pattern.compile(
                SERVLET_PATH + "/pdf/([0-9\\-]*)/([0-9]+)/([0-9]*x[0-9]*)/thumbnail/?\\??(.*)")
            .matcher((requestUrlForPreview));

    // Handle Image thumbnail request
    if (thumbnailImage.find()) {
      String previewArea = thumbnailImage.group(3);
      PreviewQueryParameters queryParameters = parseQueryParameters(thumbnailImage.group(4));
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
    if (thumbnailPdf.find()) {
      String previewArea = thumbnailPdf.group(3);
      PreviewQueryParameters queryParameters = parseQueryParameters(thumbnailPdf.group(4));
      return Try.of(
              () ->
                  previewClient.postThumbnailOfPdf(
                      attachmentMimePartInputStream.get(),
                      generateQuery(previewArea, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              thumbnailOfImage ->
                  mapResponseToBlobResponseStore(
                      thumbnailOfImage.get(), attachmentFileName, dispositionType));
    }
    // Handle Preview Image request
    if (previewImage.find()) {
      String previewArea = previewImage.group(3);
      PreviewQueryParameters queryParameters = parseQueryParameters(previewImage.group(5));
      return Try.of(
              () ->
                  previewClient.postPreviewOfImage(
                      attachmentMimePartInputStream.get(),
                      generateQuery(previewArea, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              thumbnailOfImage ->
                  mapResponseToBlobResponseStore(
                      thumbnailOfImage.get(), attachmentFileName, dispositionType));
    }

    // Handle Preview PDF request
    if (previewPdf.find()) {
      PreviewQueryParameters queryParameters = parseQueryParameters(previewPdf.group(4));
      return Try.of(
              () ->
                  previewClient.postPreviewOfPdf(
                      attachmentMimePartInputStream.get(),
                      generateQuery(null, queryParameters),
                      attachmentFileName))
          .flatMapTry(
              thumbnailOfImage ->
                  mapResponseToBlobResponseStore(
                      thumbnailOfImage.get(), attachmentFileName, dispositionType));
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
   * This method generates the final query {@link Query} from passed Optional area string and {@link
   * PreviewQueryParameters}
   *
   * @param optArea the optional area {@link String} parameter
   * @param queryParameters the {@link PreviewQueryParameters} object
   * @return {@link Query}
   */
  private Query generateQuery(String optArea, PreviewQueryParameters queryParameters) {
    QueryBuilder parameterBuilder = new QueryBuilder();
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
  PreviewQueryParameters parseQueryParameters(String queryParameters) {
    Map<String, String> parameters =
        Arrays.stream(queryParameters.replace("?", "").split("&"))
            .map(parameter -> parameter.split("="))
            .filter(parameter -> parameter.length == 2)
            .collect(Collectors.toMap(parameter -> parameter[0], parameter -> parameter[1]));
    return new ObjectMapper().convertValue(parameters, PreviewQueryParameters.class);
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
    String queryString = request.getQueryString();
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

    final AuthToken authToken = getAuthTokenFromCookie(req, resp);
    checkAuthTokenFromCookieOrRespondWithError(authToken, req, resp);

    final Pattern requiredQueryParametersPattern =
        Pattern.compile(SERVLET_PATH + "/([a-zA-Z]+)/([0-9]+)/([0-9]+)");
    final Matcher requiredQueryParametersMatcher =
        requiredQueryParametersPattern.matcher(getUrlWithQueryParams(req));

    // check url for the presence of required parameters and query string
    // send error otherwise
    if (req.getQueryString() == null
        || (!requiredQueryParametersMatcher.find()
            || requiredQueryParametersMatcher.groupCount() != 3)) {
      respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
    } else {
      final int messageId = Integer.parseInt(requiredQueryParametersMatcher.group(2));
      final String partNo = requiredQueryParametersMatcher.group(3);

      // get attachment
      final Try<MimePart> attachmentMimePart = getAttachment(authToken, messageId, partNo);
      if (attachmentMimePart.isFailure()) {
        respondWithError(
            resp,
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            attachmentMimePart.failed().get().getMessage());
      }

      // send preview response or commit preview api error as response
      Try.of(() -> getAttachmentPreview(getUrlWithQueryParams(req), attachmentMimePart.get()))
          .onSuccess(previewOfAttachment -> respondWithSuccess(resp, previewOfAttachment.get()))
          .onFailure(
              ex -> {
                if (ex instanceof BadRequest) {
                  respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                } else if (ex instanceof ItemNotFound) {
                  respondWithError(resp, HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
                } else if (ex instanceof ValidationError) {
                  respondWithError(resp, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                } else {
                  respondWithError(
                      resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                }
              });
    }
  }

  /**
   * @param authToken the {@link AuthToken} we want to check
   * @param req the {@link HttpServletRequest} object that will be used to provide req metadata in
   *     error message
   * @param resp the {@link HttpServletResponse} to send the error response
   */
  void checkAuthTokenFromCookieOrRespondWithError(
      AuthToken authToken, HttpServletRequest req, HttpServletResponse resp) {
    if (authToken == null) {
      respondWithError(
          resp,
          HttpServletResponse.SC_UNAUTHORIZED,
          L10nUtil.getMessage(MsgKey.errMustAuthenticate, req));
    }
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
    PNG
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
