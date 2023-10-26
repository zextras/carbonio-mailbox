package com.zextras.mailbox.client;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.zextras.mailbox.client.admin.service.AdminServiceClient;
import io.swagger.models.HttpMethod;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.IOUtils;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.BinaryBody;
import org.mockserver.model.HttpStatusCode;

public class MailboxServerSimulator implements AutoCloseable {

  private static final String SERVICE_TYPE = "service";
  private static final String ADMIN_SERVICE_TYPE = "adminService";

  private final int port;
  private final ClientAndServer mailboxMockServer;
  private final String type;
  private final String server;
  private final MailboxClient client;

  private MailboxServerSimulator(int port, String type) throws Exception {
    this.port = port;
    this.type = type;
    mailboxMockServer = ClientAndServer.startClientAndServer(port);
    setUpWsdlResponse();
    server = String.format("http://localhost:%s", this.port);
    client = new MailboxClient.Builder().withServer(server).build();
  }

  public static MailboxServerSimulator startService(int port) throws Exception {
    return new MailboxServerSimulator(port, SERVICE_TYPE);
  }

  public static MailboxServerSimulator startAdminService(int port) throws Exception {
    return new MailboxServerSimulator(port, ADMIN_SERVICE_TYPE);
  }

  public ServiceClient createServiceClient() {
    return client.newServiceClientBuilder().withServer(server).build();
  }

  public AdminServiceClient createAdminServiceClient() {
    return client.newAdminServiceClientBuilder().withServer(server).build();
  }

  public void setupServerFor(String name) {
    mailboxMockServer
        .when(
            request()
                .withMethod(HttpMethod.POST.toString())
                .withPath(soapUrl())
                .withBody(requestFor(name)))
        .respond(
            response().withStatusCode(HttpStatusCode.OK_200.code()).withBody(responseFor(name)));
  }

  @Override
  public void close() throws Exception {
    if (mailboxMockServer != null) {
      mailboxMockServer.close();
    }
  }

  private String requestFor(String name) {
    return getXmlFile(fullPathFor(name, "request"));
  }

  private String responseFor(String name) {
    return getXmlFile(fullPathFor(name, "response"));
  }

  private String fullPathFor(String name, String type) {
    return String.format("soap/%s/%s/%s.xml", xmlFolder(), name, type);
  }

  private String getXmlFile(String path) {
    try (InputStream resource = getClass().getClassLoader().getResourceAsStream(path)) {
      return IOUtils.toString(resource, StandardCharsets.UTF_8)
          // This replacement is necessary to remove the indentation and new lines
          .replaceAll(">\\s+<", "><")
          // This replacement is necessary to remove the end of file new line
          .replaceAll("\n", "");
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

  private String xmlFolder() {
    if (SERVICE_TYPE.equals(type)) {
      return "service";
    }
    return "adminService";
  }

  private String soapUrl() {
    if (SERVICE_TYPE.equals(type)) {
      return "/service/soap/";
    }
    return "/service/admin/soap/";
  }
}
