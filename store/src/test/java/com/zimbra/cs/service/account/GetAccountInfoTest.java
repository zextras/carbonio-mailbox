package com.zimbra.cs.service.account;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.GetAccountInfoRequest;
import com.zimbra.soap.account.message.GetAccountInfoResponse;
import com.zimbra.soap.type.AccountSelector;
import com.zimbra.soap.type.NamedValue;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class GetAccountInfoTest extends MailboxTestSuite {

  @Test
  void shouldContainAccountStatusInReturnedAttributes() throws Exception {
    Account account = createAccount().create();
    GetAccountInfoRequest request = new GetAccountInfoRequest();
    request.setAccount(AccountSelector.fromId(account.getId()));
    Element requestElement = JaxbUtil.jaxbToElement(request);

    Element handle = newRequest()
        .handle(requestElement, ServiceTestUtil.getRequestContext(account));
    GetAccountInfoResponse response = JaxbUtil.elementToJaxb(handle);

    List<NamedValue> attributes = Objects.requireNonNull(response).getAttrs()
        .stream()
        .filter(namedValue -> namedValue.getName().contains(Provisioning.A_zimbraAccountStatus))
        .collect(Collectors.toList());
    assertEquals(1, attributes.size());
  }

  @Test
  void shouldContainExternalVirtualInReturnedAttributes() throws Exception {
    Account account = createAccount().create();
    GetAccountInfoRequest request = new GetAccountInfoRequest();
    request.setAccount(AccountSelector.fromId(account.getId()));
    Element requestElement = JaxbUtil.jaxbToElement(request);

    Element handle = newRequest()
        .handle(requestElement, ServiceTestUtil.getRequestContext(account));
    GetAccountInfoResponse response = JaxbUtil.elementToJaxb(handle);

    List<NamedValue> attributes = Objects.requireNonNull(response).getAttrs()
        .stream()
        .filter(namedValue -> namedValue.getName().contains(Provisioning.A_zimbraIsExternalVirtualAccount))
        .collect(Collectors.toList());
    assertEquals(1, attributes.size());
    assertEquals("FALSE", attributes.get(0).getValue());
  }

  @Test
  void shouldDenyInfoOfAnotherAccountLikeAnUnknownAccount() throws Exception {
    Account caller = createAccount().create();
    Account other = createAccount().create();

    ServiceException otherAccount = assertThrows(ServiceException.class,
        () -> getAccountInfoByName(caller, other.getName()));
    ServiceException unknownAccount = assertThrows(ServiceException.class,
        () -> getAccountInfoByName(caller, "unknown-" + other.getName()));

    assertEquals(ServiceException.PERM_DENIED, otherAccount.getCode());
    assertEquals(unknownAccount.getCode(), otherAccount.getCode());
    assertEquals(unknownAccount.getMessage(), otherAccount.getMessage());
  }

  @Test
  void shouldReturnInfoOfAnotherAccountToGlobalAdmin() throws Exception {
    Account admin = createAccount().asGlobalAdmin().create();
    Account other = createAccount().create();

    GetAccountInfoResponse response = JaxbUtil.elementToJaxb(getAccountInfoByName(admin, other.getName()));

    assertEquals(other.getName(), Objects.requireNonNull(response).getName());
  }

  @Test
  void shouldReturnOwnInfoByName() throws Exception {
    Account caller = createAccount().create();

    GetAccountInfoResponse response = JaxbUtil.elementToJaxb(getAccountInfoByName(caller, caller.getName()));

    assertEquals(caller.getName(), Objects.requireNonNull(response).getName());
  }

  private Element getAccountInfoByName(Account caller, String name) throws Exception {
    GetAccountInfoRequest request = new GetAccountInfoRequest();
    request.setAccount(AccountSelector.fromName(name));
    return newRequest().handle(JaxbUtil.jaxbToElement(request), ServiceTestUtil.getRequestContext(caller));
  }

  private GetAccountInfo newRequest() {
    final GetAccountInfo getAccountInfo = new GetAccountInfo();
    getAccountInfo.setResponseQName(AccountConstants.GET_ACCOUNT_INFO_RESPONSE);
    return getAccountInfo;

  }

}