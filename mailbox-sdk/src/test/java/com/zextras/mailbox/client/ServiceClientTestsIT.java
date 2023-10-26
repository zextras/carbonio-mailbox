package com.zextras.mailbox.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zimbra.NamedValue;

class ServiceClientTestsIT {

  private final int PORT = 10_000;
  private final String authToken = "dummy-token";
  private final String email = "foo@test.domain.io";
  private final String id = "846a6715-d0c8-452c-885c-869f7892d3f0";
  private ServiceClient serviceClient;
  private MailboxServerSimulator mailboxServerSimulator;

  @BeforeEach
  void setUp() throws Exception {
    mailboxServerSimulator = MailboxServerSimulator.startService(PORT);
    serviceClient = mailboxServerSimulator.createServiceClient();
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
        serviceClient.send(ServiceRequests.AccountInfo.byEmail(email).withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertEquals(id, readAttribute(result.getAttr(), "zimbraId"));
  }

  @Test
  void getAccountInfoById() throws Exception {
    mailboxServerSimulator.setupServerFor("getAccountInfo_ById");

    final var result =
        serviceClient.send(ServiceRequests.AccountInfo.byId(id).withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertEquals(id, readAttribute(result.getAttr(), "zimbraId"));
  }

  private static String readAttribute(List<NamedValue> attributes, String name) {
    return attributes.stream()
        .filter(x -> Objects.equals(x.getName(), name))
        .findFirst()
        .get()
        .getValue();
  }

}
