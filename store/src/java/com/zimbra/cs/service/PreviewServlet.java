package com.zimbra.cs.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.preview.PreviewClient;
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
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.mail.internet.MimePart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The preview service servlet - serves preview for requested mail attachment using Carbonio
 * previewer service
 *
 * <pre>
 *   https://nbm-s01.demo.zextras.io/service/preview/{format}/{itemId}/{partNo}/{area}/{thumbnail}/?[{query-params}]
 *
 *          Path parameters:
 *                    format  =  image | pdf
 *                    itemId  =  mail-item-id
 *                    partNo  =  mail-item-part-number
 *                      area  =  500x500
 *                 thumbnail  =  omit for full preview type
 *                               'thumbnail' if requesting for preview type
 *                                thumbnail
 *
 *          Query parameters:
 *                     shape  =  rounded | rectangular
 *                   quality  =  lowest | low | medium | high | highest
 *             output_format  =  jpeg | png
 *
 *            Authentication  =  expects ZM_AUTH_TOKEN cookie passed in request
 *                               headers
 *
 * </pre>
 *
 * @author keshavbhatt
 */
public class PreviewServlet extends ZimbraServlet {

  public static final String SERVLET_PATH = "/preview";
  private static final long serialVersionUID = -4834966842520538743L;
  private static final Log mLog = LogFactory.getLog(PreviewServlet.class);
  private static final String PREVIEW_SERVICE_BASE_URL = "http://127.78.0.6:10000/";

  /**
   * This method is used to retrieve the attachment from mailbox
   *
   * @param accountId the accountId of user
   * @param token the {@link AuthToken} of user
   * @param messageId the messageId that we want to get attachment from
   * @param part the part number of the attachment in email
   * @return the {@link MimePart} object
   */
  Try<MimePart> getAttachment(String accountId, AuthToken token, int messageId, String part) {
    final Try<MimePart> mimePart =
        Try.of(() -> MailboxManager.getInstance().getMailboxByAccountId(accountId))
            .mapTry(mailbox -> mailbox.getMessageById(new OperationContext(token), messageId))
            .mapTry(message -> Mime.getMimePart(message.getMimeMessage(), part));
    if (mimePart.isSuccess()) {
      return mimePart;
    }
    return Try.failure(mimePart.failed().get());
  }

