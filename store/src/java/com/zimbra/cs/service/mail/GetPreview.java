package com.zimbra.cs.service.mail;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.FileUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.client.LmcSession;
import com.zimbra.soap.ZimbraSoapContext;
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
  private final Log log = ZimbraLog.misc;
  private final String serverBaseUrl = getServerBaseUrl(null, false);

  /**
   * Helper method to return the baseURL for mailbox server
   *
   * @param server ({@link com.zimbra.cs.account.Server})
   * @return URL string of mailbox server
   */
  private String getServerBaseUrl(Server server, boolean withPort) {
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
    } catch (ServiceException e) {
      return "";
    }
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zc = getZimbraSoapContext(context);
    ZAuthToken authToken = zc.getRawAuthToken();
    LmcSession session = new LmcSession(authToken, null);
    ServiceResponse attachmentResponse = null;
    ServiceResponse previewResponse = null;
    Element response = zc.createElement(MailConstants.GET_PREVIEW_RESPONSE);
    Map<Integer, String> serviceStatus = getPreviewServiceStatus();
    Optional<Entry<Integer, String>> first = serviceStatus.entrySet().stream().findFirst();
    first.ifPresent(entry -> response.addUniqueElement("previewServiceStatus")
        .addAttribute("error-code", entry.getKey()).addAttribute("error-reason", entry.getValue()));

    if (serverBaseUrl.isEmpty()) {
      return appendError(response, "Unable to get preview. Not able to get the server URL.");
    }

    String itemId = request.getAttribute("itemId", null);
    String partNo = request.getAttribute("part", null);

    if (StringUtil.isNullOrEmpty(itemId) || StringUtil.isNullOrEmpty(partNo)) {
      return appendError(response, "Unable to get preview. Missing required parameters.");
    }

    //Get the attachment
    try {
      attachmentResponse = getAttachment(itemId, partNo, serverBaseUrl,
          session, serverBaseUrl);
    } catch (HttpException | IOException e) {
      log.error("An exception occurred while making getAttachment request, trace: ",
          e.getMessage());
    }

    //Post it to the preview service with he passed parameters
    if (attachmentResponse != null && attachmentResponse.getStatusCode() == 200) {
      try {
        previewResponse = getPreview(request, new File(attachmentResponse.getTempFilePath()));
        previewResponse.setOrigFileName(attachmentResponse.getOrigFileName());
      } catch (HttpException | IOException e) {
        log.error("An exception occurred while making preview request, trace: ", e.getMessage());
      } finally {
        try {
          FileUtil.delete(new File(attachmentResponse.getTempFilePath()));
        } catch (IOException e) {
          //ignore
        }
      }
    } else {
      return appendError(response, "Failed to get the attachment.");
    }

    //Pass the response in the soap response
    if (previewResponse != null && previewResponse.getStatusCode() == 200) {
      String previewStream = Base64.getEncoder().encodeToString(previewResponse.getContent());
      Element previewDataStream = response.addUniqueElement("previewDataStream");
      previewDataStream.setText(previewStream);
      previewDataStream.addAttribute("file-name", previewResponse.getOrigFileName());
    } else {
      return appendError(response,
          "Failed to generate preview.");
    }
    return response;
  }

  /**
   * Get Preview from previewer service for attachment file passed
   *
   * @param f file to post to preview service(downloaded attachment in this case)
   * @return ServiceResponse ({@link ServiceResponse })
   * @throws HttpException HTTP exceptions occurred during the transport
   * @throws IOException IO exception occurred during the transport
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
      String url;
      if (contentType == null || contentType.isEmpty()) {
        //TODO raise exception here and return
        contentType = "application/pdf";
        url = GetPreview.PREVIEW_SERVICE_BASE_URL + "preview/pdf/" + getPdfParams(requestElement);
      } else {
        if (contentType.equalsIgnoreCase("image/png") || contentType.equalsIgnoreCase(
            "image/jpeg")) {
          url = GetPreview.PREVIEW_SERVICE_BASE_URL + "preview/image/" + getImageParams(
              requestElement);
        } else {
          url = GetPreview.PREVIEW_SERVICE_BASE_URL + "preview/pdf/" + getPdfParams(requestElement);
        }
      }
      post.setURI(URI.create(url));
      log.info("K_PREVIEWER preview URL: " + url);
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
   * @param requestElement referenced IMage request element
   * @return string containing request parameters
   */
  private String getImageParams(Element requestElement) {
    String imageParams = "";
    Element imageEle = requestElement.getOptionalElement("image");
    if (imageEle != null) {
      String previewType = imageEle.getAttribute("preview_type", ""); //mn
      String area = imageEle.getAttribute("area", ""); //mn
      String quality = imageEle.getAttribute("quality", ""); //on
      String outputFormat = imageEle.getAttribute("output_format", "jpeg"); //od
      if (previewType.equals("thumbnail")) {
        //add path arguments
        imageParams = "/" + area + "/" + previewType + "/";
        //add query params
        imageParams += (quality.isEmpty() ? "?" : "?quality=" + quality + "&") + "output_format="
            + outputFormat;
      } else {
        String crop = imageEle.getAttribute("crop", "false"); //od
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
   * @param requestElement referenced PDF request element
   * @return string containing request parameters
   */
  private String getPdfParams(Element requestElement) {
    String pdfParams = "";
    Element pdfEle = requestElement.getOptionalElement("pdf");
    if (pdfEle != null) {
      String previewType = pdfEle.getAttribute("preview_type", ""); //mn
      if (previewType.equals("thumbnail")) {
        String quality = pdfEle.getAttribute("quality", ""); //on
        String outputFormat = pdfEle.getAttribute("output_format", "jpeg"); //od
        String area = pdfEle.getAttribute("area", ""); //mn
        //add path arguments
        pdfParams += "/" + area + "/" + previewType + "/";
        //add query params
        pdfParams += (quality.isEmpty() ? "?" : "?quality=" + quality + "&") + "output_format="
            + outputFormat;
      } else {
        String firstPage = pdfEle.getAttribute("first_page", "1"); //od
        String lastPage = pdfEle.getAttribute("last_page", "1"); //od
        //add query params
        pdfParams = "?first_page=" + firstPage + "&last_page=" + lastPage;
      }
    }
    return pdfParams;
  }


  /**
   * Method used to get the status(health/live) of PreviewService
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
   * @param itemId item's ID (attachment id)
   * @param partNo attachment's part number
   * @param baseURL base URL of server where mailbox is hosted
   * @param session ({@link LmcSession}) active session which has to be used to authenticate with request
   * @param cookieDomain cookie domain is domain base for the mailbox server
   * @return ServiceResponse ({@link ServiceResponse})
   * @throws HttpException HTTP exceptions occurred during the transport
   * @throws IOException IO exception occurred during the transport
   */
  private ServiceResponse getAttachment(String itemId, String partNo, String baseURL,
      LmcSession session, String cookieDomain) throws HttpException, IOException {
    ServiceResponse serviceResponse = new ServiceResponse();
    if (session == null) {
      log.error(System.currentTimeMillis() + " " + Thread.currentThread()
          + " getAttachment session=null");
    } else {
      HttpClientBuilder clientBuilder = ZimbraHttpConnectionManager.getInternalHttpConnMgr()
          .newHttpClient();
      String url = baseURL + "/service/content/get?id=" + itemId + "&part=" + partNo + "&auth=co";
      log.info("K_PREVIEWER using getAttachment URL: " + url);
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
          serviceResponse.storeContent(fileName , EntityUtils.toByteArray(entity));
        }
      } finally {
        get.releaseConnection();
      }
    }
    return serviceResponse;
  }

  /**
   * append error element to the response and return the modified response
   * @param response response element to be appended with error element
   * @param errorMessage error message that will set to text of error element
   * @return modified response
   */
  private Element appendError(Element response, String errorMessage) {
    Element error = response.addNonUniqueElement("error");
    error.setText(errorMessage);
    return response;
  }

}

/** Represent response received from services
 * this class stores the data we receive from
 * various services, it can store the response
 * received in bytearray and have ability to
 * store the response in temporary file.
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
   * @param fileName filename received in content-disposition header whose data is being stored
   * @param bytes data in form of byte array
   * @return true if content was stored correctly
   */
  public boolean storeContent(String fileName, byte[] bytes) {
    boolean stored;
    origFileName = fileName;
    content = bytes;
    try {
      File tempFile = File.createTempFile("preview_", "_" + origFileName);
      tempFilePath = tempFile.getAbsolutePath();
      ZimbraLog.misc.info("K_PREVIEWER stored attachment in: " + tempFilePath);
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
