package com.zimbra.cs.service.mail;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.FileUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.client.LmcSession;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.GetPreviewRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

/**
 * GetPreview class is registered to dispatch response to the requests coming from the
 * GetPreviewRequest SOAP endpoint
 */
public class GetPreview extends MailDocumentHandler {

  private static final String PREVIEW_SERVICE_BASE_URL = "http://127.78.0.6:10000/";
  private static final Log LOG = LogFactory.getLog(GetPreview.class);
  private final String serverBaseUrl = getServerBaseUrl(null, false);

  /**
   * Helper method to return the baseURL for mailbox server
   *
   * @param server ({@link com.zimbra.cs.account.Server})
   * @return URL string of mailbox server
   */
  public static String getServerBaseUrl(Server server, boolean withPort) {
    String scheme = "https";
    try {
      server = server == null ? Provisioning.getInstance().getLocalServer() : server;
      String hostname = server.getServiceHostname();
      int port = server.getIntAttr(Provisioning.A_zimbraMailSSLPort, 0);
      if (port <= 0) {
        port = server.getIntAttr(Provisioning.A_zimbraMailPort, 0);
        scheme = "http";
      }
      return scheme + "://" + hostname + (withPort ? ":" + port : "");
    } catch (ServiceException ignored) {
      return "";
    }
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zc = getZimbraSoapContext(context);
    ZAuthToken authToken = zc.getRawAuthToken();
    LmcSession session = new LmcSession(authToken, null);
    ServiceResponse attachmentResponse;
    ServiceResponse previewResponse;
    GetPreviewRequest getPreviewRequest = zc.elementToJaxb(request);
    Element response = zc.createElement(MailConstants.GET_PREVIEW_RESPONSE);
    final int previewServiceStatusCode = getPreviewServiceStatus().entrySet().stream().findFirst().map(entry -> {
          response.addUniqueElement(MailConstants.E_P_PREVIEW_SERVICE_STATUS)
              .addAttribute(MailConstants.A_P_STATUS_CODE, entry.getKey())
              .addAttribute(MailConstants.A_P_STATUS_MESSAGE, entry.getValue());
          return entry.getKey();
        }
    ).orElseGet(-1);

    if (serverBaseUrl.isEmpty()) {
      throw ServiceException.RESOURCE_UNREACHABLE(
          "Unable to get preview. Not able to get the mailbox server URL.", null);
    }

    if (previewServiceStatusCode != 200) {
      throw ServiceException.TEMPORARILY_UNAVAILABLE();
    }

    String itemId = getPreviewRequest.getItemId();
    String partNo = getPreviewRequest.getPart();

    if (StringUtil.isNullOrEmpty(itemId) || StringUtil.isNullOrEmpty(partNo)) {
      throw ServiceException.INVALID_REQUEST("Unable to get preview. Missing required parameters.",
          null);
    }

    //Get the attachment
    try {
      attachmentResponse = getAttachment(itemId, partNo, serverBaseUrl,
          session, serverBaseUrl);
      //Post it to the preview service with he passed parameters
      if (attachmentResponse.getStatusCode() == 200) {
        previewResponse = getPreview(request, new File(attachmentResponse.getTempFilePath()));
        previewResponse.setOrigFileName(attachmentResponse.getOrigFileName());
        FileUtil.delete(new File(attachmentResponse.getTempFilePath()));
        //Pass the response in the soap response
        if (previewResponse.getStatusCode() == 200) {
          String previewStream = Base64.getEncoder().encodeToString(previewResponse.getContent());
          Element previewDataStream = response.addUniqueElement(
              MailConstants.E_P_PREVIEW_DATA_STREAM);
          previewDataStream.setText(previewStream);
          previewDataStream.addAttribute(MailConstants.A_P_FILE_NAME,
              previewResponse.getOrigFileName());
        } else {
          throw ServiceException.RESOURCE_UNREACHABLE(
              "Failed to generate preview. Preview service returned with code "
                  + previewResponse.getStatusCode(), null);
        }
      } else {
        throw ServiceException.RESOURCE_UNREACHABLE(
            "Failed to get the attachment. Attachment provider service returned with code "
                + attachmentResponse.getStatusCode(), null);
      }
    } catch (HttpException | IOException e) {
      throw ServiceException.RESOURCE_UNREACHABLE(
          "An exception occurred while completing your request.", e);
    }
    return response;
  }

