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
import com.zimbra.soap.admin.message.GetCosRequest;
import com.zimbra.soap.admin.message.GetCosResponse;
import com.zimbra.soap.admin.message.GetDomainRequest;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.admin.type.CosSelector.CosBy;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.DomainSelector.DomainBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AdminGroupTest extends SoapTestSuite {

	private static Provisioning provisioning;

	@BeforeAll
	static void setUp() throws Exception {
		provisioning = Provisioning.getInstance();
	}

	@Test
	void delegatedAdmin_IsAbleToRetrieveCOS() throws Exception {
		final Cos cos = provisioning.createCos("COS1", Maps.newHashMap());
		final Account account = createAccount().create();
		account.setIsDelegatedAdminAccount(true);

		final SoapResponse response = getCOSAdminApi(cos, account);
		final String soapResponse = response.body();
		final GetCosResponse cosResponse = SoapUtils.getSoapResponse(soapResponse,
				AdminConstants.E_GET_COS_RESPONSE, GetCosResponse.class);
		Assertions.assertEquals(cosResponse.getCos().getId(), cos.getId());

	}

	@Test
	void domainAdmin_IsNotAbleToRetrieveCOS() throws Exception {
		final Cos cos = provisioning.createCos("COS2", Maps.newHashMap());

		final Account account = createAccount().create();
		account.setIsDomainAdminAccount(true);

		final SoapResponse cosAdminApi = getCOSAdminApi(cos, account);
		Assertions.assertTrue(cosAdminApi.body().contains("permission denied: need adequate admin"));

	}

	private SoapResponse getCOSAdminApi(Cos cos, Account account) throws Exception {
		final GetCosRequest getCosRequest = new GetCosRequest(new CosSelector(CosBy.id, cos.getId()));
		final Request request = getSoapClient().newAdminRequest().setCaller(account)
				.setSoapBody(getCosRequest);
		return request.call();
	}

}
