package com.zimbra.cs.service.admin;

import static org.junit.Assert.*;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.ModifyDomainRequest;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ModifyDomainIT {

  private static Provisioning provisioning;

  @BeforeClass
  public static void setUp() throws Exception {
    MailboxTestUtil.initServer();
    provisioning = Provisioning.getInstance();
  }

  @Rule public ExpectedException expectedEx = ExpectedException.none();

  private Account createDelegatedAdminForDomain(Domain domain) throws ServiceException {
    return provisioning.createAccount(
        "admin@" + domain.getDomainName(),
        "testPwd",
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE");
            put(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE");
          }
        });
  }

  private HashMap<String, Object> getSoapContextFromAccount(Account account)
      throws ServiceException {
    return new HashMap<String, Object>() {
      {
        put(
            SoapEngine.ZIMBRA_CONTEXT,
            new ZimbraSoapContext(
                AuthProvider.getAuthToken(account, true),
                account.getId(),
                SoapProtocol.Soap12,
                SoapProtocol.Soap12));
      }
    };
  }

  @Test
  public void shouldThrowServiceExceptionIfPublicServiceHostnameNotSubdomainOfDomainName()
      throws ServiceException {
    final String domainName = "demo.zextras.io";
    final String newPubServiceHostname = "this.domain.iz.fake";
    expectedEx.expect(ServiceException.class);
    expectedEx.expectMessage(
        "Public service hostname "
            + newPubServiceHostname
            + " must be subdomain of "
            + domainName
            + ".");
    final Domain domain =
        provisioning.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraDomainName, domainName);
              }
            });
    final Account adminAccount = createDelegatedAdminForDomain(domain);
    final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
    ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
    final HashMap<String, Object> attrsToUpdate =
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraPublicServiceHostname, newPubServiceHostname);
          }
        };
    modifyDomainRequest.setAttrs(attrsToUpdate);
    new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
  }

  @Test
  public void shouldUpdatePublicServiceHostnameIfSubdomainOfDomainName() throws ServiceException {
    final String domainName = "demo2.zextras.io";
    final String newPubServiceHostname = "this.domain.is.legit.demo2.zextras.io";
    final Domain domain =
        provisioning.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraDomainName, domainName);
              }
            });
    final Account adminAccount = createDelegatedAdminForDomain(domain);
    final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
    ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
    final HashMap<String, Object> attrsToUpdate =
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraPublicServiceHostname, newPubServiceHostname);
          }
        };
    modifyDomainRequest.setAttrs(attrsToUpdate);
    new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
    assertEquals(
        newPubServiceHostname, provisioning.getDomainByName(domainName).getPublicServiceHostname());
  }

  @Test
  public void shouldThrowDomainNameImmutableWhenModifyingDomainName() throws ServiceException {
    expectedEx.expect(ServiceException.class);
    expectedEx.expectMessage(ZAttrProvisioning.A_zimbraDomainName + " cannot be changed.");
    final String domainName = "demo3.zextras.io";
    final String newDomainName = "newDemo3.zextras.io";
    final Domain domain =
        provisioning.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraDomainName, domainName);
              }
            });
    final Account adminAccount = createDelegatedAdminForDomain(domain);
    final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
    ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
    final HashMap<String, Object> attrsToUpdate =
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraDomainName, newDomainName);
          }
        };
    modifyDomainRequest.setAttrs(attrsToUpdate);
    new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
  }
}
