package com.zimbra.cs.service.account;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.SearchEnabledUsersRequest;
import com.zimbra.soap.account.message.SearchEnabledUsersResponse;
import com.zimbra.soap.admin.type.AccountInfo;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
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
  public static final String ACCOUNT_UID = "first.account";
  public static final String ACCOUNT_NAME = "name";
  private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;
  private static Account userAccount;

  @BeforeAll
  static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
    userAccount = createAccount("user", "User");
    /*
    var cosAttrs = new HashMap<String, Object>();
    cosAttrs.put(SearchEnabledUsersRequest.Features.FILES.getFeature(), "TRUE");
    var cos = provisioning.createCos("cos-with-files", cosAttrs);
    firstAccount = createAccount(FIRST_ACCOUNT_NAME, "Test User", SearchEnabledUsersRequest.Features.CHATS);
    secondAccount = accountCreatorFactory.get()
        .withDomain(DEFAULT_DOMAIN)
        .withUsername(SECOND_ACCOUNT_NAME)
        .withAttribute("displayName", "Other User")
        .withAttribute("zimbraCOSId", cos.getId())
        .create();*/
  }

  @AfterAll
  static void tearDown() throws ServiceException {
    provisioning.deleteAccount(userAccount.getId());
  }

  @Test
  void searchUserByUid() throws Exception {
    var account = createAccount(ACCOUNT_UID, ACCOUNT_NAME);

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts(ACCOUNT_UID))
          .execute();

      assertSuccessWithSingleAccount(httpResponse, account);
    } finally {
      cleanUp(account);
    }
  }

  @Test
  void searchUserByUidCaseInsensitive() throws Exception {
    var account = createAccount(ACCOUNT_UID.toLowerCase(), ACCOUNT_NAME);

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts(ACCOUNT_UID.toUpperCase()))
          .execute();

      assertSuccessWithSingleAccount(httpResponse, account);
    } finally {
      cleanUp(account);
    }
  }

  @Test
  void searchUserPartialUid() throws Exception {
    var account = createAccount(ACCOUNT_UID, ACCOUNT_NAME);
    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts(ACCOUNT_UID.substring(3, 5)))
          .execute();

      assertSuccessWithSingleAccount(httpResponse, account);
    } finally {
      cleanUp(account);
    }
  }

  @Test
  void searchUserInDisplayName() throws Exception {
    var account = createAccount(ACCOUNT_UID, ACCOUNT_NAME);

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts(ACCOUNT_NAME))
          .execute();

      assertSuccessWithSingleAccount(httpResponse, account);
    } finally {
      cleanUp(account);
    }
  }

  @Test
  void multipleMatches() throws Exception {
    var account1 = createAccount("first.account", "Test1");
    var account2 = createAccount("second.account", "Test2");

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts("account"))
          .execute();

      assertEquals(2, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
    }
  }

  @Test
  void filterByEnabled() throws Exception {
    var account1 = createAccount("first.account", "Test1", SearchEnabledUsersRequest.Features.CHATS);
    var account2 = createAccount("second.account", "Test2");

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts("account", SearchEnabledUsersRequest.Features.CHATS))
          .execute();

      assertEquals(1, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
    }
  }

  @Test
  void filterByEnabledInCos() throws Exception {
    var cosAttrs = new HashMap<String, Object>();
    cosAttrs.put(SearchEnabledUsersRequest.Features.CHATS.getFeature(), "TRUE");
    var cos = provisioning.createCos("cos-with-chats", cosAttrs);

    var account1 = createAccount("first.account", "Test1", cos);
    var account2 = createAccount("second.account", "Test2");

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts("account", SearchEnabledUsersRequest.Features.CHATS))
          .execute();

      assertEquals(1, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
    }
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

  private static Account createAccount(String accountName, String fullName) throws ServiceException {
    return createAccount(accountName, fullName, null, null);
  }

  private static Account createAccount(String accountName, String fullName, Cos cos) throws ServiceException {
    return createAccount(accountName, fullName, null, cos);
  }

  private static Account createAccount(String accountName, String fullName, SearchEnabledUsersRequest.Features feature) throws ServiceException {
    return createAccount(accountName, fullName, feature, null);
  }

  private static Account createAccount(String accountName, String fullName, SearchEnabledUsersRequest.Features feature, Cos cos) throws ServiceException {
    var account = accountCreatorFactory.get()
        .withDomain(DEFAULT_DOMAIN)
        .withUsername(accountName)
        .withAttribute("displayName", fullName);
    if (feature != null) {
      return account
          .withAttribute(feature.getFeature(), "TRUE")
          .create();
    }
    if (cos != null) {
      return account
          .withAttribute("zimbraCOSId", cos.getId())
          .create();
    }
    return account.create();
  }

  private static void assertSuccessWithSingleAccount(HttpResponse httpResponse, Account account) throws IOException, ServiceException {
    SearchEnabledUsersResponse response = getResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(1, accounts.size());
    assertEquals(account.getId(), accounts.get(0).getId());
    assertEquals(account.getName(), accounts.get(0).getName());
  }

  private static void cleanUp(Account account) throws ServiceException {
    provisioning.deleteAccount(account.getId());
  }

  private static SearchEnabledUsersResponse getResponse(HttpResponse httpResponse) throws IOException, ServiceException {
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    return parseSoapResponse(httpResponse);
  }
}