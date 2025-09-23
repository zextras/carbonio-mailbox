package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class ModifyDomainIT extends MailboxTestSuite {

	private static Provisioning provisioning;

	@BeforeAll
	public static void setUp() throws Exception {
		provisioning = Provisioning.getInstance();
	}

	private Domain createDomain() {
		try {
			return provisioning.createDomain(
					UUID.randomUUID() + ".test.com",
					new HashMap<>());
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	}

	private Account createDelegatedAdminAccount(Domain domain, Boolean hasRight)
			throws ServiceException {
		return provisioning.createAccount(
				"delegated.admin." + hasRight + "@" + domain.getDomainName(),
				"testPwd",
				new HashMap<>() {
					{
						put(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE");
					}
				});
	}

	private Account createGlobalAdminAccount(Domain domain) throws ServiceException {
		return provisioning.createAccount(
				"global.admin@" + domain.getDomainName(),
				"testPwd",
				new HashMap<>() {
					{
						put(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE");
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
	void shouldThrowExceptionForDelegatedAdminIfPublicServiceHostnameNotCompliantWithDomain()
			throws ServiceException {
		final String domainName = "demo.zextras.io";
		final String newPubServiceHostname = "newdemo.zextras.io";
		final Domain domain = createDomain();
		final Account adminAccount = createDelegatedAdminAccount(domain, false);
		final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
		ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
		final HashMap<String, Object> attrsToUpdate =
				new HashMap<>() {
					{
						put(ZAttrProvisioning.A_zimbraPublicServiceHostname, newPubServiceHostname);
					}
				};
		modifyDomainRequest.setAttrs(attrsToUpdate);
		assertThrows(ServiceException.class, () ->
						new MockedModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx),
				"Public service hostname must be a valid FQDN and compatible with current domain (or its"
						+ " aliases).");
	}

	@Test
	void shouldAllowDelegatedAdminWithRightChangePublicServiceHostnameNotCompliantWithDomain()
			throws ServiceException {
		final Domain domain = createDomain();
		final String domainName = domain.getDomainName();
		final String newPubServiceHostname = "virtual." + UUID.randomUUID();
		final Account adminAccount = createDelegatedAdminAccount(domain, true);
		final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
		ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
		final HashMap<String, Object> attrsToUpdate =
				new HashMap<>() {
					{
						put(ZAttrProvisioning.A_zimbraPublicServiceHostname, newPubServiceHostname);
					}
				};
		modifyDomainRequest.setAttrs(attrsToUpdate);
		new MockedModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
		assertEquals(
				newPubServiceHostname, provisioning.getDomainByName(domainName).getPublicServiceHostname());
	}

	@Test
	void shouldAllowGlobalAdminChangePublicServiceHostnameIfNotCompliantWithDomain()
			throws ServiceException {
		final Domain domain = createDomain();
		final String domainName = domain.getDomainName();
		final String newPubServiceHostname = "virtual." + UUID.randomUUID();
		final Account adminAccount = createGlobalAdminAccount(domain);
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
	void shouldUpdatePublicServiceHostnameIfCompliantWithDomain() throws ServiceException {
		final Domain domain = createDomain();
		final String domainName = domain.getName();
		final String newPubServiceHostname = "this.domain.is.legit." + domainName;
		final Account adminAccount = createDelegatedAdminAccount(domain, false);
		final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
		ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
		final HashMap<String, Object> attrsToUpdate =
				new HashMap<>() {
					{
						put(ZAttrProvisioning.A_zimbraPublicServiceHostname, newPubServiceHostname);
					}
				};
		modifyDomainRequest.setAttrs(attrsToUpdate);
		new MockedModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
		assertEquals(
				newPubServiceHostname, provisioning.getDomainByName(domainName).getPublicServiceHostname());
	}

	/**
	 * VirtualHostnames n = 1
	 */
	@Test
	void shouldAddVirtualHostnameIfCompliantWithDomain() throws ServiceException {
		final Domain domain = createDomain();
		final String domainName = domain.getDomainName();
		final String virtualHostname = "virtual." + domainName;
		final Account adminAccount = createGlobalAdminAccount(domain);
		final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
		ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
		final HashMap<String, Object> attrsToUpdate =
				new HashMap<>() {
					{
						put(ZAttrProvisioning.A_zimbraVirtualHostname, new String[]{virtualHostname});
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
	void shouldAddMultipleVirtualHostnamesIfCompliantWithDomain() throws ServiceException {
		final Domain domain = createDomain();
		final String domainName = domain.getDomainName();
		final String virtualHostname = "virtual." + domainName;
		final String virtualHostname2 = "virtual2." + domainName;
		final Account adminAccount = createGlobalAdminAccount(domain);
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
	void shouldAddMultipleVirtualHostnamesAndPublicServiceHostnameIfFQDNSEqualToDoamin()
			throws ServiceException {
		final Domain domain = createDomain();
		final String domainName = domain.getName();
		final Account adminAccount = createGlobalAdminAccount(domain);
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
	void shouldThrowExceptionForDelegatedAdminIfVirtualHostnameNotCompliantWithDomain()
			throws ServiceException {
		final Domain domain = createDomain();
		final String domainName = domain.getName();
		final String virtualHostname = "virtual.whatever.not.compliant";
		final Account adminAccount = createDelegatedAdminAccount(domain, false);
		final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
		ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
		final HashMap<String, Object> attrsToUpdate =
				new HashMap<>() {
					{
						put(ZAttrProvisioning.A_zimbraVirtualHostname, new String[]{virtualHostname});
					}
				};
		modifyDomainRequest.setAttrs(attrsToUpdate);
		assertThrows(ServiceException.class,
				() -> new MockedModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx),
				"Virtual hostnames must be valid FQDNs and compatible with current domain (or its"
						+ " aliases).");
		assertEquals(0, provisioning.getDomainByName(domainName).getVirtualHostname().length);
	}

	@Test
	void shouldAllowDelegatedAdminWithRightChangeVirtualHostnameNotCompliantWithDomain()
			throws ServiceException {
		final Domain domain = createDomain();
		final String domainName = domain.getName();
		final String virtualHostname = "virtual.whatever.not.compliant";
		final Account adminAccount = createDelegatedAdminAccount(domain, true);
		final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
		ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
		final HashMap<String, Object> attrsToUpdate =
				new HashMap<>() {
					{
						put(ZAttrProvisioning.A_zimbraVirtualHostname, new String[]{virtualHostname});
					}
				};
		modifyDomainRequest.setAttrs(attrsToUpdate);
		new MockedModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
		assertTrue(
				Arrays.stream(provisioning.getDomainByName(domainName).getVirtualHostname())
						.collect(Collectors.toList())
						.contains(virtualHostname));
	}

	@Test
	void shouldAllowGlobalAdminChangeVirtualHostnameNotCompliantWithDomain()
			throws ServiceException {
		final Domain domain = createDomain();
		final String domainName = domain.getName();
		final String virtualHostname = "virtual.whatever.not.compliant";
		final Account adminAccount = createGlobalAdminAccount(domain);
		final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
		ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
		final HashMap<String, Object> attrsToUpdate =
				new HashMap<>() {
					{
						put(ZAttrProvisioning.A_zimbraVirtualHostname, new String[]{virtualHostname});
					}
				};
		modifyDomainRequest.setAttrs(attrsToUpdate);
		new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
		assertTrue(
				Arrays.stream(provisioning.getDomainByName(domainName).getVirtualHostname())
						.collect(Collectors.toList())
						.contains(virtualHostname));
	}

	@Test
	void shouldThrowDomainNameImmutableWhenModifyingDomainName() throws ServiceException {
		final String domainName = "demo4.zextras.io";
		final String newDomainName = "newDemo4.zextras.io";
		final Domain domain = createDomain();
		final Account adminAccount = createGlobalAdminAccount(domain);
		final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
		ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
		final HashMap<String, Object> attrsToUpdate =
				new HashMap<>() {
					{
						put(ZAttrProvisioning.A_zimbraDomainName, newDomainName);
					}
				};
		modifyDomainRequest.setAttrs(attrsToUpdate);
		assertThrows(ServiceException.class,
				() -> new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx),
				ZAttrProvisioning.A_zimbraDomainName + " cannot be changed.");
	}

	/**
	 * [CO-544] empty virtual hostnames should remove all virtual hostnames
	 */
	@Test
	void shouldRemoveVirtualHostnamesWhenVirtualHostnamesEmptyArray() throws ServiceException {
		final Domain domain = createDomain();
		final String domainName = domain.getName();
		final String virtual1 = "virtual1" + domainName;
		final String virtual2 = "virtual2" + domainName;
		final Account adminAccount = createGlobalAdminAccount(domain);
		final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
		ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
		final HashMap<String, Object> attrsToUpdate =
				new HashMap<>() {
					{
						put(ZAttrProvisioning.A_zimbraVirtualHostname, new String[]{""});
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
	void shouldNotRemoveVirtualHostnamesWhenVirtualHostnamesNotPassed()
			throws ServiceException {
		final Domain domain = createDomain();
		final String domainName = domain.getName();
		final String virtual1 = "virtual1" + domainName;
		final String virtual2 = "virtual2" + domainName;
		domain.modify(new HashMap<>(
				Map.of(
						ZAttrProvisioning.A_zimbraVirtualHostname, new String[]{virtual1, virtual2}
				)));
		final Account adminAccount = createGlobalAdminAccount(domain);
		final Map<String, Object> ctx = getSoapContextFromAccount(adminAccount);
		ModifyDomainRequest modifyDomainRequest = new ModifyDomainRequest(domain.getId());
		final HashMap<String, Object> attrsToUpdate =
				new HashMap<>() {
				};
		modifyDomainRequest.setAttrs(attrsToUpdate);
		new ModifyDomain().handle(JaxbUtil.jaxbToElement(modifyDomainRequest), ctx);
		assertEquals(
				2, Arrays.stream(provisioning.getDomainByName(domainName).getVirtualHostname()).count());
	}

	//skip checking delegated admin rights
	private static class MockedModifyDomain extends ModifyDomain {

		@Override
		protected AdminAccessControl checkDomainRight(ZimbraSoapContext zsc, Domain d, Object needed)
				throws ServiceException {
			return AdminAccessControl.getAdminAccessControl(zsc);
		}

		@Override
		protected boolean hasAdminRightAndCanModifyConfig(AdminAccessControl adminAccessControl,
				Config config) {
			return adminAccessControl.mAuthedAcct.getName().contains("delegated.admin.true");
		}
	}
}

