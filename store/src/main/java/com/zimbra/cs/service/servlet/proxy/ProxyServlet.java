// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.servlet.proxy;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.ContentDisposition;
import com.zimbra.common.mime.ContentType;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.httpclient.HttpProxyUtil;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.cs.servlet.ZimbraServlet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

/**
 * @author jylee
 */
public class ProxyServlet extends ZimbraServlet {

  private static final String TARGET_PARAM = "target";
  private static final String UPLOAD_PARAM = "upload";
  private static final String FILENAME_PARAM = "filename";
  private static final String FORMAT_PARAM = "fmt";

  private static final String USER_PARAM = "user";
  private static final String PASS_PARAM = "pass";
  private static final String AUTH_PARAM = "auth";
  private static final String AUTH_BASIC = "basic";
  private static final String DEFAULT_CONTENT_TYPE_HEADER_VALUE = "text/xml";

  private Set<String> getAllowedDomains(AuthToken auth) throws ServiceException {
    Provisioning prov = Provisioning.getInstance();
    Account acct = prov.get(AccountBy.id, auth.getAccountId(), auth);
    Set<String> allowedDomains = prov.getCOS(acct).getMultiAttrSet(ZAttrProvisioning.A_zimbraProxyAllowedDomains);
    ZimbraLog.misc.debug("get allowedDomains result: " + allowedDomains);
    return allowedDomains;
  }

  private boolean checkPermissionOnTarget(URL target, AuthToken auth) {
    String host = target.getHost().toLowerCase();
    ZimbraLog.misc.debug("checking allowedDomains permission on target host: " + host);
    Set<String> domains;
    try {
      domains = getAllowedDomains(auth);
    } catch (ServiceException se) {
      ZimbraLog.misc.info("error getting allowedDomains: " + se.getMessage());
      return false;
    }
    for (String domain : domains) {
      if (domain.charAt(0) == '*') {
        domain = domain.substring(1);
        if (host.endsWith(domain)) {
          return true;
        }
      } else if (host.equals(domain)) {
        return true;
      }
    }
    return false;
  }

  private boolean canProxyHeader(String header) {
    if (header == null) {
      return false;
    }
    header = header.toLowerCase();
    return !header.startsWith("accept") &&
        !"content-length".equals(header) &&
        !"connection".equals(header) &&
        !"keep-alive".equals(header) &&
        !"pragma".equals(header) &&
        !"host".equals(header) &&
        !"cache-control".equals(header) &&
        !"cookie".equals(header) &&
        !"transfer-encoding".equals(header);
  }

