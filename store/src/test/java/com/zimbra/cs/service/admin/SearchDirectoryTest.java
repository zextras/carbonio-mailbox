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
import static org.junit.jupiter.api.Assertions.*;

@Tag("api")
class SearchDirectoryTest extends SoapTestSuite {
  private static AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;
  private static Account adminAccount;
  private static Account secondAccount;
  private static Account firstAccount;

  @BeforeAll
  static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    provisioning.createDomain("different.com", new HashMap<>());
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
    adminAccount = newAccountOn(DEFAULT_DOMAIN).withUsername("admin.account").asGlobalAdmin().create();
    secondAccount = newAccountOn(DEFAULT_DOMAIN).withUsername("second.account").create();
    firstAccount = newAccountOn(DEFAULT_DOMAIN).withUsername("first.account").create();
  }

  @Test
  void searchAccountsInADomain() throws Exception {
    HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(adminAccount)
        .setSoapBody(searchAccountsByDomain(DEFAULT_DOMAIN))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchDirectoryResponse response = parseSoapResponse(httpResponse);
    List<AccountInfo> accounts = response.getAccounts();
    assertEquals(1 + 2, accounts.size());
    assertEquals(adminAccount.getId(), accounts.get(0).getId());
    assertEquals(adminAccount.getName(), accounts.get(0).getName());
    assertEquals(firstAccount.getId(), accounts.get(1).getId());
    assertEquals(firstAccount.getName(), accounts.get(1).getName());
  }

  @Test
  void searchAccountPaginationFirstPage() throws Exception {
    HttpResponse firstPageHttpResponse = getSoapClient().newRequest()
        .setCaller(adminAccount)
        .setSoapBody(searchAccountsByDomain(DEFAULT_DOMAIN, 1, 0))
        .execute();

    assertEquals(HttpStatus.SC_OK, firstPageHttpResponse.getStatusLine().getStatusCode());
    SearchDirectoryResponse firstPageResponse = parseSoapResponse(firstPageHttpResponse);
    assertEquals(1, firstPageResponse.getAccounts().size());
    assertEquals(adminAccount.getName(), firstPageResponse.getAccounts().get(0).getName());
    assertEquals(3, firstPageResponse.getSearchTotal());
    assertTrue(firstPageResponse.isMore());
  }

  @Test
  void searchAccountPaginationSecondPage() throws Exception {
    HttpResponse secondPageHttpResponse = getSoapClient().newRequest()
        .setCaller(adminAccount)
        .setSoapBody(searchAccountsByDomain(DEFAULT_DOMAIN, 1, 1))
        .execute();

    assertEquals(HttpStatus.SC_OK, secondPageHttpResponse.getStatusLine().getStatusCode());
    SearchDirectoryResponse secondPageResponse = parseSoapResponse(secondPageHttpResponse);
    assertEquals(1, secondPageResponse.getAccounts().size());
    assertEquals(firstAccount.getName(), secondPageResponse.getAccounts().get(0).getName());
    assertEquals(3, secondPageResponse.getSearchTotal());
    assertTrue(secondPageResponse.isMore());
  }

  @Test
  void searchAccountPaginationLastPage() throws Exception {
    final HttpResponse lastPageHttpResponse = getSoapClient().newRequest()
        .setCaller(adminAccount)
        .setSoapBody(searchAccountsByDomain(DEFAULT_DOMAIN, 1, 2))
        .execute();

    assertEquals(HttpStatus.SC_OK, lastPageHttpResponse.getStatusLine().getStatusCode());
    SearchDirectoryResponse lastPageResponse = parseSoapResponse(lastPageHttpResponse);
    assertEquals(1, lastPageResponse.getAccounts().size());
    assertEquals(secondAccount.getName(), lastPageResponse.getAccounts().get(0).getName());
    assertEquals(3, lastPageResponse.getSearchTotal());
    assertFalse(lastPageResponse.isMore());
  }

  private static SearchDirectoryResponse parseSoapResponse(HttpResponse httpResponse) throws IOException, ServiceException {
    final String responseBody = EntityUtils.toString(httpResponse.getEntity());
    final Element rootElement = parseXML(responseBody).getElement("Body").getElement("SearchDirectoryResponse");
    return JaxbUtil.elementToJaxb(rootElement, SearchDirectoryResponse.class);
  }

  private static SearchDirectoryRequest searchAccountsByDomain(String domain) {
    return searchAccountsByDomain(domain, null, null);
  }

  private static SearchDirectoryRequest searchAccountsByDomain(String domain, Integer limit, Integer offset) {
    SearchDirectoryRequest request = new SearchDirectoryRequest();
    request.setDomain(domain);
    request.setTypes("accounts");
    request.setAttrs("");

    if (limit != null) {
      request.setLimit(limit);
    }

    if (offset != null) {
      request.setOffset(offset);
    }
    return request;
  }

  private static AccountCreator newAccountOn(String domain) {
    return accountCreatorFactory.get().withDomain(domain);
  }

}
