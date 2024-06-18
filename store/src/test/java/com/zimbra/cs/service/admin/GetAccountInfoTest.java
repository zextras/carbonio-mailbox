package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Maps;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetAccountInfoRequest;
import com.zimbra.soap.admin.message.GetAccountInfoResponse;
import com.zimbra.soap.admin.type.Attr;
import com.zimbra.soap.type.AccountSelector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    RightManager.getInstance();
  }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  void shouldContainAccountStatusInReturnedAttributes() throws Exception {
    Provisioning prov = Provisioning.getInstance();
    Domain domain = prov.createDomain(UUID.randomUUID().toString(), Maps.newHashMap());
    Map<String, Object> attributes = new HashMap<>();
    attributes.put(Provisioning.A_zimbraIsAdminAccount, "TRUE");
    Account adminAccount = prov.createAccount(UUID.randomUUID() + "@" + domain.getName(),
        UUID.randomUUID().toString(), attributes);
    GetAccountInfoRequest request = new GetAccountInfoRequest();
    request.setAccount(AccountSelector.fromId(adminAccount.getId()));
    Element requestElement = JaxbUtil.jaxbToElement(request);

    Element handle = new GetAccountInfo().handle(requestElement, getAdminContext(adminAccount));
    GetAccountInfoResponse response = JaxbUtil.elementToJaxb(handle);

    List<Attr> attrs = Objects.requireNonNull(response).getAttrList()
        .stream()
        .filter(attr -> attr.getKey().equals(Provisioning.A_zimbraAccountStatus))
        .collect(Collectors.toList());
    assertEquals(1, attrs.size());
  }

  @Test
  void shouldContainExternalVirtualInReturnedAttributes() throws Exception {
    Provisioning prov = Provisioning.getInstance();
    Domain domain = prov.createDomain(UUID.randomUUID().toString(), Maps.newHashMap());
    Map<String, Object> attributes = new HashMap<>();
    attributes.put(Provisioning.A_zimbraIsAdminAccount, "TRUE");
    attributes.put(Provisioning.A_zimbraIsExternalVirtualAccount, "TRUE");
    Account adminAccount = prov.createAccount(UUID.randomUUID() + "@" + domain.getName(),
        UUID.randomUUID().toString(), attributes);
    GetAccountInfoRequest request = new GetAccountInfoRequest();
    request.setAccount(AccountSelector.fromId(adminAccount.getId()));
    Element requestElement = JaxbUtil.jaxbToElement(request);

    Element handle = new GetAccountInfo().handle(requestElement, getAdminContext(adminAccount));
    GetAccountInfoResponse response = JaxbUtil.elementToJaxb(handle);

    List<Attr> attrs = Objects.requireNonNull(response).getAttrList()
        .stream()
        .filter(attr -> attr.getKey().equals(Provisioning.A_zimbraIsExternalVirtualAccount))
        .collect(Collectors.toList());
    assertEquals(1, attrs.size());
    assertEquals("TRUE", attrs.get(0).getValue());
  }

  Map<String, Object> getAdminContext(Account account) throws ServiceException {
    Map<String, Object> context = new HashMap<>();
    var zsc = new ZimbraSoapContext(
            AuthProvider.getAuthToken(account, true),
            account.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    return context;
  }
}