  /**
   * This method is used to get the preview of passed attachment from the preview service based on
   * the requestUrl, calling different endpoints of preview service
   *
   * @param requestUrl the {@link String} requestUrl
   * @return the {@link BlobResponseStore} object
   */
  Try<BlobResponseStore> getAttachmentPreview(String requestUrl, MimePart attachmentMimePart) {

    PreviewClient previewClient = PreviewClient.atURL(PREVIEW_SERVICE_BASE_URL);

    final String attachmentFileName = Try.of(attachmentMimePart::getFileName).getOrElse("unknown");
    final Try<InputStream> attachmentMimePartInputStream =
        Try.of(attachmentMimePart::getInputStream);

    if (attachmentMimePartInputStream.isFailure()) {
      return Try.failure(
          ServiceException.FAILURE(
              "Cannot process attachment", attachmentMimePartInputStream.getCause()));
    }

    Matcher previewImage =
        Pattern.compile(
                SERVLET_PATH
                    + "/image/([0-9\\-]*)/([0-9]+)/([0-9]*x[0-9]*)/?((?=(?!thumbnail))(?=([^/\\n ]*)))")
            .matcher(requestUrl);

    Matcher thumbnailImage =
        Pattern.compile(
                SERVLET_PATH + "/image/([0-9\\-]*)/([0-9]+)/([0-9]*x[0-9]*)/thumbnail/?\\??(.*)")
            .matcher((requestUrl));

    Matcher previewPdf =
        Pattern.compile(
                SERVLET_PATH + "/pdf/([0-9\\-]*)/([0-9]+)/?((?=(?!thumbnail))(?=([^/\\n ]*)))")
            .matcher((requestUrl));

    Matcher thumbnailPdf =
        Pattern.compile(
                SERVLET_PATH + "/pdf/([0-9\\-]*)/([0-9]+)/([0-9]*x[0-9]*)/thumbnail/?\\??(.*)")
            .matcher((requestUrl));

    if (thumbnailImage.find()) {
      String previewArea = thumbnailImage.group(3);
      PreviewQueryParameters queryParameters = parseQueryParameters(thumbnailImage.group(4));
      Try<BlobResponse> thumbnailOfImage =
          previewClient.postThumbnailOfImage(
              attachmentMimePartInputStream.get(),
              generateQuery(previewArea, queryParameters),
              attachmentFileName);
      if (thumbnailOfImage.isSuccess()) {
        return mapResponseToBlobResponseStore(thumbnailOfImage, attachmentFileName);
      } else {
        return Try.failure(thumbnailOfImage.failed().get());
      }
    }

    if (thumbnailPdf.find()) {
      String previewArea = thumbnailPdf.group(3);
      PreviewQueryParameters queryParameters = parseQueryParameters(thumbnailPdf.group(4));
      Try<BlobResponse> thumbnailOfPdf =
          previewClient.postThumbnailOfPdf(
              attachmentMimePartInputStream.get(),
              generateQuery(previewArea, queryParameters),
              attachmentFileName);
      if (thumbnailOfPdf.isSuccess()) {
        return mapResponseToBlobResponseStore(thumbnailOfPdf, attachmentFileName);
      } else {
        return Try.failure(thumbnailOfPdf.failed().get());
      }
    }

    if (previewImage.find()) {
      String previewArea = previewImage.group(3);
      PreviewQueryParameters queryParameters = parseQueryParameters(previewImage.group(5));
      final Try<BlobResponse> previewOfImage =
          previewClient.postPreviewOfImage(
              attachmentMimePartInputStream.get(),
              generateQuery(previewArea, queryParameters),
              attachmentFileName);
      if (previewOfImage.isSuccess()) {
        return mapResponseToBlobResponseStore(previewOfImage, attachmentFileName);
      } else {
        return Try.failure(previewOfImage.failed().get());
      }
    }

    if (previewPdf.find()) {
      PreviewQueryParameters queryParameters = parseQueryParameters(previewPdf.group(4));
      final Try<BlobResponse> previewOfPdf =
          previewClient.postPreviewOfPdf(
              attachmentMimePartInputStream.get(),
              generateQuery(null, queryParameters),
              attachmentFileName);
      if (previewOfPdf.isSuccess()) {
        return mapResponseToBlobResponseStore(previewOfPdf, attachmentFileName);
      } else {
        return Try.failure(previewOfPdf.failed().get());
      }
    }
    return Try.failure(ServiceException.INVALID_REQUEST("Cannot handle request", null));
  }

  /**
   * This method is used to map the preview service's {@link BlobResponse} to our {@link
   * BlobResponseStore} object
   *
   * @param response preview service's {@link BlobResponse}
   * @param fileName filename that we want to assign to our {@link BlobResponseStore} object
   * @return mapped {@link BlobResponseStore} object
   */
  Try<BlobResponseStore> mapResponseToBlobResponseStore(
      Try<BlobResponse> response, String fileName) {
    return (response.isSuccess())
        ? Try.success(
        new BlobResponseStore(
            response.get().getContent(),
            fileName,
            response.get().getLength(),
            response.get().getMimeType()))
        : Try.failure(response.failed().get());
  }

