package com.zimbra.cs.service.account;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
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

    Element handle = new GetAccountInfo()
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

    Element handle = new GetAccountInfo()
        .handle(requestElement, ServiceTestUtil.getRequestContext(account));
    GetAccountInfoResponse response = JaxbUtil.elementToJaxb(handle);

    List<NamedValue> attributes = Objects.requireNonNull(response).getAttrs()
        .stream()
        .filter(namedValue -> namedValue.getName().contains(Provisioning.A_zimbraIsExternalVirtualAccount))
        .collect(Collectors.toList());
    assertEquals(1, attributes.size());
    assertEquals("FALSE", attributes.get(0).getValue());
  }
}