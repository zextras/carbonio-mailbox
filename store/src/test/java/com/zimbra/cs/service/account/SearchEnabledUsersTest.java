package com.zimbra.cs.service.account;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Group;
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
import java.util.UUID;

import static com.zextras.mailbox.util.MailboxTestUtil.DEFAULT_DOMAIN;
import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    userAccount = buildAccount("user", "User").create();
  }

  @AfterAll
  static void tearDown() throws ServiceException {
    provisioning.deleteAccount(userAccount.getId());
  }

  @Test
  void searchUserByUid() throws Exception {
    var account = buildAccount(ACCOUNT_UID, ACCOUNT_NAME).create();

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
    var account = buildAccount(ACCOUNT_UID.toLowerCase(), ACCOUNT_NAME).create();

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
    var account = buildAccount(ACCOUNT_UID, ACCOUNT_NAME).create();
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
    var account = buildAccount(ACCOUNT_UID, ACCOUNT_NAME).create();

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
    var account = buildAccount(ACCOUNT_UID, ACCOUNT_NAME).create();

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
    var account1 = buildAccount("first.account", "Test1").create();
    var account2 = buildAccount("second.account", "Test2").create();

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
    var account1 = withFeature(
        SearchEnabledUsersRequest.Features.CHATS, true,
        buildAccount("first.account", "Test1")
    ).create();
    var account2 = buildAccount("second.account", "Test2").create();

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
    var cos = createCos(SearchEnabledUsersRequest.Features.CHATS, true);

    var account1 = withCos(cos, buildAccount("first.account", "Test1")).create();
    var account2 = buildAccount("second.account", "Test2").create();

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
  void multipleCos() throws Exception {
    var cos1 = createCos(SearchEnabledUsersRequest.Features.CHATS, true);
    var cos2 = createCos(SearchEnabledUsersRequest.Features.CHATS, true);

    var account1 = withCos(cos1, buildAccount("first.account", "Test1")).create();
    var account2 = withCos(cos2, buildAccount("second.account", "Test2")).create();

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts("account", SearchEnabledUsersRequest.Features.CHATS))
          .execute();

      assertEquals(2, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
      cleanUp(cos1);
      cleanUp(cos2);
    }
  }

  @Test
  void featureEnabledInCosDisabledInAccount() throws Exception {
    var cos = createCos(SearchEnabledUsersRequest.Features.CHATS, true);
    var account = withFeature(SearchEnabledUsersRequest.Features.CHATS, false,
        withCos(cos, buildAccount("first.account", "Test1"))
    ).create();

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
    var cos = createCos(SearchEnabledUsersRequest.Features.CHATS, true);
    var account = withFeature(SearchEnabledUsersRequest.Features.CHATS, true,
        withCos(cos, buildAccount("first.account", "Test1"))
    ).create();

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

  @Test
  void hiddenInGalNotIncluded() throws Exception {
    var account = buildAccount("first.account", "Test1")
        .withAttribute("zimbraHideInGal", "TRUE").create();

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts("account"))
          .execute();

      assertEquals(0, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account);
    }
  }

  @Test
  void distributionListsAndGroupsNotIncluded() throws Exception {
    var dl = provisioning.createDistributionList("accounts-dl@" + DEFAULT_DOMAIN, new HashMap<>());
    var group = provisioning.createGroup("accounts-group@" + DEFAULT_DOMAIN, new HashMap<>(), false);

    try {
      HttpResponse httpResponse = getSoapClient().newRequest()
          .setCaller(userAccount)
          .setSoapBody(SearchEnabledUsersTest.searchAccounts("account"))
          .execute();

      assertEquals(0, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(dl);
      cleanUp(group);
    }
  }

  /*
   * TODO: Implement the following test cases
   */
  @Disabled("Not implemented")
  @Test
  void testIncludedAttributes() {
    assertTrue(false);
  }

  @Disabled("Not implemented")
  @Test
  void testMaxResults() {
    assertTrue(false);
  }

  @Disabled("Not implemented")
  @Test
  void testResultsOnlyInAccountDomain() {
    assertTrue(false);
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

  private static MailboxTestUtil.AccountCreator buildAccount(String uid, String fullName) {
    return accountCreatorFactory.get()
        .withDomain(DEFAULT_DOMAIN)
        .withUsername(uid)
        .withAttribute("displayName", fullName);
  }

  private static MailboxTestUtil.AccountCreator withFeature(SearchEnabledUsersRequest.Features feature,
                                                                  boolean enabled,
                                                                  MailboxTestUtil.AccountCreator account) {
    if (feature != null) {
      account = account
          .withAttribute(feature.getFeature(), enabled ? "TRUE" : "FALSE");
    }
    return account;
  }

  private static MailboxTestUtil.AccountCreator withCos(Cos cos, MailboxTestUtil.AccountCreator account) {
    if (cos != null) {
      account = account
          .withAttribute("zimbraCOSId", cos.getId());
    }
    return account;
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

  private static void cleanUp(DistributionList dl) throws ServiceException {
    provisioning.deleteDistributionList(dl.getId());
  }

  private static void cleanUp(Group group) throws ServiceException {
    provisioning.deleteGroup(group.getId());
  }

  private static void cleanUp(Cos cos) throws ServiceException {
    provisioning.deleteCos(cos.getId());
  }

  private static SearchEnabledUsersResponse getResponse(HttpResponse httpResponse) throws IOException, ServiceException {
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    return parseSoapResponse(httpResponse);
  }


  private static Cos createCos(SearchEnabledUsersRequest.Features feature, boolean enabled) throws ServiceException {
    var cosAttrs = new HashMap<String, Object>();
    cosAttrs.put(feature.getFeature(), enabled ? "TRUE" : "FALSE");
    return provisioning.createCos(UUID.randomUUID().toString(), cosAttrs);
  }
}