package com.zextras.mailbox.client.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;

import com.zextras.mailbox.client.MailboxClient;
import com.zextras.mailbox.client.admin.service.AdminServiceClient;
import com.zextras.mailbox.client.admin.service.AdminServiceRequests;
import io.swagger.models.HttpMethod;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.BinaryBody;
import org.mockserver.model.HttpStatusCode;
import zimbraadmin.Attr;

class AdminServiceClientTestsIT {

  private final int PORT = 10_001;
  private final String authToken = "dummy-token";
  private final String email = "foo@test.domain.io";
  private final String id = "846a6715-d0c8-452c-885c-869f7892d3f0";
  private ClientAndServer mailboxMockServer;
  private AdminServiceClient adminServiceClient;

  @BeforeEach
  void setUp() throws Exception {
    mailboxMockServer = ClientAndServer.startClientAndServer(PORT);
    setUpWsdlResponse();
    setUpClient();
  }

  @AfterEach
  void tearDown() {
    if (mailboxMockServer != null) {
      mailboxMockServer.close();
    }
  }

  @Test
  void getAccountInfoByEmail() throws Exception {
    setupServerFor("getAccountInfo_ByEmail");

    final var result =
        adminServiceClient.send(
            AdminServiceRequests.AccountInfo.byEmail(email).withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertEquals(id, readAttribute(result.getA(), "zimbraId"));
  }

  @Test
  void getAccountInfoById() throws Exception {
    setupServerFor("getAccountInfo_ById");

    final var result =
        adminServiceClient.send(AdminServiceRequests.AccountInfo.byId(id).withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertEquals(id, readAttribute(result.getA(), "zimbraId"));
  }

  private static String readAttribute(List<Attr> attributes, String name) {
    return attributes.stream()
        .filter(x -> Objects.equals(x.getN(), name))
        .findFirst()
        .get()
        .getValue();
  }

  private void setupServerFor(String name) {
    final var request = requestFor(name);
    final var response = responseFor(name);

    mailboxMockServer
        .when(
            request()
                .withMethod(HttpMethod.POST.toString())
                .withPath("/service/admin/soap/")
                .withBody(request))
        .respond(response().withStatusCode(HttpStatusCode.OK_200.code()).withBody(response));
  }

  private String requestFor(String name) {
    return getXmlFile(fullPathFor(name, "request"));
  }

  private String responseFor(String name) {
    return getXmlFile(fullPathFor(name, "response"));
  }

  private String fullPathFor(String name, String type) {
    return String.format("soap/adminService/%s/%s.xml", name, type);
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

  private void setUpClient()
      throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException {
    String server = String.format("http://localhost:%s", PORT);
    final var client = new MailboxClient.Builder().withServer(server).build();

    adminServiceClient = client.newAdminServiceClientBuilder().withServer(server).build();
  }
}
