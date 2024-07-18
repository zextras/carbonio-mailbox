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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.MessageFormat;
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
          .setSoapBody(SearchEnabledUsersTest.searchAccounts(ACCOUNT_UID.substring(0, 2)))
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
  void searchUserInEmail() throws Exception {
    var account = createAccount(ACCOUNT_UID, ACCOUNT_NAME);

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts(getEmailAddress(ACCOUNT_UID, DEFAULT_DOMAIN)))
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
  void featureEnabledInAccountNotInCos() throws Exception {
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
  void featureEnabledInCosNotInAccount() throws Exception {
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
      cleanUp(cos);
    }
  }

  @Test
  void featureEnabledInCosDisabledInAccount() throws Exception {
    var cosAttrs = new HashMap<String, Object>();
    cosAttrs.put(SearchEnabledUsersRequest.Features.CHATS.getFeature(), "TRUE");
    var cos = provisioning.createCos("cos-with-chats", cosAttrs);
    var account = createAccount("first.account", "Test1", SearchEnabledUsersRequest.Features.CHATS, false, cos);

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts("account", SearchEnabledUsersRequest.Features.CHATS))
          .execute();

      assertEquals(0, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account);
      cleanUp(cos);
    }
  }

  @Test
  void featureEnabledBothCosAndAccount() throws Exception {
    var cosAttrs = new HashMap<String, Object>();
    cosAttrs.put(SearchEnabledUsersRequest.Features.CHATS.getFeature(), "TRUE");
    var cos = provisioning.createCos("cos-with-chats", cosAttrs);
    var account = createAccount("first.account", "Test1", SearchEnabledUsersRequest.Features.CHATS, true, cos);

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts("account", SearchEnabledUsersRequest.Features.CHATS))
          .execute();

      assertSuccessWithSingleAccount(httpResponse, account);
    } finally {
      cleanUp(account);
      cleanUp(cos);
    }
  }

  /*
   * TODO: Implement the following test cases
   */
  @Disabled
  @Test
  public void distributionListsAndGroupsNotIncluded() {

  }

  @Disabled
  @Test
  public void hiddenInGalNotIncluded() {

  }

  @Disabled
  @Test
  public void testIncludedAttributes() {

  }

  @Disabled
  @Test
  public void testMaxResults() {

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
    return createAccount(accountName, fullName, null, false, null);
  }

  private static Account createAccount(String accountName, String fullName, Cos cos) throws ServiceException {
    return createAccount(accountName, fullName, null, false, cos);
  }

  private static Account createAccount(String accountName, String fullName, SearchEnabledUsersRequest.Features feature, boolean enabled) throws ServiceException {
    return createAccount(accountName, fullName, feature, enabled, null);
  }

  private static Account createAccount(String accountName, String fullName, SearchEnabledUsersRequest.Features feature) throws ServiceException {
    return createAccount(accountName, fullName, feature, true, null);
  }

  private static Account createAccount(String uid, String fullName, SearchEnabledUsersRequest.Features feature, boolean enabled, Cos cos) throws ServiceException {
    var account = accountCreatorFactory.get()
        .withDomain(DEFAULT_DOMAIN)
        .withUsername(uid)
        .withAttribute("displayName", fullName);
    if (feature != null) {
      return account
          .withAttribute(feature.getFeature(), enabled ? "TRUE" : "FALSE")
          .create();
    }
    if (cos != null) {
      return account
          .withAttribute("zimbraCOSId", cos.getId())
          .create();
    }
    return account.create();
  }

  private static String getEmailAddress(String uid, String domain) {
    return MessageFormat.format("{0}@{1}", uid, domain);
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

  private static void cleanUp(Cos cos) throws ServiceException {
    provisioning.deleteCos(cos.getId());
  }

  private static SearchEnabledUsersResponse getResponse(HttpResponse httpResponse) throws IOException, ServiceException {
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    return parseSoapResponse(httpResponse);
  }
}