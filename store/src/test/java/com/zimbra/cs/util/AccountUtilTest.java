package com.zimbra.cs.util;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AccountUtilTest extends MailboxTestSuite {

	private static Provisioning provisioning;

	@BeforeAll
	static void setUpClass() throws Exception {
		provisioning = Provisioning.getInstance();
	}

	@Test
	void shouldGetCanonicalAddressWhenSet() throws ServiceException {
		final String canonicalAddress = "canonicaltest321@zextras.com";
		final Account account = createAccount().withAttribute(Provisioning.A_zimbraMailCanonicalAddress,
				canonicalAddress).create();
		assertEquals(canonicalAddress, AccountUtil.getCanonicalAddress(account));
	}

	@Test
	void shouldGetCanonicalAddressWhenNotSet() throws ServiceException {
		final Account account = createAccount().create();
		assertEquals(account.getName(), AccountUtil.getCanonicalAddress(account));
	}

	@Test
	void shouldGetSoapUriWhenCalled() throws ServiceException {
		final String serverHostname = "demo.zextras.com";
		// Note: server name must be equal to serviceHostname. If not everything falls apart. It's absurd.
		final Server server = provisioning.createServer(serverHostname, new HashMap<>(
				Map.of(
						ZAttrProvisioning.A_zimbraServiceHostname, serverHostname,
						ZAttrProvisioning.A_zimbraServiceEnabled, "service",
						ZAttrProvisioning.A_zimbraMailPort, "80"
				)));
		final Domain domain = provisioning.createDomain(serverHostname, new HashMap<>());
		final Account account = provisioning.createAccount("test@" + domain.getName(), "password",
				new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, server.getName())));

		assertEquals("http://demo.zextras.com:80/service/soap/", AccountUtil.getSoapUri(account));
	}

	@Test
	void shouldReturnBooleanWhenIsGalAccountCalled() throws ServiceException {

		final Account account = createAccount().create();
		Domain domain = Provisioning.getInstance().getDomainByName(account.getDomainName());

		// galAccountId is unset in domain; isGalSyncAccount should return false
		domain.unsetGalAccountId();
		assertFalse(AccountUtil.isGalSyncAccount(account));

		// galAccountId is set in domain; isGalSyncAccount should return true
		domain.setGalAccountId(new String[]{account.getId()});
		assertTrue(AccountUtil.isGalSyncAccount(account));
	}

	@Test
	void shouldReturnFalseWhenDomainIsExternalAndAddressHasInternalDomainCalled()
			throws ServiceException {
		assertFalse(AccountUtil.addressHasInternalDomain("kiraplsignh@gmail.com"));
	}

	@Test
	void shouldReturnTrueWhenDomainIsExternalAndAddressHasInternalDomainCalled()
			throws ServiceException {
		Account account = createAccount().create();
		assertTrue(AccountUtil.addressHasInternalDomain(account.getName()));
	}

}
