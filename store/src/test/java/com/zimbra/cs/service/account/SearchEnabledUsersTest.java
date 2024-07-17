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
  public static final String FIRST_ACCOUNT_NAME = "first.account";
  public static final String SECOND_ACCOUNT_NAME = "second.account";
  private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;
  private static Account userAccount;
  private static Account firstAccount;
  private static Account secondAccount;

  @BeforeAll
  static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    var cosAttrs = new HashMap<String, Object>();
    cosAttrs.put(SearchEnabledUsersRequest.Features.FILES.getFeature(), "TRUE");
    var cos = provisioning.createCos("cos-with-files", cosAttrs);
    accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
    userAccount = accountCreatorFactory.get()
        .withDomain(DEFAULT_DOMAIN)
        .withUsername("user")
        .withAttribute("displayName", "User")
        .create();
    firstAccount = accountCreatorFactory.get()
        .withDomain(DEFAULT_DOMAIN)
        .withUsername(FIRST_ACCOUNT_NAME)
        .withAttribute("displayName", "Test User")
        .withAttribute(SearchEnabledUsersRequest.Features.CHATS.getFeature(), "TRUE")
        .create();
    secondAccount = accountCreatorFactory.get()
        .withDomain(DEFAULT_DOMAIN)
        .withUsername(SECOND_ACCOUNT_NAME)
        .withAttribute("displayName", "Other User")
        .withAttribute("zimbraCOSId", cos.getId())
        .create();
  }

  @Test
  void searchUserExact() throws Exception {
    HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(userAccount)
        .setSoapBody(SearchEnabledUsersTest.searchAccounts(FIRST_ACCOUNT_NAME))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchEnabledUsersResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(1, accounts.size());
    assertEquals(firstAccount.getId(), accounts.get(0).getId());
    assertEquals(firstAccount.getName(), accounts.get(0).getName());
  }

  @Test
  void searchUserPartial() throws Exception {
    HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(userAccount)
        .setSoapBody(SearchEnabledUsersTest.searchAccounts("st"))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchEnabledUsersResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(1, accounts.size());
    assertEquals(firstAccount.getId(), accounts.get(0).getId());
    assertEquals(firstAccount.getName(), accounts.get(0).getName());
  }

  @Test
  void searchUserInDisplayName() throws Exception {
    HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(userAccount)
        .setSoapBody(SearchEnabledUsersTest.searchAccounts("Test"))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchEnabledUsersResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(1, accounts.size());
    assertEquals(firstAccount.getId(), accounts.get(0).getId());
    assertEquals(firstAccount.getName(), accounts.get(0).getName());
  }

  @Test
  void multipleMatches() throws Exception {
    HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(userAccount)
        .setSoapBody(SearchEnabledUsersTest.searchAccounts("account"))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchEnabledUsersResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(2, accounts.size());
  }

  @Test
  void multipleMatchesCaseInsensitive() throws Exception {
    HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(userAccount)
        .setSoapBody(SearchEnabledUsersTest.searchAccounts("ACCOUNT"))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchEnabledUsersResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(2, accounts.size());
  }

  @Test
  void filterByEnabled() throws Exception {
    HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(userAccount)
        .setSoapBody(SearchEnabledUsersTest.searchAccounts("account", SearchEnabledUsersRequest.Features.CHATS))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchEnabledUsersResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(1, accounts.size());
  }

  @Test
  void filterByEnabledInCos() throws Exception {
    HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(userAccount)
        .setSoapBody(SearchEnabledUsersTest.searchAccounts("account", SearchEnabledUsersRequest.Features.FILES))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchEnabledUsersResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(1, accounts.size());
  }

  private static SearchEnabledUsersResponse parseSoapResponse(HttpResponse httpResponse) throws IOException, ServiceException {
    final String responseBody = EntityUtils.toString(httpResponse.getEntity());
    final Element rootElement = parseXML(responseBody).getElement("Body").getElement("SearchEnabledUsersResponse");
    return JaxbUtil.elementToJaxb(rootElement, SearchEnabledUsersResponse.class);
  }

  private static SearchEnabledUsersRequest searchAccounts(String name) {
    return searchAccounts(name, null);
  }

  private static SearchEnabledUsersRequest searchAccounts(String name, SearchEnabledUsersRequest.Features feature) {
    SearchEnabledUsersRequest request = new SearchEnabledUsersRequest();
    request.setName(name);
    request.setFeature(feature);

    return request;
  }
}