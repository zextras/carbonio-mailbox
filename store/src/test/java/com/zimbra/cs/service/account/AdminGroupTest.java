package com.zimbra.cs.service.account;

import com.google.common.collect.Maps;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.soap.SoapUtils;
import com.zextras.mailbox.util.SoapClient.Request;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.admin.message.GetAccountRequest;
import com.zimbra.soap.admin.message.GetCosRequest;
import com.zimbra.soap.admin.message.GetCosResponse;
import com.zimbra.soap.admin.message.GetDomainRequest;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.admin.type.CosSelector.CosBy;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.DomainSelector.DomainBy;
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
	void globalAdmin_IsAbleToRetrieveCOS() throws Exception {
		final Account account = createAccount().asGlobalAdmin().create();

		final SoapResponse response = getCOSAdminApi(account);
		assertAllowed(response);
	}

	@Test
	void delegatedAdmin_IsNotAbleToRetrieveCOS_IFHasNoAccessToCOS() throws Exception {
		final Account account = createAccount().create();
		account.setIsDelegatedAdminAccount(true);

		final SoapResponse response = getCOSAdminApi(account);
		assertDenied(response);
	}

	@Test
	void domainAdmin_IsNotAbleToRetrieveCOS_IfCOSInDifferentDomain() throws Exception {
		final Account account = createAccount().create();
		account.setIsDomainAdminAccount(true);

		final SoapResponse cosAdminApi = getCOSAdminApi(account);
		assertDenied(cosAdminApi);
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
		final Request request = getSoapClient().newAdminRequest().setCaller(account)
				.setSoapBody(getAccountRequest);
		return request.call();
	}

}
