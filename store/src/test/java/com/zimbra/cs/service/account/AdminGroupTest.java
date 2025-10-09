package com.zimbra.cs.service.account;

import com.google.common.collect.Maps;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.SoapClient.Request;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.soap.admin.message.GetAccountRequest;
import com.zimbra.soap.admin.message.GetCosRequest;
import com.zimbra.soap.admin.message.GetDomainRequest;
import com.zimbra.soap.admin.message.GetServerRequest;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.admin.type.CosSelector.CosBy;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.DomainSelector.DomainBy;
import com.zimbra.soap.admin.type.ServerSelector;
import com.zimbra.soap.admin.type.ServerSelector.ServerBy;
import com.zimbra.soap.type.AccountBy;
import com.zimbra.soap.type.AccountSelector;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AdminGroupTest extends SoapTestSuite {

	private static Provisioning provisioning;

	@BeforeAll
	static void setUp() {
		provisioning = Provisioning.getInstance();
	}

	@Test
	void globalAdmin_IsAbleToRetrieve_COS_Account_Domain() throws Exception {
		final Account account = createAccount().asGlobalAdmin().create();

		assertAllowed(getCOSAdminApi(account));
		assertAllowed(getAccountAdminApi(account));
		assertAllowed(getServerAdminApi(account));
		assertAllowed(getOtherDomainAdminApi(account));
	}

	@Test
	void delegatedAdmin_Is_NOT_AbleToRetrieve_COS_Account_Domain() throws Exception {
		final Account account = createAccount().create();
		account.setIsDelegatedAdminAccount(true);

		assertDenied(getCOSAdminApi(account));
		assertDenied(getAccountAdminApi(account));
		assertDenied(getServerAdminApi(account));
		assertDenied(getOtherDomainAdminApi(account));
	}

	@Test
	void domainAdminWithoutDelegatedAdmin_Is_NOT_AbleToRetrieve_COS_Account_Domain() throws Exception {
		final Account account = createAccount().create();
		account.setIsDomainAdminAccount(true);

		assertDenied(getCOSAdminApi(account));
		assertDenied(getAccountAdminApi(account));
		assertDenied(getServerAdminApi(account));
		assertDenied(getOtherDomainAdminApi(account));
	}

	private static void assertDenied(SoapResponse response) {
		Assertions.assertTrue(response.body().contains("permission denied"));
	}

	private static void assertAllowed(SoapResponse cosAdminApi) {
		Assertions.assertFalse(cosAdminApi.body().contains("permission denied"));
	}

	private SoapResponse getCOSAdminApi(Account account) throws Exception {
		final Cos cos = provisioning.createCos(UUID.randomUUID().toString(), Maps.newHashMap());
		final GetCosRequest getCosRequest = new GetCosRequest(new CosSelector(CosBy.id, cos.getId()));
		final Request request = getSoapClient().newAdminRequest().setCaller(account)
				.setSoapBody(getCosRequest);
		return request.call();
	}

	private SoapResponse getAccountAdminApi(Account account) throws Exception {
		final Account accountToRetrieve = createAccount().create();
		final GetAccountRequest getAccountRequest = new GetAccountRequest(new AccountSelector(AccountBy.id, accountToRetrieve.getId()));
		return executeRequest(account, getAccountRequest);
	}

	private SoapResponse getServerAdminApi(Account account) throws Exception {
		final Server localServer = provisioning.getLocalServer();
		final GetServerRequest getServerRequest = new GetServerRequest(new ServerSelector(ServerBy.id, localServer.getId()), false);
		return executeRequest(account, getServerRequest);
	}

	private SoapResponse getOtherDomainAdminApi(Account account) throws Exception {
		final Domain domain = provisioning.createDomain(UUID.randomUUID().toString() + ".com", Maps.newHashMap());
		final GetDomainRequest getDomainRequest = new GetDomainRequest(new DomainSelector(DomainBy.id, domain.getId()), false);
		return executeRequest(account, getDomainRequest);
	}

	private SoapResponse executeRequest(Account account, Object getDomainRequest)
			throws Exception {
		final Request request = getSoapClient().newAdminRequest().setCaller(account)
				.setSoapBody(getDomainRequest);
		return request.call();
	}

}
