// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import static com.zextras.mailbox.util.MailboxTestUtil.SERVER_NAME;

import com.zextras.mailbox.util.JettyServerFactory;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.servlet.FirstServlet;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.mail.message.AutoCompleteRequest;
import com.zimbra.soap.mail.message.CreateContactRequest;
import com.zimbra.soap.mail.message.FullAutocompleteRequest;
import com.zimbra.soap.mail.type.ContactSpec;
import com.zimbra.soap.mail.type.NewContactAttr;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FullAutoCompleteTest {

  private static Server server;
  private static Provisioning provisioning;
  private static final int PORT = 8090;

  @BeforeAll
  static void beforeAll() throws Exception {
    MailboxTestUtil.setUp();
    provisioning = Provisioning.getInstance();
    provisioning.getServerByName(SERVER_NAME).modify(
        new HashMap<>(
            Map.of(
                Provisioning.A_zimbraMailPort, String.valueOf(PORT),
                ZAttrProvisioning.A_zimbraMailMode, "http",
                ZAttrProvisioning.A_zimbraPop3SSLServerEnabled, "FALSE",
                ZAttrProvisioning.A_zimbraImapSSLServerEnabled, "FALSE"
            )
        )
    );

    // See web.xml, Soap servlet calls Zimbra#startup which waits for FirstServlet initialization
    // We should consider refactoring the startup process

    final ServletHolder firstServlet = new ServletHolder(FirstServlet.class);
    firstServlet.setInitOrder(1);
    final ServletHolder soapServlet = new ServletHolder(SoapServlet.class);
    soapServlet.setInitParameter("engine.handler.0", "com.zimbra.cs.service.mail.MailService");
    soapServlet.setInitOrder(2);
    server = JettyServerFactory.create(PORT,
        Map.of( "/firstServlet", firstServlet,
            AccountConstants.USER_SERVICE_URI + "*", soapServlet));
    server.start();
  }

  @AfterAll
  static void afterAll() throws Exception {
    MailboxTestUtil.tearDown();
  }


  @Test
  void shouldReturnContactsOfAuthenticatedUserOnly() throws Exception {
    final Account account = MailboxTestUtil.createRandomAccountForDefaultDomain();
    final String contactEmail = "test@abc.com";
    final AutoCompleteRequest autoCompleteRequest = new AutoCompleteRequest(contactEmail);
    String soapUrl = URLUtil.getSoapURL(account.getServer(), true);
    final FullAutocompleteRequest fullAutocompleteRequest = new FullAutocompleteRequest(autoCompleteRequest);

    final ContactSpec contactSpec = new ContactSpec();
    contactSpec.addAttr(NewContactAttr.fromNameAndValue("email", contactEmail));

    // create contact
    final CreateContactRequest createContactRequest = new CreateContactRequest(contactSpec);
    final Element requestContact = JaxbUtil.jaxbToElement(createContactRequest);
    final HttpResponse contactResponse = doExecuteSoap(account, soapUrl, requestContact);
    Assertions.assertEquals(HttpStatus.SC_OK, contactResponse.getStatusLine().getStatusCode(), "API: Was unable to create contact " + contactEmail + " for account " + account.getId());

    // make the call
    final Element request = JaxbUtil.jaxbToElement(fullAutocompleteRequest);
    final HttpResponse execute = doExecuteSoap(account, soapUrl, request);
    Assertions.assertEquals(HttpStatus.SC_OK, execute.getStatusLine().getStatusCode());
    final String responseBody = new String(execute.getEntity().getContent().readAllBytes(),
        StandardCharsets.UTF_8);
    System.out.println("Received: " + responseBody);
  }

  private HttpResponse doExecuteSoap(Account account, String soapUrl, Element soapBody) throws Exception {
    AuthToken authToken = AuthProvider.getAuthToken(account);
    BasicCookieStore cookieStore = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(false), authToken.getEncoded());
    cookie.setDomain("localhost");
    cookie.setPath("/");
    cookieStore.addCookie(cookie);
    try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore)
        .build()) {
      final HttpPost httpPost = new HttpPost();
      final Element envelope = SoapProtocol.Soap12.soapEnvelope(soapBody);
      httpPost.setURI(URI.create(soapUrl));
      httpPost.setEntity(new StringEntity(envelope.toString()));
      return client.execute(httpPost);
    }
  }

}