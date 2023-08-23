// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.mime.ContentDisposition;
import com.zimbra.common.mime.ContentType;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.UserServlet;
import com.zimbra.cs.util.AccountUtil;
import io.vavr.control.Try;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.MessagingException;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

/**
 * This client makes HTTP calls to the Mailbox APIs. The endpoint is determined by the account from
 * we want to request information. It routes the call on the mailbox of requested account as Mailbox
 * is stateful
 *
 * <p>E.g.: user2 is on mailbox2, the request will be made to mailbox2 endpoint.
 */
public class MailboxHttpClient {

  private final Provisioning provisioning;

  public MailboxHttpClient(Provisioning provisioning) {
    this.provisioning = provisioning;
  }

  /**
   * Return the rest endpoint Url for content servlet using the provided account
   *
   * @param targetAccountId accountId of the requested account attachment
   * @param userServletRequest request for {@link UserServlet}
   * @return Try of attachment's URL {@link String} for content servlet
   */
  private String getUserServletResourceUrl(
      String targetAccountId, UserServletRequest userServletRequest) {
    return Try.of(
            () -> {
              final Account requestedAccount = provisioning.getAccountById(targetAccountId);
              final String baseUrl = AccountUtil.getBaseUri(requestedAccount);
              return baseUrl + UserServlet.SERVLET_PATH + "?" + userServletRequest.toString();
            })
        .get();
  }

  /**
   * Calls the UserServlet
   *
   * @param authToken token of authenticated user
   * @param accountId accountid of user servlet
   * @return http response from {@link UserServlet}
   * @throws HttpException
   * @throws IOException
   * @throws ServiceException
   * @throws MessagingException
   * @throws AuthTokenException
   */
  public UserServletResponse callUserServlet(
      AuthToken authToken, String accountId, UserServletRequest userServletRequest)
      throws HttpException, IOException, ServiceException, MessagingException, AuthTokenException {
    HttpGet request = new HttpGet(getUserServletResourceUrl(accountId, userServletRequest));
    final HttpResponse httpResponse = this.doSendRequest(authToken, request);
    Header contentDispositionHeader = httpResponse.getFirstHeader(CONTENT_DISPOSITION);
    Header contentTypeHeader = httpResponse.getFirstHeader(CONTENT_TYPE);
    String filename =
        contentDispositionHeader == null
            ? "unknown"
            : new ContentDisposition(contentDispositionHeader.getValue()).getParameter("filename");
    String contentType =
        contentTypeHeader == null
            ? "unknown"
            : new ContentType(contentTypeHeader.getValue()).getContentType();

    final InputStream content = httpResponse.getEntity().getContent();
    return new UserServletResponse(contentType, filename, content);
  }

  /**
   * Makes the Http call using the token as cookie.
   *
   * @param authToken Token used as cookie
   * @param request request to make
   * @return response from Mailbox
   * @throws ServiceException
   */
  private HttpResponse doSendRequest(AuthToken authToken, HttpGet request)
      throws ServiceException, HttpException, IOException, AuthTokenException {
    HttpClientBuilder clientBuilder =
        ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient();

    BasicCookieStore cookieStore = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(false), authToken.getEncoded());
    final String domain =
        authToken.getAccount().getServer().getAttr(ZAttrProvisioning.A_zimbraServiceHostname);
    cookie.setDomain(domain);
    cookie.setPath("/");
    cookieStore.addCookie(cookie);

    final HttpClient httpClient = clientBuilder.setDefaultCookieStore(cookieStore).build();
    return HttpClientUtil.executeMethod(httpClient, request);
  }
}
