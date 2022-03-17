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

public class GetPreview extends MailDocumentHandler {

  static final String PREVIEW_SERVICE_BASE_URL = "http://127.78.0.6:10000/";
  private final Log log = ZimbraLog.misc;
  private final String serverBaseUrl = getServerBaseUrl(null, false);
  Element requestElement = null;

  /**
   * Helper function to return the URL of server
   *
   * @param server ({@link com.zimbra.cs.account.Server})
   * @return url of server
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
    } catch (ServiceException e) {
      return "";
    }
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    requestElement = request;
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
    if (attachmentResponse != null && attachmentResponse.statusCode == 200) {
      try {
        previewResponse = getPreview(new File(attachmentResponse.getTempFilePath()));
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
    if (previewResponse != null && previewResponse.statusCode == 200) {
      String previewStream = Base64.getEncoder().encodeToString(previewResponse.content);
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
   * @throws HttpException http exception
   * @throws IOException   IO exception
   */
  private ServiceResponse getPreview(File f) throws HttpException, IOException {
    ServiceResponse serviceResponse = new ServiceResponse();
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
        url = GetPreview.PREVIEW_SERVICE_BASE_URL + "preview/pdf/" + getPdfParams();
      } else {
        if (contentType.equalsIgnoreCase("image/png") || contentType.equalsIgnoreCase(
            "image/jpeg")) {
          url = GetPreview.PREVIEW_SERVICE_BASE_URL + "preview/image/" + getImageParams();
        } else {
          url = GetPreview.PREVIEW_SERVICE_BASE_URL + "preview/pdf/" + getPdfParams();
        }
      }
      post.setURI(URI.create(url));
      log.info("K_PREVIEWER preview URL: " + url);
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.addBinaryBody("file", f, ContentType.create(contentType, "UTF-8"), f.getName());
      post.setEntity(builder.build());
      HttpResponse response = HttpClientUtil.executeMethod(client, post);
      serviceResponse.statusCode = response.getStatusLine().getStatusCode();
      serviceResponse.content = EntityUtils.toByteArray(response.getEntity());
    } finally {
      post.releaseConnection();
    }
    return serviceResponse;
  }

  private String getImageParams() {
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


  private String getPdfParams() {
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
        serviceResponse.statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          Header header = response.getFirstHeader("Content-Disposition");
          String disposition = header == null ? "" : header.getValue();
          String fileName = disposition.replaceFirst("(?i)^.*filename=\"([^\"]+)\".*$", "$1");
          serviceResponse.content = EntityUtils.toByteArray(entity);
          serviceResponse.storeContent(fileName, serviceResponse.content);
        }
      } finally {
        get.releaseConnection();
      }
    }
    return serviceResponse;
  }

  private Element appendError(Element response, String errorMessage) {
    Element error = response.addNonUniqueElement("error");
    error.setText(errorMessage);
    return response;
  }

  private static class ServiceResponse {

    int statusCode;
    byte[] content;
    String origFileName;
    String tempFilePath;

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

    public boolean storeContent(String filename, byte[] content) {
      boolean stored;
      origFileName = filename;
      try {
        File tempFile = File.createTempFile("preview_", "_" + filename);
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
}
