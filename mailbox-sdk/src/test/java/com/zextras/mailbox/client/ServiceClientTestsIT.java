// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.client.service.ServiceClient;
import com.zextras.mailbox.client.service.ServiceRequests;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import zimbra.NamedValue;

class ServiceClientTestsIT {

  private final int PORT = 10_000;
  private final String authToken = "dummy-token";
  private final String email = "foo@test.domain.io";
  private final String id = "846a6715-d0c8-452c-885c-869f7892d3f0";
  private ServiceClient serviceClient;
  private MailboxSOAPSimulator mailboxSOAPSimulator;

  @BeforeEach
  void setUp() throws Exception {
    mailboxSOAPSimulator = MailboxSOAPSimulator.startService(PORT);
    serviceClient = mailboxSOAPSimulator.createServiceClient();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (mailboxSOAPSimulator != null) {
      mailboxSOAPSimulator.close();
    }
  }

  @Test
  void getAccountInfoByEmail() throws Exception {
    mailboxSOAPSimulator.setupServerFor("getAccountInfo_ByEmail");

    final var result =
        serviceClient.send(ServiceRequests.AccountInfo.byEmail(email).withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertAttributeEquals(id, "zimbraId", result.getAttr());
  }

  @Test
  void getAccountInfoById() throws Exception {
    mailboxSOAPSimulator.setupServerFor("getAccountInfo_ById");

    final var result =
        serviceClient.send(ServiceRequests.AccountInfo.byId(id).withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertAttributeEquals(id, "zimbraId", result.getAttr());
  }

  @Test
  void getInfoAllSections() throws Exception {
    mailboxSOAPSimulator.setupServerFor("getInfo_AllSections");

    final var result =
        serviceClient.send(ServiceRequests.Info.allSections().withAuthToken(authToken));

    assertEquals(email, result.getName());
  }

  public static void assertAttributeEquals(
      String expected, String name, List<NamedValue> attributes) {
    assertEquals(expected, readAttribute(attributes, name));
  }

  private static String readAttribute(List<NamedValue> attributes, String name) {
    Optional<NamedValue> attribute =
        attributes.stream().filter(x -> Objects.equals(x.getName(), name)).findFirst();
    if (attribute.isEmpty()) {
      throw new AssertionFailedError("Attribute not found: " + name);
    }
    return attribute.get().getValue();
  }
}
