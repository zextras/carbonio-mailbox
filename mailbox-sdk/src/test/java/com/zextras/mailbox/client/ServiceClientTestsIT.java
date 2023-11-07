// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.client.service.InfoRequests.Sections;
import com.zextras.mailbox.client.service.ServiceClient;
import com.zextras.mailbox.client.service.ServiceRequests;
import com.zextras.mailbox.support.MailboxSOAPSimulator;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import zimbra.NamedValue;
import zimbraaccount.Attr;
import zimbraaccount.GetInfoResponse;

class ServiceClientTestsIT {

  private final int PORT = 10_000;
  private final String authToken = "dummy-token";
  private final String email = "foo@test.domain.io";
  private final String id = "846a6715-d0c8-452c-885c-869f7892d3f0";
  private final String domain = "test.domain.io";
  private final String publicUrl = "http://server." + domain;
  private final String name = "foo";
  private final String locale = "pt_BR";

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
    mailboxSOAPSimulator.setupServerFor(
        mailboxSOAPSimulator.request().getAccountInfo_ByEmail(authToken, email),
        mailboxSOAPSimulator.response().getAccountInfo_ByEmail(id, email, domain, name));

    final var result =
        serviceClient.send(ServiceRequests.AccountInfo.byEmail(email).withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertEquals(publicUrl, result.getPublicURL());
    assertNamedValueEquals(id, "zimbraId", result.getAttr());
    assertNamedValueEquals(name, "displayName", result.getAttr());
  }

  @Test
  void getAccountInfoById() throws Exception {
    mailboxSOAPSimulator.setupServerFor(
        mailboxSOAPSimulator.request().getAccountInfo_ById(authToken, id),
        mailboxSOAPSimulator.response().getAccountInfo_ById(id, email, domain, name));

    final var result =
        serviceClient.send(ServiceRequests.AccountInfo.byId(id).withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertEquals(publicUrl, result.getPublicURL());
    assertNamedValueEquals(id, "zimbraId", result.getAttr());
    assertNamedValueEquals(name, "displayName", result.getAttr());
  }

  @Test
  void getInfoAllSections() throws Exception {
    mailboxSOAPSimulator.setupServerFor(
        mailboxSOAPSimulator.request().getInfo_AllSections(authToken),
        mailboxSOAPSimulator.response().getInfo_AllSections(id, email, domain, name, locale));

    final var result =
        serviceClient.send(ServiceRequests.Info.allSections().withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertEquals(publicUrl, result.getPublicURL());
    assertAttrEquals(id, "zimbraId", result.getAttrs().getAttr());
    assertAttrEquals(name, "displayName", result.getAttrs().getAttr());
    assertPrefsEquals(locale, "zimbraPrefLocale", result.getPrefs());
  }

  @Test
  void getInfoSomeSections() throws Exception {
    mailboxSOAPSimulator.setupServerFor(
        mailboxSOAPSimulator.request().getInfo_SomeSections(authToken),
        mailboxSOAPSimulator.response().getInfo_SomeSections(id, email, domain, name));

    final var result =
        serviceClient.send(
            ServiceRequests.Info.sections(Sections.children, Sections.attrs)
                .withAuthToken(authToken));

    assertEquals(email, result.getName());
    assertEquals(publicUrl, result.getPublicURL());
    assertAttrEquals(id, "zimbraId", result.getAttrs().getAttr());
    assertAttrEquals(name, "displayName", result.getAttrs().getAttr());
  }

  private static void assertNamedValueEquals(
      String expected, String name, List<NamedValue> attributes) {
    final var attribute =
        attributes.stream().filter(x -> Objects.equals(x.getName(), name)).findFirst();
    if (attribute.isEmpty()) {
      throw new AssertionFailedError("Attribute not found: " + name);
    }
    assertEquals(expected, attribute.get().getValue());
  }

  private static void assertAttrEquals(String expected, String name, List<Attr> attributes) {
    final var attribute =
        attributes.stream().filter(x -> Objects.equals(x.getName(), name)).findFirst();
    if (attribute.isEmpty()) {
      throw new AssertionFailedError("Attribute not found: " + name);
    }
    assertEquals(expected, attribute.get().getValue());
  }

  private void assertPrefsEquals(String expected, String name, GetInfoResponse.Prefs prefs) {
    final var pref =
        prefs.getPref().stream().filter(x -> Objects.equals(x.getName(), name)).findFirst();
    if (pref.isEmpty()) {
      throw new AssertionFailedError("Pref not found: " + name);
    }
    assertEquals(expected, pref.get().getValue());
  }
}
