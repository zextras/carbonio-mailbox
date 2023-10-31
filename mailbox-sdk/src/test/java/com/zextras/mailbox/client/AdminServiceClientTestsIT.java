// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.client.admin.service.AdminServiceClient;
import com.zextras.mailbox.client.admin.service.AdminServiceRequests;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import zimbraadmin.Attr;

class AdminServiceClientTestsIT {

  private final int PORT = 10_001;
  private final String authToken = "dummy-token";
  private final String email = "foo@test.domain.io";
  private final String id = "846a6715-d0c8-452c-885c-869f7892d3f0";
  private AdminServiceClient adminServiceClient;
  private MailboxSOAPSimulator mailboxSOAPSimulator;

  @BeforeEach
  void setUp() throws Exception {
    mailboxSOAPSimulator = MailboxSOAPSimulator.startAdminService(PORT);
    adminServiceClient = mailboxSOAPSimulator.createAdminServiceClient();
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
        adminServiceClient.send(
            AdminServiceRequests.AccountInfo.byEmail(email).withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertAttrEquals(id, "zimbraId", result.getA());
  }

  @Test
  void getAccountInfoById() throws Exception {
    mailboxSOAPSimulator.setupServerFor("getAccountInfo_ById");

    final var result =
        adminServiceClient.send(AdminServiceRequests.AccountInfo.byId(id).withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertAttrEquals(id, "zimbraId", result.getA());
  }

  public static void assertAttrEquals(String expected, String name, List<Attr> attributes) {
    final var attribute =
        attributes.stream().filter(x -> Objects.equals(x.getN(), name)).findFirst();
    if (attribute.isEmpty()) {
      throw new AssertionFailedError("Attribute not found: " + name);
    }
    assertEquals(expected, attribute.get().getValue());
  }
}
