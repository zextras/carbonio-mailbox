// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.admin;

import static com.zimbra.common.service.ServiceException.AUTH_REQUIRED;
import static com.zimbra.common.service.ServiceException.PERM_DENIED;

import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.servlet.FirstServlet;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.admin.message.DeleteAccountRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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

  private HttpClient createHttpClientForAdminAccount(Account account) throws Exception {
    final AuthToken authToken = AuthProvider.getAuthToken(account, true);
    BasicCookieStore cookieStore = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(true), authToken.getEncoded());
    cookie.setDomain("localhost");
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    return HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
  }

  @Test
  void shouldDeleteUserAsAdmin() throws Exception {
    final Account adminAccount = MailboxTestUtil.createBasicAccount();
    final Account toDelete = MailboxTestUtil.createBasicAccount();
    adminAccount.modify(new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE")));
    final HttpPost httpPost = new HttpPost("http://localhost:" + ADMIN_PORT);
    final Element element = JaxbUtil.jaxbToElement(new DeleteAccountRequest(toDelete.getId()));
    Element envelope = SoapProtocol.Soap12.soapEnvelope(element, null);
    httpPost.setEntity(new StringEntity(envelope.toString()));
    final HttpClient httpClientForAccount = createHttpClientForAdminAccount(adminAccount);
    final HttpResponse response = httpClientForAccount.execute(httpPost);
    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
  }

  @Test
  void shouldGet500WithAuthRequiredIfNoToken() throws Exception {
    final Account toDelete = MailboxTestUtil.createBasicAccount();
    final HttpPost httpPost = new HttpPost("http://localhost:" + ADMIN_PORT);
    final Element element = JaxbUtil.jaxbToElement(new DeleteAccountRequest(toDelete.getId()));
    Element envelope = SoapProtocol.Soap12.soapEnvelope(element, null);
    httpPost.setEntity(new StringEntity(envelope.toString()));
    final HttpClient httpClientForAccount = HttpClientBuilder.create().build();
    final HttpResponse response = httpClientForAccount.execute(httpPost);
    final String responseEnvelope = new String(response.getEntity().getContent().readAllBytes());
    Assertions.assertTrue(responseEnvelope.contains("<Code>" + AUTH_REQUIRED + "</Code>"));
    Assertions.assertEquals(
        HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
  }

  @Test
  void shouldGet500WithPerDeniedIfNotAnAdmin() throws Exception {
    final Account standardAccount = MailboxTestUtil.createBasicAccount();
    final Account toDelete = MailboxTestUtil.createBasicAccount();
    final HttpPost httpPost = new HttpPost("http://localhost:" + ADMIN_PORT);
    final Element element = JaxbUtil.jaxbToElement(new DeleteAccountRequest(toDelete.getId()));
    Element envelope = SoapProtocol.Soap12.soapEnvelope(element, null);
    httpPost.setEntity(new StringEntity(envelope.toString()));
    final HttpClient httpClientForAccount = createHttpClientForAdminAccount(standardAccount);
    final HttpResponse response = httpClientForAccount.execute(httpPost);
    final String responseEnvelope = new String(response.getEntity().getContent().readAllBytes());
    Assertions.assertTrue(responseEnvelope.contains("<Code>" + PERM_DENIED + "</Code>"));
    Assertions.assertEquals(
        HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
  }
}
