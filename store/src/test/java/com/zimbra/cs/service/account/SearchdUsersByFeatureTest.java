package com.zimbra.cs.service.account;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.SoapClient;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.UserInfo;
import com.zimbra.soap.account.message.SearchdUsersByFeatureRequest;
import com.zimbra.soap.account.message.SearchUsersByFeatureResponse;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

@Tag("api")
public class SearchdUsersByFeatureTest extends SoapTestSuite {
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
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts(ACCOUNT_UID))
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
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts(ACCOUNT_UID.toUpperCase()))
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
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts(ACCOUNT_UID.substring(0, 2)))
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
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts(ACCOUNT_NAME))
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
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts(MessageFormat.format("{0}@{1}", ACCOUNT_UID, DEFAULT_DOMAIN)))
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
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts("account"))
          .execute();

      assertEquals(2, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
    }
  }

  @Test
  void featureEnabledInAccountNotInCos() throws Exception {
    var account1 = withChatsFeature(
        true,
        buildAccount("first.account", "Test1")
    ).create();
    var account2 = buildAccount("second.account", "Test2").create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts("account", SearchdUsersByFeatureRequest.Features.CHATS))
          .execute();

      assertEquals(1, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
    }
  }

  @Test
  void featureEnabledInCosNotInAccount() throws Exception {
    var cos = createCosWithChatsEnabled();

    var account1 = withCos(cos, buildAccount("first.account", "Test1")).create();
    var account2 = buildAccount("second.account", "Test2").create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts("account", SearchdUsersByFeatureRequest.Features.CHATS))
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
    var cos1 = createCosWithChatsEnabled();
    var cos2 = createCosWithChatsEnabled();

    var account1 = withCos(cos1, buildAccount("first.account", "Test1")).create();
    var account2 = withCos(cos2, buildAccount("second.account", "Test2")).create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts("account", SearchdUsersByFeatureRequest.Features.CHATS))
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
    var cos = createCosWithChatsEnabled();
    var account = withChatsFeature(false,
        withCos(cos, buildAccount("first.account", "Test1"))
    ).create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts("account", SearchdUsersByFeatureRequest.Features.CHATS))
          .execute();

      assertEquals(0, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account);
      cleanUp(cos);
    }
  }

  @Test
  void featureEnabledBothCosAndAccount() throws Exception {
    var cos = createCosWithChatsEnabled();
    var account = withChatsFeature(true,
        withCos(cos, buildAccount("first.account", "Test1"))
    ).create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts("account", SearchdUsersByFeatureRequest.Features.CHATS))
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
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts("account"))
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
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts("account"))
          .execute();

      assertEquals(0, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(dl);
      cleanUp(group);
    }
  }

  @Test
  void testMaxResults() throws Exception {
    var account1 = withChatsFeature(true, buildAccount("first.account", "Test1")).create();
    var account2 = withChatsFeature(true, buildAccount("second.account", "Test2")).create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts("account", SearchdUsersByFeatureRequest.Features.CHATS, 1))
          .execute();

      assertEquals(1, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
    }
  }

  @Test
  void testResultsOnlyInAccountDomain() throws Exception {
    var domain = provisioning.createDomain("anotherdomain.com", new HashMap<>());
    var account1 = buildAccount("first.account", "Test1").create();
    var account2 = buildAccount("second.account", "Test2", "anotherdomain.com").create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts("account"))
          .execute();

      assertEquals(1, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
      cleanUp(domain);
    }
  }

  @Test
  void testIncludedAttributes() throws Exception {
    var account = buildAccount(ACCOUNT_UID, ACCOUNT_NAME).create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts(ACCOUNT_UID))
          .execute();

      var returnedAccount = assertSuccessWithSingleAccount(httpResponse, account);
      var attributes = returnedAccount.getAttrList();
      assertEquals(3, attributes.size());
    } finally {
      cleanUp(account);
    }
  }

  @Test
  void testSortByUid() throws Exception {
    var account1 = buildAccount("zzz.account", ACCOUNT_NAME).create();
    var account2 = buildAccount("aaa.account", ACCOUNT_NAME).create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchdUsersByFeatureTest.searchAccounts("account"))
          .execute();

      var returnedAccounts = getResponse(httpResponse).getAccounts();
      assertEquals(2, returnedAccounts.size());
      assertEquals("aaa.account@" + DEFAULT_DOMAIN, returnedAccounts.get(0).getName());
      assertEquals("zzz.account@" + DEFAULT_DOMAIN, returnedAccounts.get(1).getName());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
    }
  }

  private static SearchUsersByFeatureResponse parseSoapResponse(HttpResponse httpResponse) throws IOException, ServiceException {
    final String responseBody = EntityUtils.toString(httpResponse.getEntity());
    final Element rootElement = parseXML(responseBody).getElement("Body").getElement("SearchUsersByFeatureResponse");
    return JaxbUtil.elementToJaxb(rootElement, SearchUsersByFeatureResponse.class);
  }

  private static SearchdUsersByFeatureRequest searchAccounts(String name) {
    return searchAccounts(name, null);
  }

  private static SearchdUsersByFeatureRequest searchAccounts(String name, SearchdUsersByFeatureRequest.Features feature) {
    return searchAccounts(name, feature, 0);
  }

  private static SearchdUsersByFeatureRequest searchAccounts(String name, SearchdUsersByFeatureRequest.Features feature, int maxResults) {
    SearchdUsersByFeatureRequest request = new SearchdUsersByFeatureRequest();
    request.setName(name);
    request.setFeature(feature);
    if (maxResults > 0) {
      request.setLimit(maxResults);
    }

    return request;
  }

  private static MailboxTestUtil.AccountCreator buildAccount(String uid, String fullName) {
    return buildAccount(uid, fullName, DEFAULT_DOMAIN);
  }

  private static MailboxTestUtil.AccountCreator buildAccount(String uid, String fullName, String domain) {
    return accountCreatorFactory.get()
        .withDomain(domain)
        .withUsername(uid)
        .withAttribute("displayName", fullName);
  }

  private static MailboxTestUtil.AccountCreator withChatsFeature(boolean enabled,
                                                                 MailboxTestUtil.AccountCreator account) {
    return account
        .withAttribute(SearchdUsersByFeatureRequest.Features.CHATS.getFeature(), enabled ? "TRUE" : "FALSE");
  }

  private static MailboxTestUtil.AccountCreator withCos(Cos cos, MailboxTestUtil.AccountCreator account) {
    if (cos != null) {
      account = account
          .withAttribute("zimbraCOSId", cos.getId());
    }
    return account;
  }

  private static UserInfo assertSuccessWithSingleAccount(HttpResponse httpResponse, Account account) throws IOException, ServiceException {
    SearchUsersByFeatureResponse response = getResponse(httpResponse);
    List<UserInfo> accounts = response.getAccounts();
    assertEquals(1, accounts.size());
    var firstAccount = accounts.get(0);
    assertEquals(account.getId(), firstAccount.getId());
    assertEquals(account.getName(), firstAccount.getName());
    return firstAccount;
  }

  private static void cleanUp(Account account) throws ServiceException {
    provisioning.deleteAccount(account.getId());
  }

  private static void cleanUp(Domain domain) throws ServiceException {
    provisioning.deleteDomain(domain.getId());
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

  private static SearchUsersByFeatureResponse getResponse(HttpResponse httpResponse) throws IOException, ServiceException {
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    return parseSoapResponse(httpResponse);
  }


  private static Cos createCosWithChatsEnabled() throws ServiceException {
    var cosAttrs = new HashMap<String, Object>();
    cosAttrs.put(SearchdUsersByFeatureRequest.Features.CHATS.getFeature(), "TRUE");
    return provisioning.createCos(UUID.randomUUID().toString(), cosAttrs);
  }

  private SoapClient.Request buildRequest() {
    return getSoapClient().newRequest()
        .setCaller(userAccount);
  }
}