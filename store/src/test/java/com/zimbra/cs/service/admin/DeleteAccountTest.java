// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.admin;

import static com.zimbra.common.service.ServiceException.AUTH_REQUIRED;
import static com.zimbra.common.service.ServiceException.PERM_DENIED;
import static com.zimbra.cs.account.AccountServiceException.NO_SUCH_ACCOUNT;

import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.AuthProviderException;
import com.zimbra.cs.servlet.FirstServlet;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.admin.message.DeleteAccountRequest;
import com.zimbra.soap.admin.message.GetAccountRequest;
import com.zimbra.soap.type.AccountSelector;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DeleteAccountTest {

  private static Server mailboxServer;
  private static final int ADMIN_PORT = 7071;

  @BeforeAll
  static void setUp() throws Exception {
    System.setProperty("java.library.path", new File("./../").getAbsolutePath() + "/native/target");
    MailboxTestUtil.setUp();
    final ServletHolder firstServlet = new ServletHolder("FirstServlet", FirstServlet.class);
    firstServlet.setInitOrder(0);
    final ServletHolder adminServlet = new ServletHolder("AdminServlet", SoapServlet.class);
    adminServlet.setInitParameter("allowed.ports", Integer.toString(ADMIN_PORT));
    adminServlet.setInitOrder(1);
    adminServlet.setInitParameter("engine.handler.0", "com.zimbra.cs.service.admin.AdminService");
    mailboxServer =
        JettyServerFactory.create(
            ADMIN_PORT,
            Map.of(
                "/*", adminServlet,
                "/firstServlet/*", firstServlet));
    mailboxServer.start();
  }

  @AfterAll
  static void tearDown() throws Exception {
    mailboxServer.stop();
    MailboxTestUtil.tearDown();
  }

  private HttpClientBuilder client() {
    return HttpClientBuilder.create();
  }

  private CookieStore getCookie(Account account) throws AuthProviderException, AuthTokenException {
    final AuthToken authToken = AuthProvider.getAuthToken(account, true);
    BasicCookieStore cookieStore = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(true), authToken.getEncoded());
    cookie.setDomain("localhost");
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    return cookieStore;
  }

  private HttpPost deleteRequest(String toDeleteId)
      throws ServiceException, UnsupportedEncodingException {
    final HttpPost httpPost = new HttpPost("http://localhost:" + ADMIN_PORT);
    final Element element = JaxbUtil.jaxbToElement(new DeleteAccountRequest(toDeleteId));
    Element envelope = SoapProtocol.Soap12.soapEnvelope(element, null);
    httpPost.setEntity(new StringEntity(envelope.toString()));
    return httpPost;
  }

  private HttpResponse getAccountAsAdmin(Account account, String accountName) throws Exception {
    final HttpClient httpClient = this.client().setDefaultCookieStore(getCookie(account)).build();
    final GetAccountRequest getAccountRequest = new GetAccountRequest();
    final HttpPost httpPost = new HttpPost("http://localhost:" + ADMIN_PORT);
    getAccountRequest.setAccount(AccountSelector.fromName(accountName));
    final Element element = JaxbUtil.jaxbToElement(getAccountRequest);
    Element envelope = SoapProtocol.Soap12.soapEnvelope(element, null);
    httpPost.setEntity(new StringEntity(envelope.toString()));
    return httpClient.execute(httpPost);
  }

  // TODO: test with delegated admin

  @Test
  void shouldDeleteUserAsGlobalAdmin() throws Exception {
    final Account adminAccount = MailboxTestUtil.createBasicAccount();
    final Account toDelete = MailboxTestUtil.createBasicAccount();
    final String toDeleteId = toDelete.getId();
    final String toDeleteName = toDelete.getName();
    adminAccount.modify(new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE")));
    final HttpResponse response =
        this.client()
            .setDefaultCookieStore(getCookie(adminAccount))
            .build()
            .execute(deleteRequest(toDeleteId));
    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    final HttpResponse getDeletedAccount = this.getAccountAsAdmin(adminAccount, toDeleteName);
    Assertions.assertEquals(
        HttpStatus.SC_INTERNAL_SERVER_ERROR, getDeletedAccount.getStatusLine().getStatusCode());
    final String getAccountXML =
        new String(getDeletedAccount.getEntity().getContent().readAllBytes());
    Assertions.assertTrue(getAccountXML.contains("<Code>" + NO_SUCH_ACCOUNT + "</Code>"));
  }

  @Test
  void shouldGet500WithAuthRequiredIfNoToken() throws Exception {
    final Account toDelete = MailboxTestUtil.createBasicAccount();
    final HttpResponse response = this.client().build().execute(deleteRequest(toDelete.getId()));
    final String responseEnvelope = new String(response.getEntity().getContent().readAllBytes());
    Assertions.assertTrue(responseEnvelope.contains("<Code>" + AUTH_REQUIRED + "</Code>"));
    Assertions.assertEquals(
        HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
  }

  @Test
  void shouldGet500WithPermDeniedIfNotAnAdmin() throws Exception {
    final Account standardAccount = MailboxTestUtil.createBasicAccount();
    final Account toDelete = MailboxTestUtil.createBasicAccount();

    final HttpResponse response =
        this.client()
            .setDefaultCookieStore(this.getCookie(standardAccount))
            .build()
            .execute(deleteRequest(toDelete.getId()));
    final String responseEnvelope = new String(response.getEntity().getContent().readAllBytes());
    Assertions.assertTrue(responseEnvelope.contains("<Code>" + PERM_DENIED + "</Code>"));
    Assertions.assertEquals(
        HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
  }
}