  /**
   * Get Preview from previewer service for attachment file passed
   *
   * @param requestElement the request element
   * @param f              file to post to preview service(downloaded attachment in this case)
   * @return ServiceResponse ({@link ServiceResponse })
   * @throws HttpException HTTP exceptions occurred during the transport
   * @throws IOException   IO exception occurred during the transport
   */
  private ServiceResponse getPreview(Element requestElement, File f)
      throws HttpException, IOException {
    ServiceResponse previewServiceResponse = new ServiceResponse();
    HttpClientBuilder clientBuilder = ZimbraHttpConnectionManager.getInternalHttpConnMgr()
        .newHttpClient();

    RequestConfig reqConfig = RequestConfig.copy(
            ZimbraHttpConnectionManager.getInternalHttpConnMgr().getZimbraConnMgrParams()
                .getReqConfig())
        .setCookieSpec(CookieSpecs.DEFAULT).build();

    clientBuilder.setDefaultRequestConfig(reqConfig);
    SocketConfig config = SocketConfig.custom().setSoTimeout(5000).build();
    clientBuilder.setDefaultSocketConfig(config);

    HttpClient client = clientBuilder.build();
    HttpPost post = new HttpPost();
    try {
      String contentType = URLConnection.getFileNameMap().getContentTypeFor(f.getName());
      final String url = Optional.ofNullable(contentType)
                .map(String::toLowerCase)
                .filter(cntType -> !cntType.isEmpty() && !cntType.isBlank())
                .map(cntType -> {
            switch (contentType) {
                case "image/png":
                case "image/jpeg":
                case "image/jpg":
                {
                    // ...
                    return GetPreview.PREVIEW_SERVICE_BASE_URL + "preview/image/" + getImageParamsAsQueryString(
                  requestElement);
                }
                case "application/pdf":
                {
                  return GetPreview.PREVIEW_SERVICE_BASE_URL + "preview/pdf/" + getPdfParamsAsQueryString(
              requestElement);
                }
                default:
                {
                    // This means we don't support preview for this file type
                    throw new RuntimeException("Preview for this file type is not supported"); // FIXME use correct exception
                }
            }
        }).orElse(GetPreview.PREVIEW_SERVICE_BASE_URL + "preview/pdf/" + getPdfParamsAsQueryString(
              requestElement)); // FIXME Are you sure here you wanna return PDFs? In case fix this
      post.setURI(URI.create(url));
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.addBinaryBody("file", f, ContentType.create(contentType, "UTF-8"), f.getName());
      post.setEntity(builder.build());
      HttpResponse response = HttpClientUtil.executeMethod(client, post);
      previewServiceResponse.setStatusCode(response.getStatusLine().getStatusCode());
      previewServiceResponse.setContent(EntityUtils.toByteArray(response.getEntity()));
    } finally {
      post.releaseConnection();
    }
    return previewServiceResponse;
  }

