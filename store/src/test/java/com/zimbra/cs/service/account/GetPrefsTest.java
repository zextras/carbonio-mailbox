package com.zimbra.cs.service.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Maps;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.GetPrefsRequest;
import com.zimbra.soap.account.message.GetPrefsResponse;
import com.zimbra.soap.account.type.Pref;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GetPrefsTest {

  public static final String TEST_DOMAIN = "test.com";

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> attrs = Maps.newHashMap();
    prov.createDomain(TEST_DOMAIN, attrs);
  }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  private Account createGetRandomAccount() throws ServiceException {
    return Provisioning.getInstance()
        .createAccount(UUID.randomUUID() + "@" + TEST_DOMAIN, "secret", Maps.newHashMap());
  }

  @Test
  void shouldReturnEmptyResponseWhenNoPrefsAreSet() throws Exception {
    Account account = createGetRandomAccount();

    GetPrefsRequest request = new GetPrefsRequest();
    request.setPref(List.of());
    Element req = JaxbUtil.jaxbToElement(request);
    Element handle = new GetPrefs().handle(req, ServiceTestUtil.getRequestContext(account));
    GetPrefsResponse response = JaxbUtil.elementToJaxb(handle);

    assertTrue(Objects.requireNonNull(response).getPref().isEmpty());
  }

  @Test
  void shouldNotReturnPrefWhenNotSetButAsked() throws Exception {
    Account account = createGetRandomAccount();

    GetPrefsRequest request = new GetPrefsRequest();
    request.setPref(List.of(new Pref(Provisioning.A_carbonioPrefWebUiDarkMode)));
    Element req = JaxbUtil.jaxbToElement(request);
    Element handle = new GetPrefs().handle(req, ServiceTestUtil.getRequestContext(account));
    GetPrefsResponse response = JaxbUtil.elementToJaxb(handle);

    assertTrue(Objects.requireNonNull(response).getPref().isEmpty());
  }

  @Test
  void shouldReturnPrefWhenSetAndAsked() throws Exception {
    Account account = createGetRandomAccount();
    account.setCarbonioPrefWebUiDarkMode(true);

    GetPrefsRequest request = new GetPrefsRequest();
    request.setPref(List.of(new Pref(Provisioning.A_carbonioPrefWebUiDarkMode)));
    Element req = JaxbUtil.jaxbToElement(request);
    Element handle = new GetPrefs().handle(req, ServiceTestUtil.getRequestContext(account));
    GetPrefsResponse response = JaxbUtil.elementToJaxb(handle);

    assertEquals(1, Objects.requireNonNull(response).getPref().size());
    assertEquals(Provisioning.A_carbonioPrefWebUiDarkMode, response.getPref().get(0).getName());
  }

  @Test
  void shouldReturnMultiCardinalPrefWhenSetAndAsked() throws Exception {
    Account account = createGetRandomAccount();

    GetPrefsRequest request = new GetPrefsRequest();

    String[] timezones = {"America/Los_Angeles", "Asia/Kolkata", "Europe/Paris"};
    account.setPrefTimeZoneId(timezones);

    request.setPref(List.of(new Pref(Provisioning.A_zimbraPrefTimeZoneId)));
    Element req = JaxbUtil.jaxbToElement(request);
    Element handle = new GetPrefs().handle(req, ServiceTestUtil.getRequestContext(account));
    GetPrefsResponse response = JaxbUtil.elementToJaxb(handle);

    assertEquals(3, Objects.requireNonNull(response).getPref().size());
    response.getPref().forEach(pref -> assertEquals(Provisioning.A_zimbraPrefTimeZoneId, pref.getName()));
  }

  @Test
  void shouldReturnAllSetPrefsWhenPassedNull() throws Exception {
    Account account = createGetRandomAccount();

    account.setCarbonioPrefWebUiDarkMode(true);
    account.setPrefShowChatsFolderInMail(true);

    GetPrefsRequest request = new GetPrefsRequest();
    request.setPref(null);
    Element req = JaxbUtil.jaxbToElement(request);
    Element handle = new GetPrefs().handle(req, ServiceTestUtil.getRequestContext(account));
    GetPrefsResponse response = JaxbUtil.elementToJaxb(handle);

    assertEquals(2, Objects.requireNonNull(response).getPref().size());
  }
}