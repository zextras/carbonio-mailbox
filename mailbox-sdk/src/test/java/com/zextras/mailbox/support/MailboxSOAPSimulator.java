// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.support;

import com.zextras.mailbox.client.MailboxClient;
import com.zextras.mailbox.client.admin.service.AdminServiceClient;
import com.zextras.mailbox.client.service.ServiceClient;
import io.swagger.models.HttpMethod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.BinaryBody;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;

public class MailboxSOAPSimulator implements AutoCloseable {

  private static final String SERVICE_TYPE = "service";
  private static final String ADMIN_SERVICE_TYPE = "adminService";

  public final ClientAndServer mailboxMockServer;
  private final String type;
  private final String server;
  private final MailboxClient client;

  private MailboxSOAPSimulator(int port, String type) throws Exception {
    this.type = type;
    mailboxMockServer = ClientAndServer.startClientAndServer(port);
    setUpWsdlResponse();
    server = String.format("http://localhost:%s", port);
    client = new MailboxClient.Builder().withServer(server).build();
  }

  public static MailboxSOAPSimulator startService(int port) throws Exception {
    return new MailboxSOAPSimulator(port, SERVICE_TYPE);
  }

  public static MailboxSOAPSimulator startAdminService(int port) throws Exception {
    return new MailboxSOAPSimulator(port, ADMIN_SERVICE_TYPE);
  }

  public ServiceClient createServiceClient() {
    return client.newServiceClientBuilder().withServer(server).build();
  }

  public AdminServiceClient createAdminServiceClient() {
    return client.newAdminServiceClientBuilder().withServer(server).build();
  }

  public void setupServerFor(String request, String response) {
    mailboxMockServer
        .when(
            HttpRequest.request()
                .withMethod(HttpMethod.POST.toString())
                .withPath(soapUrl())
                .withBody(request))
        .respond(
            HttpResponse.response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withBody(response));
  }

  public ScenarioRequest request() {
    return new ScenarioRequest(xmlFolder());
  }

  public ScenarioResponse response() {
    return new ScenarioResponse(xmlFolder());
  }

  @Override
  public void close() throws Exception {
    if (mailboxMockServer != null) {
      mailboxMockServer.close();
    }
  }

  private void setUpWsdlResponse() throws IOException {
    final var wsdl = Files.readAllBytes(Path.of("schemas/ZimbraService.wsdl"));

    mailboxMockServer
        .when(
            HttpRequest.request()
                .withMethod(HttpMethod.GET.toString())
                .withPath("/service/wsdl/ZimbraService.wsdl"))
        .respond(HttpResponse.response().withStatusCode(200).withBody(BinaryBody.binary(wsdl)));
  }

  private String xmlFolder() {
    if (SERVICE_TYPE.equals(type)) {
      return "service";
    }
    return "adminService";
  }

  public String soapUrl() {
    if (SERVICE_TYPE.equals(type)) {
      return "/service/soap/";
    }
    return "/service/admin/soap/";
  }
}