  /**
   * Helper method to form request parameters for request type Image
   *
   * @param requestElement referenced IMage request element
   * @return string containing request parameters
   */
  public static String getImageParamsAsQueryString(Element requestElement) {
    String imageParams = "";
    Element imageEle = requestElement.getOptionalElement(MailConstants.E_P_IMAGE);
    if (imageEle != null) {
      String previewType = imageEle.getAttribute(MailConstants.A_P_PREVIEW_TYPE, ""); //mn
      String area = imageEle.getAttribute(MailConstants.A_P_AREA, ""); //mn
      String quality = imageEle.getAttribute(MailConstants.A_P_QUALITY, ""); //on
      String outputFormat = imageEle.getAttribute(MailConstants.A_P_OUTPUT_FORMAT, "jpeg"); //od
      if (previewType.equals("thumbnail")) {
        //add path arguments
        imageParams = "/" + area + "/" + previewType + "/";
        //add query params
        imageParams += (quality.isEmpty() ? "?" : "?quality=" + quality + "&") + "output_format="
            + outputFormat;
      } else {
        String crop = imageEle.getAttribute(MailConstants.A_P_CROP, "false"); //od
        //add path arguments
        imageParams = "/" + area + "/";
        //add query params
        imageParams += (crop.isEmpty() ? "" : "?crop=" + crop) + (quality.isEmpty() ? ""
            : "&quality=" + quality) + "&output_format=" + outputFormat;
      }
    }
    return imageParams;
  }

  /**
   * Helper method to form request parameters for request type PDF
   *
   * @param requestElement referenced PDF request element
   * @return string containing request parameters
   */
  public static String getPdfParamsAsQueryString(Element requestElement) {
    String pdfParams = "";
    Element pdfEle = requestElement.getOptionalElement(MailConstants.E_P_PDF);
    if (pdfEle != null) {
      String previewType = pdfEle.getAttribute(MailConstants.A_P_PREVIEW_TYPE, ""); //mn
      if (previewType.equals("thumbnail")) {
        String quality = pdfEle.getAttribute(MailConstants.A_P_QUALITY, ""); //on
        String outputFormat = pdfEle.getAttribute(MailConstants.A_P_OUTPUT_FORMAT, "jpeg"); //od
        String area = pdfEle.getAttribute(MailConstants.A_P_AREA, ""); //mn
        //add path arguments
        pdfParams += "/" + area + "/" + previewType + "/";
        //add query params
        pdfParams += (quality.isEmpty() ? "?" : "?quality=" + quality + "&") + "output_format="
            + outputFormat;
      } else {
        String firstPage = pdfEle.getAttribute(MailConstants.A_P_FIRST_PAGE, "1"); //od
        String lastPage = pdfEle.getAttribute(MailConstants.A_P_LAST_PAGE, "1"); //od
        //add query params
        pdfParams = "?first_page=" + firstPage + "&last_page=" + lastPage;
      }
    }
    return pdfParams;
  }


  /**
   * Method used to get the status(health/live) of PreviewService
   *
   * @return HashMap of containing StatusCode(int) and ReasonString(string)
   */
  private Map<Integer, String> getPreviewServiceStatus() {
    Map<Integer, String> status = new LinkedHashMap<>();
    HttpClientBuilder clientBuilder = ZimbraHttpConnectionManager.getInternalHttpConnMgr()
        .newHttpClient();
    HttpGet get = new HttpGet(PREVIEW_SERVICE_BASE_URL + "health/live/");
    get.addHeader("accept", "application/json");
    HttpClient client = clientBuilder.build();
    try {
      HttpResponse response = HttpClientUtil.executeMethod(client, get);
      StatusLine statusLine = response.getStatusLine();
      status.put(statusLine.getStatusCode(), statusLine.getReasonPhrase());
      return status;
    } catch (HttpException | IOException e) {
      status.put(0, "Carbonio preview service is not reachable.");
    } finally {
      get.releaseConnection();
    }
    return status;
  }

