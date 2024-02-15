// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.util;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.JaxbUtil;
import java.net.URI;
import java.util.Objects;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

/**
 * A SoapClient that wraps the Body in an Envelope.
 *
 */
public class SoapClient {

  /**
   * Endpoint in form of http://<host>:<port>/<basePath>
   */
  private String endpoint;

  public SoapClient(String endpoint) {
    this.endpoint = endpoint;
  }

  public static class Request {

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
      AuthToken authToken = AuthProvider.getAuthToken(caller, isAdminAccount());
      BasicCookieStore cookieStore = new BasicCookieStore();
      BasicClientCookie cookie =
          new BasicClientCookie(ZimbraCookie.authTokenCookieName(false), authToken.getEncoded());
      cookie.setDomain(caller.getServerName());
      cookie.setPath("/");
      cookieStore.addCookie(cookie);
      try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore)
          .build()) {
        final HttpPost httpPost = new HttpPost();
        Element envelope;
        if (Objects.isNull(requestedAccount)) {
          envelope = SoapProtocol.Soap12.soapEnvelope(soapBody);
        } else {
          final Element headerXml = Element.parseXML(String.format(
              "<context xmlns=\"urn:zimbra\"><account by=\"id\">%s</account></context>",
              requestedAccount.getId()));
          envelope = SoapProtocol.Soap12.soapEnvelope(soapBody, headerXml);
        }
        httpPost.setURI(URI.create(this.url));
        httpPost.setEntity(new StringEntity(envelope.toString()));
        return client.execute(httpPost);
      }
    }

    private boolean isAdminAccount() {
      return caller.isIsAdminAccount() || caller.isIsDelegatedAdminAccount();
    }
  }

  public Request newRequest() {
    return new Request().setBaseURL(this.endpoint);
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