  /**
   * This method generates the final query {@link Query} from passed Optional area string and {@link
   * PreviewQueryParameters}
   *
   * @param optArea the optional area {@link String} parameter
   * @param queryParameters the {@link PreviewQueryParameters} object
   * @return {@link Query}
   */
  Query generateQuery(String optArea, PreviewQueryParameters queryParameters) {
    QueryBuilder parameterBuilder = new QueryBuilder();
    if (optArea != null) parameterBuilder.setPreviewArea(optArea);
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
   * This method is used to get the complete URL from {@link HttpServletRequest}
   *
   * @param request the {@link HttpServletRequest} object
   * @return {@link String} complete URL (
   *     <pre> protocol + servername + port + path + query </pre>
   *     )
   */
  String getUrlWithQueryParams(final HttpServletRequest request) {
    return request.getQueryString() == null
        ? request.getRequestURL().toString()
        : request.getRequestURL().append("?").append(request.getQueryString()).toString();
  }

  /**
   * This method is used to send success response for {@link HttpServletRequest} with the blob we
   * got from preview service
   *
   * @param resp the {@link HttpServletResponse} object
   * @param blobResponseStore the {@link BlobResponseStore} object
   */
  void respondWithSuccess(HttpServletResponse resp, BlobResponseStore blobResponseStore) {
    resp.addHeader("connection", "close");
    resp.addHeader("content-length", String.valueOf(blobResponseStore.getSize()));
    resp.addHeader("content-type", blobResponseStore.getMimeType());

    try {
      resp.addHeader(
          "content-disposition",
          "attachment; filename*=UTF-8''"
              + URLEncoder.encode(blobResponseStore.getFilename(), StandardCharsets.UTF_8));
      ByteUtil.copy(blobResponseStore.getBlobStream(), true, resp.getOutputStream(), false);
    } catch (Exception e) {
      mLog.error(e.getMessage(), e);
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
    resp.setContentType("text/html; charset=utf-8");
    try {
      resp.sendError(errCode, reason);
    } catch (IOException e) {
      mLog.error(e.getMessage(), e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    ZimbraLog.clearContext();
    addRemoteIpToLoggingContext(req);

    final AuthToken authToken = getAuthTokenFromCookie(req, resp);
    checkAuthTokenFromCookieOrRespondWithError(authToken, req, resp);

    final Pattern requiredQueryParametersPattern =
        Pattern.compile(SERVLET_PATH + "/([a-zA-Z]+)/([0-9]+)/([0-9]+)");
    final Matcher requiredQueryParametersMatcher =
        requiredQueryParametersPattern.matcher(getUrlWithQueryParams(req));

    // check url for required parameters
    if (!requiredQueryParametersMatcher.find()
        || requiredQueryParametersMatcher.groupCount() != 3) {
      respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
    } else {
      final int messageId = Integer.parseInt(requiredQueryParametersMatcher.group(2));
      final String partNo = requiredQueryParametersMatcher.group(3);
      final String accountId = authToken.getAccountId();

      // get attachment
      final Try<MimePart> attachmentMimePart =
          getAttachment(accountId, authToken, messageId, partNo);
      if (attachmentMimePart.isFailure()) {
        respondWithError(
            resp, HttpServletResponse.SC_ACCEPTED, attachmentMimePart.failed().get().getMessage());
      }

      // get preview
      final Try<BlobResponseStore> previewOfAttachment =
          getAttachmentPreview(getUrlWithQueryParams(req), attachmentMimePart.get());
      if (previewOfAttachment.isFailure()) {
        respondWithError(
            resp, HttpServletResponse.SC_ACCEPTED, previewOfAttachment.failed().get().getMessage());
      }

      // send preview response
      respondWithSuccess(resp, previewOfAttachment.get());
    }
  }

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
    mLog.info("Servlet " + getServletName() + " starting up");
    super.init();
  }

  @Override
  public void destroy() {
    mLog.info("Servlet " + getServletName() + " shutting down");
    super.destroy();
  }
}

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

  private enum Quality {
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

  private enum Format {
    @JsonProperty("jpeg")
    JPEG,

    @JsonProperty("png")
    PNG
  }

  private enum Shape {
    @JsonProperty("rounded")
    ROUNDED,

    @JsonProperty("rectangular")
    RECTANGULAR
  }
}

class BlobResponseStore {
  private final String filename;
  private final Long size;
  private final String mimeType;
  private final InputStream blobStream;

  public BlobResponseStore(InputStream blobStream, String filename, Long size, String mimeType) {
    this.blobStream = blobStream;
    this.filename = filename;
    this.size = size;
    this.mimeType = mimeType;
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
