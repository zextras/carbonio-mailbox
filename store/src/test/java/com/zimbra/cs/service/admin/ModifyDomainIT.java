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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ModifyDomainIT {
  private Provisioning provisioning;

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.initServer();
    provisioning = Provisioning.getInstance();
  }

  @After
  public void clearData() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Rule public ExpectedException expectedEx = ExpectedException.none();

  private Account createAdminAccount(Domain domain, String key) throws ServiceException {
    return provisioning.createAccount(
        "admin@" + domain.getDomainName(),
        "testPwd",
        new HashMap<>() {
          {
            put(key, "TRUE");
          }
        });
  }

  private HashMap<String, Object> getSoapContextFromAccount(Account account)
      throws ServiceException {
    return new HashMap<>() {
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
  public void shouldThrowExceptionForDelegatedAdminIfPublicServiceHostnameNotComplaintWithDomain()
      throws ServiceException {
    final String domainName = "demo.zextras.io";
    final Domain domain = provisioning.createDomain(
        domainName,
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraDomainName, domainName);
          }
        });
    final String newPubServiceHostname = "newdemo.zextras.io";
    expectedEx.expect(ServiceException.class);
    expectedEx.expectMessage(
        "Public service hostname must be a valid FQDN and compatible with current domain (or its"
            + " aliases).");

    final Account adminAccount = createAdminAccount(domain, ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount);
    final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
    ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
    final HashMap<String, Object> attrsToUpdate =
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraPublicServiceHostname, newPubServiceHostname);
          }
        };
    modifyDomainRequest.setAttrs(attrsToUpdate);
    new DumbModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
  }

  @Test
  public void shouldAllowGlobalAdminChangePublicServiceHostnameIfNotComplaintWithDomain()
      throws ServiceException {
    final String domainName = "demo.zextras.io";
    final Domain domain = provisioning.createDomain(
        domainName,
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraDomainName, domainName);
          }
        });
    final String newPubServiceHostname = "newdemo.zextras.io";
    final Account adminAccount = createAdminAccount(domain, ZAttrProvisioning.A_zimbraIsAdminAccount);
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
  public void shouldUpdatePublicServiceHostnameIfCompliantWithDomain() throws ServiceException {
    final String domainName = "demo.zextras.io";
    final Domain domain = provisioning.createDomain(
        domainName,
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraDomainName, domainName);
          }
        });
    final String newPubServiceHostname = "this.domain.is.legit.demo.zextras.io";
    final Account adminAccount = createAdminAccount(domain, ZAttrProvisioning.A_zimbraIsAdminAccount);
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
  public void shouldThrowExceptionForDelegatedAdminIfVirtualHostnameNotCompliantWithDomain()
      throws ServiceException {
    final String domainName = UUID.randomUUID() + ".zextras.io";
    final String virtualHostname = "virtual.whatever.not.compliant";
    expectedEx.expect(ServiceException.class);
    expectedEx.expectMessage(
        "Virtual hostnames must be valid FQDNs and compatible with current domain (or its"
            + " aliases).");
    final Domain domain =
        provisioning.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraDomainName, domainName);
              }
            });
    final Account adminAccount = createAdminAccount(domain, ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount);
    final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
    ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
    final HashMap<String, Object> attrsToUpdate =
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraVirtualHostname, new String[] {virtualHostname});
          }
        };
    modifyDomainRequest.setAttrs(attrsToUpdate);
    new DumbModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
    assertEquals(0, provisioning.getDomainByName(domainName).getVirtualHostname().length);
  }

  @Test
  public void shouldAllowGlobalAdminChangeVirtualHostnameIfNotCompliantWithDomain()
      throws ServiceException {
    final String domainName = UUID.randomUUID() + ".zextras.io";
    final String virtualHostname = "virtual.whatever.not.compliant";
    final Domain domain =
        provisioning.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraDomainName, domainName);
              }
            });
    final Account adminAccount = createAdminAccount(domain, ZAttrProvisioning.A_zimbraIsAdminAccount);
    final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
    ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
    final HashMap<String, Object> attrsToUpdate =
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraVirtualHostname, new String[] {virtualHostname});
          }
        };
    modifyDomainRequest.setAttrs(attrsToUpdate);
    new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
    assertEquals(virtualHostname, provisioning.getDomainByName(domainName).getVirtualHostname()[0]);
  }

  /**
   * VirtualHostnames n = 1
   */
  @Test
  public void shouldAddVirtualHostnameIfCompliantWithDomain() throws ServiceException {
    final String domainName = UUID.randomUUID() + ".zextras.io";
    final String virtualHostname = "virtual." + domainName;
    final Domain domain =
        provisioning.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraDomainName, domainName);
              }
            });
    final Account adminAccount = createAdminAccount(domain, ZAttrProvisioning.A_zimbraIsAdminAccount);
    final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
    ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
    final HashMap<String, Object> attrsToUpdate =
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraVirtualHostname, new String[] {virtualHostname});
          }
        };
    modifyDomainRequest.setAttrs(attrsToUpdate);
    new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
    assertTrue(
        Arrays.stream(provisioning.getDomainByName(domainName).getVirtualHostname())
            .collect(Collectors.toList())
            .contains(virtualHostname));
  }

  /**
   * VirtualHostnames n > 1. Reason is we get a list, with n = 1 we get just a String.
   */
  @Test
  public void shouldAddMultipleVirtualHostnamesIfCompliantWithDomain() throws ServiceException {
    final String domainName = UUID.randomUUID() + ".zextras.io";
    final String virtualHostname = "virtual." + domainName;
    final String virtualHostname2 = "virtual2." + domainName;
    final Domain domain =
        provisioning.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraDomainName, domainName);
              }
            });
    final Account adminAccount = createAdminAccount(domain, ZAttrProvisioning.A_zimbraIsAdminAccount);
    final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
    ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
    final String[] vHostnames = {virtualHostname, virtualHostname2};
    final HashMap<String, Object> attrsToUpdate =
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraVirtualHostname, vHostnames);
          }
        };
    modifyDomainRequest.setAttrs(attrsToUpdate);
    new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
    assertEquals(2, provisioning.getDomainByName(domainName).getVirtualHostname().length);
    assertTrue(
        Arrays.stream(provisioning.getDomainByName(domainName).getVirtualHostname())
            .collect(Collectors.toList())
            .containsAll(Arrays.stream(vHostnames).collect(Collectors.toList())));
  }

  @Test
  public void shouldAddMultipleVirtualHostnamesAndPublicServiceHostnameIfFQDNSEqualToDomain()
      throws ServiceException {
    final String domainName = UUID.randomUUID() + ".zextras.io";
    final Domain domain =
        provisioning.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraDomainName, domainName);
              }
            });
    final Account adminAccount = createAdminAccount(domain, ZAttrProvisioning.A_zimbraIsAdminAccount);
    final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
    ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
    final String[] vHostnames = {domainName};
    final HashMap<String, Object> attrsToUpdate =
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraPublicServiceHostname, domainName);
            put(ZAttrProvisioning.A_zimbraVirtualHostname, vHostnames);
          }
        };
    modifyDomainRequest.setAttrs(attrsToUpdate);
    new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
    assertEquals(1, provisioning.getDomainByName(domainName).getVirtualHostname().length);
    assertEquals(domainName, provisioning.getDomainByName(domainName).getPublicServiceHostname());
    assertTrue(
        Arrays.stream(provisioning.getDomainByName(domainName).getVirtualHostname())
            .collect(Collectors.toList())
            .containsAll(Arrays.stream(vHostnames).collect(Collectors.toList())));
  }



  @Test
  public void shouldThrowDomainNameImmutableWhenModifyingDomainName() throws ServiceException {
    expectedEx.expect(ServiceException.class);
    expectedEx.expectMessage(ZAttrProvisioning.A_zimbraDomainName + " cannot be changed.");
    final String domainName = "demo4.zextras.io";
    final String newDomainName = "newDemo4.zextras.io";
    final Domain domain =
        provisioning.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraDomainName, domainName);
              }
            });
    final Account adminAccount = createAdminAccount(domain, ZAttrProvisioning.A_zimbraIsAdminAccount);
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

  /**
   * [CO-544] empty virtual hostnames should remove all virtual hostnames
   */
  @Test
  public void shouldRemoveVirtualHostnamesWhenVirtualHostnamesEmptyArray() throws ServiceException {
    final String domainName = UUID.randomUUID() + ".zextras.io";
    final String virtual1 = "virtual1" + domainName;
    final String virtual2 = "virtual2" + domainName;
    final Domain domain =
        provisioning.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraDomainName, domainName);
                put(ZAttrProvisioning.A_zimbraVirtualHostname, new String[] {virtual1, virtual2});
              }
            });
    final Account adminAccount = createAdminAccount(domain, ZAttrProvisioning.A_zimbraIsAdminAccount);
    final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
    ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
    final HashMap<String, Object> attrsToUpdate =
        new HashMap<>() {
          {
            put(ZAttrProvisioning.A_zimbraVirtualHostname, new String[] {""});
          }
        };
    modifyDomainRequest.setAttrs(attrsToUpdate);
    new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
    assertEquals(
        0, Arrays.stream(provisioning.getDomainByName(domainName).getVirtualHostname()).count());
  }

  /**
   * [CO-544] when zimbraVirtualHostname not passed it should not remove existing hostnames
   */
  @Test
  public void shouldNotRemoveVirtualHostnamesWhenVirtualHostnamesNotPassed()
      throws ServiceException {
    final String domainName = UUID.randomUUID() + ".zextras.io";
    final String virtual1 = "virtual1" + domainName;
    final String virtual2 = "virtual2" + domainName;
    final Domain domain =
        provisioning.createDomain(
            domainName,
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraDomainName, domainName);
                put(ZAttrProvisioning.A_zimbraVirtualHostname, new String[] {virtual1, virtual2});
              }
            });
    final Account adminAccount = createAdminAccount(domain, ZAttrProvisioning.A_zimbraIsAdminAccount);
    final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
    ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
    final HashMap<String, Object> attrsToUpdate =
        new HashMap<>() {};
    modifyDomainRequest.setAttrs(attrsToUpdate);
    new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
    assertEquals(
        2, Arrays.stream(provisioning.getDomainByName(domainName).getVirtualHostname()).count());
  }

  //skip checking delegated admin rights
  private static class DumbModifyDomain extends ModifyDomain {
    @Override
    protected AdminAccessControl checkDomainRight(ZimbraSoapContext zsc, Domain d, Object needed)
        throws ServiceException {
      return AdminAccessControl.getAdminAccessControl(zsc);
    }
  }
}
