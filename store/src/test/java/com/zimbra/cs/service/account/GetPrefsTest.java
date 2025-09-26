package com.zimbra.cs.service.account;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.GetPrefsRequest;
import com.zimbra.soap.account.message.GetPrefsResponse;
import com.zimbra.soap.account.type.Pref;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class GetPrefsTest  extends MailboxTestSuite {

  private Account createGetRandomAccount() throws ServiceException {
    return createAccount().create();
  }

  @Test
  void shouldReturnEmptyResponseWhenNoPrefsAreSet() throws Exception {
    Account account = createGetRandomAccount();

    GetPrefsRequest request = new GetPrefsRequest();
    request.setPref(List.of());
    Element req = JaxbUtil.jaxbToElement(request);
    Element handle = new GetPrefs().handle(req, ServiceTestUtil.getRequestContext(account));
    GetPrefsResponse response = JaxbUtil.elementToJaxb(handle);

    // TODO: checkme, these tests were based on mocks
    assertFalse(Objects.requireNonNull(response).getPref().isEmpty());
  }

  @Test
  void shouldNotReturnPrefWhenNotSetButAsked() throws Exception {
    Account account = createGetRandomAccount();

    GetPrefsRequest request = new GetPrefsRequest();
    request.setPref(List.of(new Pref(Provisioning.A_carbonioPrefWebUiDarkMode)));
    Element req = JaxbUtil.jaxbToElement(request);
    Element handle = new GetPrefs().handle(req, ServiceTestUtil.getRequestContext(account));
    GetPrefsResponse response = JaxbUtil.elementToJaxb(handle);

    assertFalse(Objects.requireNonNull(response).getPref().isEmpty());
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

    assertFalse(response.getPref().isEmpty());
  }
}