  private byte[] copyPostedData(HttpServletRequest req) throws IOException {
    int size = req.getContentLength();
    if ("GET".equalsIgnoreCase(req.getMethod()) || size <= 0) {
      return new byte[0];
    }
    try (InputStream is = req.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(size)) {
      byte[] buffer = new byte[8192];
      int num;
      while ((num = is.read(buffer)) != -1) {
        byteArrayOutputStream.write(buffer, 0, num);
      }
      return byteArrayOutputStream.toByteArray();
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    handle(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    handle(req, resp);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    handle(req, resp);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    handle(req, resp);
  }

  private void handle(HttpServletRequest req, HttpServletResponse resp) {
    try {
      doProxy(req, resp);
    } catch (IOException e) {
      ZimbraLog.misc.info(e.getMessage(), e);
    }
  }

  @Override
  protected boolean isAdminRequest(HttpServletRequest req) {
    return req.getServerPort() == LC.zimbra_admin_service_port.intValue();
  }

  private void doProxy(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ZimbraLog.clearContext();
    boolean isAdmin = isAdminRequest(req);

    try {
      AuthToken authToken = getAuthTokenFrom(req, resp);
      if (authToken == null) {
        return;
      }

      // get the posted body before the server read and parse them.
      byte[] body = copyPostedData(req);

      // sanity check
      final String target = req.getParameter(TARGET_PARAM);
      if (target == null) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      // check for permission
      final URL url = new URL(target);
      if (!isAdmin && !checkPermissionOnTarget(url, authToken)) {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }

      HttpClientBuilder clientBuilder = HttpClients.custom();
      HttpProxyUtil.configureProxy(clientBuilder);
      final HttpRequestBase method = configureHttpMethod(req, target, body);
      if (method == null) {
        ZimbraLog.misc.info("unsupported request method");
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      } else {
        setBasicAuth(req, resp, clientBuilder);
        proxyHeaders(req, method);
        handleHttpResponse(req, resp, method, authToken, target);
      }
    } catch (IOException e) {
      ZimbraLog.misc.info(e.getMessage(), e);
    }
  }

  private void setBasicAuth(HttpServletRequest req, HttpServletResponse resp,
      HttpClientBuilder clientBuilder) throws IOException {
    String auth = req.getParameter(AUTH_PARAM);
    String user = req.getParameter(USER_PARAM);
    String pass = req.getParameter(PASS_PARAM);
    if (auth != null && user != null && pass != null) {
      if (!auth.equals(AUTH_BASIC)) {
        ZimbraLog.misc.info("unsupported auth type: " + auth);
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      CredentialsProvider provider = new BasicCredentialsProvider();
      provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pass));
      clientBuilder.setDefaultCredentialsProvider(provider);
    }
  }

  private void proxyHeaders(HttpServletRequest req, HttpRequestBase method) {
    Enumeration<String> headers = req.getHeaderNames();
    while (headers.hasMoreElements()) {
      String hdr = headers.nextElement();
      ZimbraLog.misc.debug("incoming: " + hdr + ": " + req.getHeader(hdr));
      if (canProxyHeader(hdr)) {
        ZimbraLog.misc.debug("outgoing: " + hdr + ": " + req.getHeader(hdr));
        method.addHeader(hdr, req.getHeader(hdr));
      }
    }
  }

  private HttpRequestBase configureHttpMethod(HttpServletRequest req, String target, byte[] body) {
    String reqMethod = req.getMethod();
    HttpRequestBase method = null;
    if ("GET".equalsIgnoreCase(reqMethod)) {
      method = new HttpGet(target);
    } else if ("POST".equalsIgnoreCase(reqMethod)) {
      HttpPost post = new HttpPost(target);
      post.setEntity(new ByteArrayEntity(body, org.apache.http.entity.ContentType.create(req.getContentType())));
      method = post;
    } else if ("PUT".equalsIgnoreCase(reqMethod)) {
      HttpPut put = new HttpPut(target);
      put.setEntity(new ByteArrayEntity(body, org.apache.http.entity.ContentType.create(req.getContentType())));
      method = put;
    } else if ("DELETE".equalsIgnoreCase(reqMethod)) {
      method = new HttpDelete(target);
    }
    return method;
  }

  private AuthToken getAuthTokenFrom(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    boolean isAdmin = isAdminRequest(req);
    AuthToken authToken = isAdmin ?
        getAdminAuthTokenFromCookie(req, resp, true) : getAuthTokenFromCookie(req, resp, true);
    if (authToken == null) {
      String zAuthTokenFromQueryParam = req.getParameter(QP_ZAUTHTOKEN);
      if (zAuthTokenFromQueryParam != null) {
        try {
          authToken = AuthProvider.getAuthToken(zAuthTokenFromQueryParam);
          if (authToken != null && authToken.isExpired()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "authtoken expired");
            return null;
          }
        } catch (AuthTokenException e) {
          resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "unable to parse authtoken");
          return null;
        }
      }
    }
    if (authToken == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "no authtoken cookie");
      return null;
    }
    if (isAdmin && !authToken.isAdmin()) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "permission denied");
      return null;
    }
    return authToken;
  }


  private void handleHttpResponse(HttpServletRequest req, HttpServletResponse resp, HttpRequestBase method,
      AuthToken authToken, String targetUrl) throws IOException {
    HttpResponse httpResp;
    try {
      HttpClientBuilder clientBuilder = HttpClients.custom();
      HttpProxyUtil.configureProxy(clientBuilder);
      if (!("POST".equalsIgnoreCase(req.getMethod()) || "PUT".equalsIgnoreCase(req.getMethod()))) {
        clientBuilder.setRedirectStrategy(new DefaultRedirectStrategy());
      }

      HttpClient client = clientBuilder.build();
      httpResp = HttpClientUtil.executeMethod(client, method);
    } catch (HttpException | IOException ex) {
      ZimbraLog.misc.info("exception while proxying " + targetUrl, ex);
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    int status = httpResp.getStatusLine() == null ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        : httpResp.getStatusLine().getStatusCode();

    Header ctHeader = httpResp.getFirstHeader("Content-Type");
    String contentType =
        ctHeader == null || ctHeader.getValue() == null ? DEFAULT_CONTENT_TYPE_HEADER_VALUE : ctHeader.getValue();

    InputStream targetResponseBody = null;
    HttpEntity targetResponseEntity = httpResp.getEntity();
    if (targetResponseEntity != null) {
      targetResponseBody = targetResponseEntity.getContent();
    }

    String uploadParam = req.getParameter(UPLOAD_PARAM);
    boolean asUpload = ("1".equals(uploadParam) || "true".equalsIgnoreCase(uploadParam));

    if (asUpload) {
      handleUploadResponse(req, resp, status, targetResponseBody, authToken, contentType, httpResp);
    } else {
      handleInlineResponse(resp, status, targetResponseBody, contentType, httpResp);
    }
  }

  private void handleUploadResponse(HttpServletRequest req, HttpServletResponse resp, int status,
      InputStream targetResponseBody, AuthToken authToken, String contentType, HttpResponse httpResp)
      throws IOException {
    String filename = req.getParameter(FILENAME_PARAM);
    if (filename == null || "".equals(filename)) {
      filename = new ContentType(contentType).getParameter("name");
    }
    if ((filename == null || "".equals(filename)) && httpResp.getFirstHeader("Content-Disposition") != null) {
      filename = new ContentDisposition(httpResp.getFirstHeader("Content-Disposition").getValue()).getParameter(
          FILENAME_PARAM);
    }
    if (filename == null || "".equals(filename)) {
      filename = "unknown";
    }

    List<Upload> uploads = null;

    if (targetResponseBody != null) {
      try {
        Upload up = FileUploadServlet.saveUpload(targetResponseBody, filename, contentType, authToken.getAccountId());
        uploads = List.of(up);
      } catch (ServiceException e) {
        if (e.getCode().equals(MailServiceException.UPLOAD_REJECTED)) {
          status = HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;
        } else {
          status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
      }
    }

    resp.setStatus(status);
    FileUploadServlet.sendResponse(resp, status, req.getParameter(FORMAT_PARAM), null, uploads, null);
  }

  private void handleInlineResponse(HttpServletResponse resp, int status, InputStream targetResponseBody,
      String contentType, HttpResponse httpResp) throws IOException {
    resp.setStatus(status);
    resp.setContentType(contentType);
    for (Header h : httpResp.getAllHeaders()) {
      if (canProxyHeader(h.getName())) {
        resp.addHeader(h.getName(), h.getValue());
      }
    }
    if (targetResponseBody != null) {
      ByteUtil.copy(targetResponseBody, true, resp.getOutputStream(), true);
    }
  }
}
