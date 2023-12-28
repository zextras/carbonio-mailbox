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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.zimbra.common.soap.Element.parseXML;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("api")
class SearchDirectoryTest extends SoapTestSuite {
  private static AccountCreator.Factory accountCreatorFactory;
  private static Provisioning provisioning;

  @BeforeAll
  static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    accountCreatorFactory = new AccountCreator.Factory(provisioning);
  }

  @Test
  void todo() throws Exception {
    Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
    String domain = adminAccount.getDomainName();

    final HttpResponse httpResponse = getSoapClient().newRequest()
        .setCaller(adminAccount)
        .setSoapBody(searchAccountsByDomain(domain))
        .execute();

    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    SearchDirectoryResponse response = parseSoapResponse(httpResponse);
    assertEquals(1, response.getAccounts().size());
  }

  private static SearchDirectoryResponse parseSoapResponse(HttpResponse httpResponse) throws IOException, ServiceException {
    final String responseBody = EntityUtils.toString(httpResponse.getEntity());
    final Element rootElement = parseXML(responseBody).getElement("Body").getElement("SearchDirectoryResponse");
    return JaxbUtil.elementToJaxb(rootElement, SearchDirectoryResponse.class);
  }

  private static SearchDirectoryRequest searchAccountsByDomain(String domain) {
    SearchDirectoryRequest request = new SearchDirectoryRequest();
    request.setDomain(domain);
    request.setTypes("accounts");
    request.setAttrs("mail");
    return request;
  }

//  @Test
//  void shouldRenameAccountByChangingOnlyDomain() throws Exception {
//    Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
//    final String accountName = UUID.randomUUID().toString();
//    Account userAccount = accountCreatorFactory.get().withUsername(accountName).create();
//    final Domain newDomain = provisioning.createDomain("myDomain.com", new HashMap<>());
//
//    RenameAccountRequest request =
//        new RenameAccountRequest(userAccount.getId(), accountName + "@" + newDomain.getName());
//    final HttpResponse response =
//        getSoapClient().newRequest().setCaller(adminAccount).setSoapBody(request).execute();
//
//    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
//  }
//
//  @Test
//  void shouldThrowNoSuchDomainWhenRenamingToNotExistingDomain() throws Exception {
//    Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
//    final String accountName = UUID.randomUUID().toString();
//    Account userAccount = accountCreatorFactory.get().withUsername(accountName).create();
//
//    RenameAccountRequest request =
//        new RenameAccountRequest(userAccount.getId(), accountName + "@" + UUID.randomUUID() + ".com");
//    final HttpResponse response =
//        getSoapClient().newRequest().setCaller(adminAccount).setSoapBody(request).execute();
//
//    Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
//    final String responseBody = EntityUtils.toString(response.getEntity());
//    Assertions.assertTrue(responseBody.contains(ERROR_CODE_NO_SUCH_DOMAIN));
//  }
}
