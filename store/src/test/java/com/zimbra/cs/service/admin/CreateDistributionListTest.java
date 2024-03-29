package com.zimbra.cs.service.admin;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.DynamicGroup;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.CreateDistributionListRequest;
import com.zimbra.soap.admin.type.Attr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class CreateDistributionListTest {

  private static final String DOMAIN_NAME = "test.com";
  private static final String ADMIN_USER_NAME = "admin";
  private static final String DOMAIN_ADMIN_EMAIL = ADMIN_USER_NAME + "@" + DOMAIN_NAME;
  private static final  String DISTRIBUTION_LIST_NAME = "developers" + "@" + DOMAIN_NAME;
  private static final String DOMAIN_ADMIN_PASSWORD = "assext";

  private static Provisioning provisioningSpy;

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    provisioningSpy = spy(Provisioning.getInstance());
  }

  @AfterAll
  public static void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

 /**
  * Delegated Admins are not allowed to create Dynamic Distribution Lists see issue CO-526
  *
  * @throws ServiceException if any
  */
 @Test
 void shouldThrowServiceExceptionWhenDelegatedAdminRequestToCreateDynamicDistributionList()
   throws ServiceException {

  final HashMap<String, Object> attrs = new HashMap<>();

  //create domain
  provisioningSpy.createDomain(DOMAIN_NAME, attrs);

  //create domain admin user
  attrs.put(Provisioning.A_zimbraIsDelegatedAdminAccount,
    ProvisioningConstants.TRUE);
  final Account domainAdminAccount = provisioningSpy.createAccount(DOMAIN_ADMIN_EMAIL,
    DOMAIN_ADMIN_PASSWORD,
    attrs);

  //AuthToken mock, set auth account as delegatedAdmin
  final AuthToken authTokenMock = mock(AuthToken.class);
  when(authTokenMock.isDelegatedAdmin()).thenReturn(true);

  //create soap context with delegated admin auth
  final Map<String, Object> context = new HashMap<>();
  final ZimbraSoapContext zsc = new ZimbraSoapContext(authTokenMock, domainAdminAccount.getId(),
    SoapProtocol.Soap12, SoapProtocol.Soap12);
  context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);

  //create CreateDistributionListRequest
  List<Attr> attributes = new ArrayList<>();
  attributes.add(new Attr(Provisioning.A_memberURL,
    "ldap:///??sub?(&amp;(objectClass=zimbraAccount)(ZimbraAccountStatus=active))"));
  attributes.add(new Attr(Provisioning.A_zimbraIsACLGroup, ProvisioningConstants.TRUE));
  final CreateDistributionListRequest createDistributionListRequest = new CreateDistributionListRequest(
    DISTRIBUTION_LIST_NAME, attributes, true);

  //create CreateDistributionList
  final CreateDistributionList createDistributions = new CreateDistributionList();

  //stub createGroup method of provisioning since it is not supported without real provisioning backend
  final DynamicGroup mockDGroup = mock(DynamicGroup.class);
  doReturn(mockDGroup).when(provisioningSpy).createGroup(anyString(), anyMap(), anyBoolean());

  //execute request
  assertThrows(ServiceException.class, () -> createDistributions.handle(JaxbUtil.jaxbToElement(createDistributionListRequest), context),
      "Delegated Admins are not allowed to create Dynamic Distribution Lists");
 }
}