  /**
   * Get the attachment content as ({@link ServiceResponse}) from mailbox's content servlet service
   *
   * @param itemId       item's ID (attachment id)
   * @param partNo       attachment's part number
   * @param baseURL      base URL of server where mailbox is hosted
   * @param session      ({@link LmcSession}) active session which has to be used to authenticate
   *                     with request
   * @param cookieDomain cookie domain is domain base for the mailbox server
   * @return ServiceResponse ({@link ServiceResponse})
   * @throws HttpException HTTP exceptions occurred during the transport
   * @throws IOException   IO exception occurred during the transport
   */
  private ServiceResponse getAttachment(String itemId, String partNo, String baseURL,
      LmcSession session, String cookieDomain) throws HttpException, IOException {
    ServiceResponse serviceResponse = new ServiceResponse();
    if (session == null) {
      LOG.error(System.currentTimeMillis() + " " + Thread.currentThread()
          + " getAttachment session=null");
    } else {
      HttpClientBuilder clientBuilder = ZimbraHttpConnectionManager.getInternalHttpConnMgr()
          .newHttpClient();
      String url = baseURL + "/service/content/get?id=" + itemId + "&part=" + partNo + "&auth=co";
      HttpGet get = new HttpGet(url);
      ZAuthToken zat = session.getAuthToken();
      Map<String, String> cookieMap = zat.cookieMap(false);
      if (cookieMap != null) {
        BasicCookieStore cookieStore = new BasicCookieStore();
        String cookieDomainBase;
        try {
          URI cookieDomainBaseUri = new URI(baseURL);
          cookieDomainBase = cookieDomainBaseUri.getHost();
        } catch (URISyntaxException e) {
          cookieDomainBase = cookieDomain;
        }
        for (Map.Entry<String, String> ck : cookieMap.entrySet()) {
          BasicClientCookie cookie = new BasicClientCookie(ck.getKey(), ck.getValue());
          cookie.setDomain(cookieDomainBase);
          cookie.setPath("/");
          cookieStore.addCookie(cookie);
        }
        clientBuilder.setDefaultCookieStore(cookieStore);
        RequestConfig reqConfig = RequestConfig.copy(
                ZimbraHttpConnectionManager.getInternalHttpConnMgr().getZimbraConnMgrParams()
                    .getReqConfig())
            .setCookieSpec(CookieSpecs.DEFAULT).build();
        clientBuilder.setDefaultRequestConfig(reqConfig);
      }
      SocketConfig config = SocketConfig.custom().setSoTimeout(5000).build();
      clientBuilder.setDefaultSocketConfig(config);
      HttpClient client = clientBuilder.build();
      try {
        HttpResponse response = HttpClientUtil.executeMethod(client, get);
        serviceResponse.setStatusCode(response.getStatusLine().getStatusCode());
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          Header header = response.getFirstHeader("Content-Disposition");
          String disposition = header == null ? "" : header.getValue();
          String fileName = disposition.replaceFirst("(?i)^.*filename=\"([^\"]+)\".*$", "$1");
          serviceResponse.storeContent(fileName, EntityUtils.toByteArray(entity));
        }
      } finally {
        get.releaseConnection();
      }
    }
    return serviceResponse;
  }
}

/**
 * Represent response received from services.
 * <p>
 * This class stores the data we receive from various services, it can store the response received
 * in bytearray and provides a method to store the response in temporary file.
 * <p>
 * This class is used to create sharable objects while passing response of one service to other
 */
class ServiceResponse {

  private int statusCode;
  private byte[] content;
  private String origFileName;
  private String tempFilePath;

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getOrigFileName() {
    return origFileName;
  }

  public void setOrigFileName(String origFileName) {
    this.origFileName = origFileName;
  }

  public String getTempFilePath() {
    return tempFilePath;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  /**
   * stores content in a temp file.
   *
   * @param fileName filename received in content-disposition header whose data is being stored
   * @param bytes    data in form of byte array
   * @return true if content was stored correctly
   */
  public boolean storeContent(String fileName, byte[] bytes) {
    boolean stored;
    origFileName = fileName;
    content = bytes;
    try {
      File tempFile = File.createTempFile("preview_", "_" + origFileName);
      tempFilePath = tempFile.getAbsolutePath();
      tempFile.deleteOnExit();
      FileOutputStream fos = new FileOutputStream(tempFile);
      fos.write(content);
      fos.close();
      stored = true;
    } catch (IOException ie) {
      stored = false;
    }
    return stored;
  }
}
