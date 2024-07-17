package com.zimbra.cs.service.account;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.SearchEnabledUsersRequest;
import com.zimbra.soap.account.message.SearchEnabledUsersResponse;
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
public class SearchEnabledUsersTest extends SoapTestSuite {
  public static final String ACCOUNT_NAME = "myaccount";
  private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;
  private static Account userAccount;

  @BeforeAll
  static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    provisioning.createDomain("different.com", new HashMap<>());
    accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
    userAccount = accountCreatorFactory.get()
        .withDomain(DEFAULT_DOMAIN)
        .withUsername(ACCOUNT_NAME)
        .withAttribute("displayName", "Test Account")
        .create();
  }

  @Test
  void searchUserExact() throws Exception {
    HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(userAccount)
        .setSoapBody(SearchEnabledUsersTest.searchAccountsByName(ACCOUNT_NAME))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchEnabledUsersResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(1, accounts.size());
    assertEquals(userAccount.getId(), accounts.get(0).getId());
    assertEquals(userAccount.getName(), accounts.get(0).getName());
  }

  @Test
  void searchUserPartial() throws Exception {
    HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(userAccount)
        .setSoapBody(SearchEnabledUsersTest.searchAccountsByName("ac"))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchEnabledUsersResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(1, accounts.size());
    assertEquals(userAccount.getId(), accounts.get(0).getId());
    assertEquals(userAccount.getName(), accounts.get(0).getName());
  }

  @Test
  void searchUserInDisplayName() throws Exception {
    HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(userAccount)
        .setSoapBody(SearchEnabledUsersTest.searchAccountsByName("Test"))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchEnabledUsersResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(1, accounts.size());
    assertEquals(userAccount.getId(), accounts.get(0).getId());
    assertEquals(userAccount.getName(), accounts.get(0).getName());
  }

  private static SearchEnabledUsersResponse parseSoapResponse(HttpResponse httpResponse) throws IOException, ServiceException {
    final String responseBody = EntityUtils.toString(httpResponse.getEntity());
    final Element rootElement = parseXML(responseBody).getElement("Body").getElement("SearchEnabledUsersResponse");
    return JaxbUtil.elementToJaxb(rootElement, SearchEnabledUsersResponse.class);
  }

  private static SearchEnabledUsersRequest searchAccountsByName(String name) {
    return searchAccountsByName(name, null, null);
  }

  private static SearchEnabledUsersRequest searchAccountsByName(String name, Integer limit, Integer offset) {
    SearchEnabledUsersRequest request = new SearchEnabledUsersRequest();
    request.setName(name);

    if (limit != null) {
      request.setLimit(limit);
    }

    if (offset != null) {
      request.setOffset(offset);
    }
    return request;
  }
}