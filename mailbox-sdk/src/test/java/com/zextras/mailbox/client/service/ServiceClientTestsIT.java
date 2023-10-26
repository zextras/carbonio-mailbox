package com.zextras.mailbox.client.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
  private final String authToken = "dummy-token";
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
  void getAccountInfoByEmail() throws Exception {
    final var request = getXmlFile("soap/getAccountInfoByEmail/request.xml");
    final var response = getXmlFile("soap/getAccountInfoByEmail/response.xml");

    mailboxMockServer
        .when(
            request()
                .withMethod(HttpMethod.POST.toString())
                .withPath("/service/soap/")
                .withBody(request))
        .respond(response().withStatusCode(HttpStatusCode.OK_200.code()).withBody(response));

    final var email = "foo@test.domain.io";

    final var result =
        serviceClient.send(ServiceRequests.AccountInfo.byEmail(email).withAuthToken(authToken));

    assertEquals(email, result.getName());
  }

  private String getXmlFile(String path) {
    try (InputStream resource = getClass().getClassLoader().getResourceAsStream(path)) {
      return IOUtils.toString(resource, StandardCharsets.UTF_8)
          // This replacement is necessary to remove the indentation and new lines
          .replaceAll(">\\s+<", "><");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
