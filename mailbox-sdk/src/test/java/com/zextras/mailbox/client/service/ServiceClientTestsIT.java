package com.zextras.mailbox.client.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;

import com.zextras.mailbox.client.MailboxClient;
import io.swagger.models.HttpMethod;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.BinaryBody;
import org.mockserver.model.HttpStatusCode;

class ServiceClientTestsIT {

  private final int PORT = 10_000;
  private ClientAndServer mailboxMockServer;
  private ServiceClient serviceClient;

  @BeforeEach
  void setUp() throws Exception {
    mailboxMockServer = ClientAndServer.startClientAndServer(PORT);
    setUpWsdlResponse();

    final var client =
        new MailboxClient.Builder()
            .withServer(String.format("http://localhost:%s", PORT))
            .trustAllCertificates()
            .build();

    serviceClient = client.newServiceClient();
  }

  @AfterEach
  void tearDown() {
    if (mailboxMockServer != null) {
      mailboxMockServer.close();
    }
  }

  private void setUpWsdlResponse() throws IOException {
    final var wsdl =
        Files.readAllBytes(
            Path.of("../", "soap/target/classes/com/zimbra/soap/ZimbraService.wsdl"));

    mailboxMockServer
        .when(
            request()
                .withMethod(HttpMethod.GET.toString())
                .withPath("/service/wsdl/ZimbraService.wsdl"))
        .respond(response().withStatusCode(200).withBody(BinaryBody.binary(wsdl)));
  }

  @Test
  void getAccountInfo() throws Exception {
//    final var request = getXmlFile("soap/GetAccountInfoRequest.xml");
    final var response = getXmlFile("soap/GetAccountInfoResponse.xml");

    mailboxMockServer
        .when(
            request()
                .withMethod(HttpMethod.POST.toString())
                .withPath("/service/soap/")
//                .withBody(request)
        )
        .respond(
            response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withBody(response)
        );

    final var token = "dummy-token";
    final var email = "foo@test.domain.io";

    final var accountInfo2 =
        serviceClient.send(ServiceRequests.AccountInfo.byEmail(email).withAuthToken(token));

    assertEquals(email , accountInfo2.getName());

//    HttpRequest[] httpRequests = mailboxMockServer.retrieveRecordedRequests(null);
//    HttpRequest httpRequest = httpRequests[1];
  }

  private String getXmlFile(String path) {
    try (InputStream resource = getClass().getClassLoader().getResourceAsStream(path)) {
      return IOUtils.toString(resource, StandardCharsets.UTF_8)
//          .replaceAll("\n( *)<", "<") // This replacement is necessary to remove the XML indentation
          .replaceAll(">[\\s]+<", "><")
      ;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void mockServerWorksHttps() throws Exception {
    final var httpRequest =
        request()
            .withSecure(true) // Abilita HTTPS
            .withMethod("GET")
            .withPath("/example");
    final var httpResponse =
        response()
            .withStatusCode(HttpStatusCode.OK_200.code())
            .withBody("Hello, World!");
    mailboxMockServer.when(httpRequest).respond(httpResponse);
  }
}
