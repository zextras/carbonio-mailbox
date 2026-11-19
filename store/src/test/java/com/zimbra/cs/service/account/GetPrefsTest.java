package com.zimbra.cs.service.account;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetPrefsTest  extends MailboxTestSuite {

  private Account createGetRandomAccount() throws ServiceException {
    return createAccount().create();
  }

  private static List<Element> getPrefs(Account account, String... names) throws ServiceException {
    Element request = new Element.XMLElement(AccountConstants.E_PREFS);
    for (String name : names) {
      request.addNonUniqueElement(AccountConstants.E_PREF).addAttribute(AccountConstants.A_NAME, name);
    }
    Element response = new Element.XMLElement(AccountConstants.E_PREFS);
    GetPrefs.handle(request, response, account);
    return response.listElements(AccountConstants.E_PREF);
  }

  @Test
  void shouldReturnEmptyResponseWhenNoPrefsAreSet() throws Exception {
    Account account = createGetRandomAccount();

    assertFalse(getPrefs(account).isEmpty());
  }

  @Test
  void shouldNotReturnPrefWhenNotSetButAsked() throws Exception {
    Account account = createGetRandomAccount();

    assertFalse(getPrefs(account, Provisioning.A_carbonioPrefWebUiDarkMode).isEmpty());
  }

  @Test
  void shouldReturnPrefWhenSetAndAsked() throws Exception {
    Account account = createGetRandomAccount();
    account.setCarbonioPrefWebUiDarkMode(true);

    List<Element> prefs = getPrefs(account, Provisioning.A_carbonioPrefWebUiDarkMode);

    assertEquals(1, prefs.size());
    assertEquals(Provisioning.A_carbonioPrefWebUiDarkMode, prefs.get(0).getAttribute(AccountConstants.A_NAME));
  }

  @Test
  void shouldReturnMultiCardinalPrefWhenSetAndAsked() throws Exception {
    Account account = createGetRandomAccount();
    String[] timezones = {"America/Los_Angeles", "Asia/Kolkata", "Europe/Paris"};
    account.setPrefTimeZoneId(timezones);

    List<Element> prefs = getPrefs(account, Provisioning.A_zimbraPrefTimeZoneId);

    assertEquals(3, prefs.size());
    for (Element pref : prefs) {
      assertEquals(Provisioning.A_zimbraPrefTimeZoneId, pref.getAttribute(AccountConstants.A_NAME));
    }
  }

  @Test
  void shouldReturnAllSetPrefsWhenPassedNull() throws Exception {
    Account account = createGetRandomAccount();
    account.setCarbonioPrefWebUiDarkMode(true);
    account.setPrefSaveToSent(true);

    assertFalse(getPrefs(account).isEmpty());
  }
}
