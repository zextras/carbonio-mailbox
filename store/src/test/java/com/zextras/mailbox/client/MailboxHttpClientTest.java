// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.zimbra.common.util.ZimbraCookie.COOKIE_ZM_AUTH_TOKEN;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.UserServlet;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Cookie;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

class MailboxHttpClientTest {

  private static final String testAccount = "test@test.com";
  private static final int MAILBOX_SERVER_PORT = 8080;
  private static Provisioning provisioning;
  private static ClientAndServer mailboxServer;

  @BeforeAll
  public static void setUpAll() throws Exception {
    MailboxTestUtil.initServer();
    mailboxServer = startClientAndServer(MAILBOX_SERVER_PORT);
    provisioning = Provisioning.getInstance();
    final Server server =
        provisioning.createServer(
            "test",
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraServiceHostname, "localhost");
                put(ZAttrProvisioning.A_zimbraMailMode, "http");
                put(ZAttrProvisioning.A_zimbraMailPort, String.valueOf(MAILBOX_SERVER_PORT));
              }
            });
    provisioning.createAccount(
        testAccount,
        "test",
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraMailHost, server.getName());
          }
        });
  }

  @AfterAll
  public static void tearDown() throws IOException {
    mailboxServer.stop();
  }

  @Test
  void shouldCallCorrectMailboxAndGetAttachment() throws Exception {
    final MailboxHttpClient mailboxHttpClient = new MailboxHttpClient(provisioning);

    final Account account = provisioning.getAccountByName(testAccount);
    final AuthToken token = AuthProvider.getAuthToken(account);
    final String accountUuid = account.getId();
    final Cookie cookie = new Cookie(COOKIE_ZM_AUTH_TOKEN, token.getEncoded());
    final byte[] expectedContent = "hello".getBytes(StandardCharsets.UTF_8);
    final String auth = "co";
    final String messageId = "1";
    final String part = "2";
    final String fileName = "hello.txt";
    final String contentType = ContentType.TEXT_PLAIN.getMimeType();
    final HttpRequest expectedRequest =
        request()
            .withPath(UserServlet.SERVLET_PATH + "/" + testAccount + "/")
            .withCookie(cookie)
            .withQueryStringParameter(UserServlet.QP_AUTH, auth)
            .withQueryStringParameter(UserServlet.QP_ID, messageId)
            .withQueryStringParameter(UserServlet.QP_PART, part);
    final HttpResponse expectedResponse =
        HttpResponse.response()
            .withBody(expectedContent)
            .withHeader(CONTENT_DISPOSITION, "inline; filename=" + fileName)
            .withHeader(CONTENT_TYPE, contentType + "; name=" + fileName);
    mailboxServer.when(expectedRequest).respond(expectedResponse.withStatusCode(200));
    final UserServletResponse userServletResponse =
        mailboxHttpClient
            .dolUserServletGetRequest(
                token, accountUuid, UserServletRequest.buildRequest(auth, messageId, part))
            .get();
    mailboxServer.verify(1, expectedRequest);
    Assertions.assertEquals(fileName, userServletResponse.getFileName());
    Assertions.assertEquals(contentType, userServletResponse.getContentType());
    Assertions.assertArrayEquals(expectedContent, userServletResponse.getContent().readAllBytes());
  }

  @Test
  void shouldReturnFailureWhenNot200() throws Exception {

    final MailboxHttpClient mailboxHttpClient = new MailboxHttpClient(provisioning);

    final Account account = provisioning.getAccountByName(testAccount);
    final AuthToken token = AuthProvider.getAuthToken(account);
    final String accountUuid = account.getId();
    final Cookie cookie = new Cookie(COOKIE_ZM_AUTH_TOKEN, token.getEncoded());
    final String auth = "co";
    final String messageId = "1";
    final String part = "2";
    final HttpRequest expectedRequest =
        request()
            .withPath(UserServlet.SERVLET_PATH)
            .withCookie(cookie)
            .withQueryStringParameter(UserServlet.QP_AUTH, auth)
            .withQueryStringParameter(UserServlet.QP_ID, messageId)
            .withQueryStringParameter(UserServlet.QP_PART, part);

    final HttpResponse expectedResponse =
        HttpResponse.response().withStatusCode(HttpStatus.SC_BAD_REQUEST);

    mailboxServer.when(expectedRequest).respond(expectedResponse);
    final Try<UserServletResponse> userServletResponse =
        mailboxHttpClient.dolUserServletGetRequest(
            token, accountUuid, UserServletRequest.buildRequest(auth, messageId, part));
    Assertions.assertTrue(userServletResponse.isFailure());
  }
}
