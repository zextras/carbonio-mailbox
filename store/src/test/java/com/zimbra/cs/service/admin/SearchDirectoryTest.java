package com.zimbra.cs.service.admin;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.admin.message.SearchDirectoryRequest;
import com.zimbra.soap.admin.message.SearchDirectoryResponse;
import com.zimbra.soap.admin.type.AccountInfo;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static com.zextras.mailbox.util.MailboxTestUtil.DEFAULT_DOMAIN;
import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("api")
class SearchDirectoryTest extends SoapTestSuite {
  private static AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;

  @BeforeAll
  static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    provisioning.createDomain("different.com", new HashMap<>());
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }

  @Test
  void searchAccountsInADomain() throws Exception {
    Account adminAccount = newAccountOn(DEFAULT_DOMAIN).withUsername("admin.account").asGlobalAdmin().create();
    Account normalAccount = newAccountOn(DEFAULT_DOMAIN).withUsername("normal.account").create();
    newAccountOn("different.com").withUsername("different.account").create();

    final HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(adminAccount)
        .setSoapBody(searchAccountsByDomain(DEFAULT_DOMAIN))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchDirectoryResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(1 + 1, accounts.size());
    assertEquals(adminAccount.getId(), accounts.get(0).getId());
    assertEquals("admin.account@" + DEFAULT_DOMAIN, accounts.get(0).getName());
    assertEquals(normalAccount.getId(), accounts.get(1).getId());
    assertEquals("normal.account@" + DEFAULT_DOMAIN, accounts.get(1).getName());
  }

  // TEST CASES:
  // read some attributes
  // sorting
  // pagination (more and searchTotal should have the bug)

  private static SearchDirectoryResponse parseSoapResponse(HttpResponse httpResponse) throws IOException, ServiceException {
    final String responseBody = EntityUtils.toString(httpResponse.getEntity());
    final Element rootElement = parseXML(responseBody).getElement("Body").getElement("SearchDirectoryResponse");
    return JaxbUtil.elementToJaxb(rootElement, SearchDirectoryResponse.class);
  }

  private static SearchDirectoryRequest searchAccountsByDomain(String domain) {
    SearchDirectoryRequest request = new SearchDirectoryRequest();
    request.setDomain(domain);
    request.setTypes("accounts");
    request.setAttrs("");
    return request;
  }

  private static AccountCreator newAccountOn(String domain) {
    return accountCreatorFactory.get().withDomain(domain);
  }

}
