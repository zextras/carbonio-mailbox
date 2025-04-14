package com.zimbra.cs.service.account;

import static com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.soap.SoapTestSuite;
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
import com.zimbra.soap.account.message.SearchUsersByFeatureRequest;
import com.zimbra.soap.account.message.SearchUsersByFeatureResponse;
import com.zimbra.soap.account.message.UserInfo;
import com.zimbra.soap.type.ZmBoolean;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
public class SearchUsersByFeatureTest extends SoapTestSuite {
  public static final String ACCOUNT_UID = "first.account";
  public static final String ACCOUNT_NAME = "name";
  private static AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;
  private static Account userAccount;

  @BeforeAll
  static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning,
        soapExtension.getDefaultDomain());
    userAccount = buildAccount("user", "User").create();
  }

  @AfterAll
  static void tearDown() throws ServiceException {
    provisioning.deleteAccount(userAccount.getId());
  }

  @BeforeEach
  void setUpTest() throws ServiceException {
    provisioning.getConfig().setCarbonioSearchAllDomainsByFeature(false);
  }

  @Test
  void searchEmptyQuery() throws Exception {
    var account = buildAccount(ACCOUNT_UID, ACCOUNT_NAME).create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts(""))
          .execute();

      // account + userAccount are in soapExtension.getDefaultDomain()
      assertEquals(2, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account);
    }
  }

  @Test
  void searchUserByUid() throws Exception {
    var account = buildAccount(ACCOUNT_UID, ACCOUNT_NAME).create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts(ACCOUNT_UID))
          .execute();

      assertSuccessWithSingleAccount(httpResponse, account);
    } finally {
      cleanUp(account);
    }
  }

  @Test
  void searchUserByDomain() throws Exception {
    var account = buildAccount(ACCOUNT_UID, ACCOUNT_NAME).create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts(soapExtension.getDefaultDomain()))
          .execute();

      // account + userAccount are in soapExtension.getDefaultDomain()
      assertEquals(2, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account);
    }
  }

  @Test
  void searchUserSymbolsInQuery() throws Exception {
    var account = buildAccount(ACCOUNT_UID, "@#./$\\").create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("@#./$\\"))
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts(ACCOUNT_UID.toUpperCase()))
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts(ACCOUNT_UID.substring(0, 2)))
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts(ACCOUNT_NAME))
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts(MessageFormat.format("{0}@{1}", ACCOUNT_UID, soapExtension.getDefaultDomain())))
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account"))
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account", SearchUsersByFeatureRequest.Features.CHATS))
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account", SearchUsersByFeatureRequest.Features.CHATS))
          .execute();

      assertEquals(1, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
      cleanUp(cos);
    }
  }

  @Test
  void featureEnabledInDefaultCosNotInAccount() throws Exception {
    var cos = createCosWithChatsEnabled();
    var anotherDomain = provisioning.createDomain("anotherdomain.com", new HashMap<>());

    var account1 = buildAccount("first.account", "Test1").create();
    var account2 = buildAccount("second.account", "Test2", "anotherdomain.com").create();

    var domain = provisioning.getDomain(account1);
    domain.setDomainDefaultCOSId(cos.getId());

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account", SearchUsersByFeatureRequest.Features.CHATS))
          .execute();

      assertEquals(1, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
      cleanUp(cos);
      cleanUp(anotherDomain);
    }
  }

  @Test
  void featureEnabledInDefaultCosForAnotherDomain() throws Exception {
    provisioning.getConfig().setCarbonioSearchAllDomainsByFeature(true);
    var cos = createCosWithChatsEnabled();
    var anotherDomain = provisioning.createDomain("anotherdomain.com", new HashMap<>());

    var account1 = buildAccount("first.account", "Test1").create();
    var account2 = buildAccount("second.account", "Test2", "anotherdomain.com").create();

    anotherDomain.setDomainDefaultCOSId(cos.getId());

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account", SearchUsersByFeatureRequest.Features.CHATS))
          .execute();

      assertEquals(1, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
      cleanUp(cos);
      cleanUp(anotherDomain);
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account", SearchUsersByFeatureRequest.Features.CHATS))
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account", SearchUsersByFeatureRequest.Features.CHATS))
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account", SearchUsersByFeatureRequest.Features.CHATS))
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account"))
          .execute();

      assertEquals(0, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account);
    }
  }

  @Test
  void distributionListsAndGroupsNotIncluded() throws Exception {
    var dl = provisioning.createDistributionList("accounts-dl@" + soapExtension.getDefaultDomain(), new HashMap<>());
    var group = provisioning.createGroup("accounts-group@" + soapExtension.getDefaultDomain(), new HashMap<>(), false);

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account"))
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account", SearchUsersByFeatureRequest.Features.CHATS, 1, 0))
          .execute();

      var response = getResponse(httpResponse);
      assertEquals(1, response.getAccounts().size());
      assertEquals(2, response.getTotal());
      assertTrue(ZmBoolean.toBool(response.getMore()));
    } finally {
      cleanUp(account1);
      cleanUp(account2);
    }
  }

  @Test
  void testOffset() throws Exception {
    var account1 = withChatsFeature(true, buildAccount("first.account", "Test1")).create();
    var account2 = withChatsFeature(true, buildAccount("second.account", "Test2")).create();
    var account3 = withChatsFeature(true, buildAccount("third.account", "Test3")).create();
    var account4 = withChatsFeature(true, buildAccount("fourth.account", "Test4")).create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account", SearchUsersByFeatureRequest.Features.CHATS, 10, 3))
          .execute();

      var response = getResponse(httpResponse);
      assertEquals(1, response.getAccounts().size());
      assertEquals(4, response.getTotal());
      assertFalse(ZmBoolean.toBool(response.getMore()));
    } finally {
      cleanUp(account1);
      cleanUp(account2);
      cleanUp(account3);
      cleanUp(account4);
    }
  }

  @Test
  void testResultsOnlyInAccountDomain() throws Exception {
    var domain = provisioning.createDomain("anotherdomain.com", new HashMap<>());
    var account1 = buildAccount("first.account", "Test1").create();
    var account2 = buildAccount("second.account", "Test2", "anotherdomain.com").create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account"))
          .execute();

      assertEquals(1, getResponse(httpResponse).getAccounts().size());
    } finally {
      cleanUp(account1);
      cleanUp(account2);
      cleanUp(domain);
    }
  }

  @Test
  void testResultsInAllDomains() throws Exception {
    var domain = provisioning.createDomain("anotherdomain.com", new HashMap<>());
    provisioning.getConfig().setCarbonioSearchAllDomainsByFeature(true);
    var account1 = buildAccount("first.account", "Test1").create();
    var account2 = buildAccount("second.account", "Test2", "anotherdomain.com").create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account"))
          .execute();

      assertEquals(2, getResponse(httpResponse).getAccounts().size());
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
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts(ACCOUNT_UID))
          .execute();

      var returnedAccount = assertSuccessWithSingleAccount(httpResponse, account);
      var attributes = returnedAccount.getAttrList();
      assertEquals(3, attributes.size());
    } finally {
      cleanUp(account);
    }
  }

  @Test
  void testSortByDisplayName() throws Exception {
    var account1 = buildAccount("zzz.account", "AAA").create();
    var account2 = buildAccount("aaa.account", "ZZZ").create();

    try {
      HttpResponse httpResponse = buildRequest()
          .setSoapBody(SearchUsersByFeatureTest.searchAccounts("account"))
          .execute();

      var returnedAccounts = getResponse(httpResponse).getAccounts();
      assertEquals(2, returnedAccounts.size());
      assertEquals("zzz.account@" + soapExtension.getDefaultDomain(), returnedAccounts.get(0).getName());
      assertEquals("aaa.account@" + soapExtension.getDefaultDomain(), returnedAccounts.get(1).getName());
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

  private static SearchUsersByFeatureRequest searchAccounts(String name) {
    return searchAccounts(name, null);
  }

  private static SearchUsersByFeatureRequest searchAccounts(String name, SearchUsersByFeatureRequest.Features feature) {
    return searchAccounts(name, feature, 0, 0);
  }

  private static SearchUsersByFeatureRequest searchAccounts(String name, SearchUsersByFeatureRequest.Features feature, int maxResults, int offset) {
    SearchUsersByFeatureRequest request = new SearchUsersByFeatureRequest();
    request.setName(name);
    request.setFeature(feature);
    request.setOffset(offset);
    if (maxResults > 0) {
      request.setLimit(maxResults);
    }

    return request;
  }

  private static AccountCreator buildAccount(String uid, String fullName) {
    return buildAccount(uid, fullName, soapExtension.getDefaultDomain());
  }

  private static AccountCreator buildAccount(String uid, String fullName, String domain) {
    return accountCreatorFactory.get()
        .withDomain(domain)
        .withUsername(uid)
        .withAttribute("displayName", fullName);
  }

  private static AccountCreator withChatsFeature(boolean enabled,
                                                                 AccountCreator account) {
    return account
        .withAttribute(SearchUsersByFeatureRequest.Features.CHATS.getFeature(), enabled ? "TRUE" : "FALSE");
  }

  private static AccountCreator withCos(Cos cos, AccountCreator account) {
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
    cosAttrs.put(SearchUsersByFeatureRequest.Features.CHATS.getFeature(), "TRUE");
    return provisioning.createCos(UUID.randomUUID().toString(), cosAttrs);
  }

  private SoapClient.Request buildRequest() {
    return getSoapClient().newRequest()
        .setCaller(userAccount);
  }
}