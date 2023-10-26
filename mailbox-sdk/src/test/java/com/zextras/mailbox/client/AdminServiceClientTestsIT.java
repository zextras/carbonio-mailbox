package com.zextras.mailbox.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;

import com.zextras.mailbox.client.admin.service.AdminServiceClient;
import com.zextras.mailbox.client.admin.service.AdminServiceRequests;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zimbraadmin.Attr;

class AdminServiceClientTestsIT {

  private final int PORT = 10_001;
  private final String authToken = "dummy-token";
  private final String email = "foo@test.domain.io";
  private final String id = "846a6715-d0c8-452c-885c-869f7892d3f0";
  private AdminServiceClient adminServiceClient;
  private MailboxServerSimulator mailboxServerSimulator;

  @BeforeEach
  void setUp() throws Exception {
    mailboxServerSimulator = MailboxServerSimulator.startAdminService(PORT);
    adminServiceClient = mailboxServerSimulator.createAdminServiceClient();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (mailboxServerSimulator != null) {
      mailboxServerSimulator.close();
    }
  }

  @Test
  void getAccountInfoByEmail() throws Exception {
    mailboxServerSimulator.setupServerFor("getAccountInfo_ByEmail");

    final var result =
        adminServiceClient.send(
            AdminServiceRequests.AccountInfo.byEmail(email).withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertEquals(id, readAttribute(result.getA(), "zimbraId"));
  }

  @Test
  void getAccountInfoById() throws Exception {
    mailboxServerSimulator.setupServerFor("getAccountInfo_ById");

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
}
