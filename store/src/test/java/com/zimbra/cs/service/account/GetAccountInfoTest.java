package com.zimbra.cs.service.account;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Maps;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.GetAccountInfoRequest;
import com.zimbra.soap.account.message.GetAccountInfoResponse;
import com.zimbra.soap.type.AccountSelector;
import com.zimbra.soap.type.NamedValue;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GetAccountInfoTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  void shouldContainAccountStatusInReturnedAttributes() throws Exception {
    Provisioning prov = Provisioning.getInstance();
    Domain domain = prov.createDomain(UUID.randomUUID().toString(), Maps.newHashMap());
    Account account = prov.createAccount(UUID.randomUUID() + "@" + domain.getName(),
        UUID.randomUUID().toString(), Maps.newHashMap());
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
}