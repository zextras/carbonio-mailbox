// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.util;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.soap.XmlParseException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.JaxbUtil;
import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Objects;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;

/** A SoapClient that wraps the Body in an Envelope. */
public class SoapClient implements Closeable {

  /** Endpoint in form of http://<host>:<port>/<basePath> */
  private final String endpoint;

  private final BasicCookieStore cookieStore;
  private final CloseableHttpClient client;

  public SoapClient(String endpoint) {
    this.endpoint = endpoint;
    cookieStore = new BasicCookieStore();
    client =
        HttpClients.custom()
            .setConnectionManager(createConnectionManager())
            .setDefaultCookieStore(cookieStore)
            .build();
  }

  private static HttpClientConnectionManager createConnectionManager() {
    final var connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(100);
    connectionManager.setDefaultMaxPerRoute(100);
    return connectionManager;
  }

  @Override
  public void close() throws IOException {
    client.close();
  }

  public static class Request {

    private final BasicCookieStore cookieStore;
    private final HttpClient client;

    public Request(BasicCookieStore cookieStore, HttpClient client) {

      this.cookieStore = cookieStore;
      this.client = client;
    }

    public Request setSoapBody(Element soapBody) {
      this.soapBody = soapBody;
      return this;
    }

    public Request setSoapBody(Object soapPOJO) throws ServiceException {
      this.soapBody = JaxbUtil.jaxbToElement(soapPOJO);
      return this;
    }

    public Request setCaller(Account caller) {
      this.caller = caller;
      return this;
    }

    private Request setBaseURL(String baseURL) {
      this.url = baseURL;
      return this;
    }

    public Request setRequestedAccount(Account requestedAccount) {
      this.requestedAccount = requestedAccount;
      return this;
    }

    private Element soapBody;
    private Account caller;
    private Account requestedAccount;
    private String url = "/";

    public HttpResponse execute() throws Exception {

      if (!Objects.isNull(caller)) {
        cookieStore.clear();
        cookieStore.addCookie(createAuthCookie());
      }

      final HttpPost httpPost = new HttpPost();
      httpPost.setURI(URI.create(this.url));
      httpPost.setEntity(createEnvelop());
      return client.execute(httpPost);
    }

    private StringEntity createEnvelop() throws XmlParseException, UnsupportedEncodingException {
      Element envelope;
      if (Objects.isNull(requestedAccount)) {
        envelope = SoapProtocol.Soap12.soapEnvelope(soapBody);
      } else {
        final Element headerXml =
            Element.parseXML(
                String.format(
                    "<context xmlns=\"urn:zimbra\"><account by=\"id\">%s</account></context>",
                    requestedAccount.getId()));
        envelope = SoapProtocol.Soap12.soapEnvelope(soapBody, headerXml);
      }
      return new StringEntity(envelope.toString());
    }

    private BasicClientCookie createAuthCookie() throws AuthTokenException, ServiceException {
      final var authToken = AuthProvider.getAuthToken(caller, isAdminAccount());
      final var name = ZimbraCookie.authTokenCookieName(false);
      final var cookie = new BasicClientCookie(name, authToken.getEncoded());
      cookie.setDomain(caller.getServerName());
      cookie.setPath("/");
      return cookie;
    }

    private boolean isAdminAccount() {
      return caller.isIsAdminAccount() || caller.isIsDelegatedAdminAccount();
    }
  }

  public Request newRequest() {
    return new Request(cookieStore, client).setBaseURL(this.endpoint);
  }

  /**
   * Executes a sop request against account server.
   *
   * @param account authenticated account
   * @param soapBody body of soap request, without envelope
   * @return
   * @throws Exception
   */
  public HttpResponse executeSoap(Account account, Element soapBody) throws Exception {
    return newRequest().setCaller(account).setSoapBody(soapBody).execute();
  }

  /**
   * @param account
   * @param soapBodyPOJO
   * @return
   * @throws Exception
   */
  public HttpResponse executeSoap(Account account, Object soapBodyPOJO) throws Exception {
    return executeSoap(account, JaxbUtil.jaxbToElement(soapBodyPOJO));
  }
}
