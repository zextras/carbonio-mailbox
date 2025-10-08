package com.zimbra.cs.service.account;

import com.google.common.collect.Maps;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.soap.SoapUtils;
import com.zextras.mailbox.util.SoapClient.Request;
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
	void delegatedAdminIsAbleToSeeCOS() throws Exception {
		final Cos cos = provisioning.createCos("COS1", Maps.newHashMap());
		final Account account = createAccount().create();
		account.setIsDelegatedAdminAccount(true);

		final GetCosResponse getCosByAdminApi = getCOSAdminApi(cos, account);
		Assertions.assertEquals(getCosByAdminApi.getCos().getId(), cos.getId());

	}

	private GetCosResponse getCOSAdminApi(Cos cos, Account account) throws Exception {
		final GetCosRequest getCosRequest = new GetCosRequest(new CosSelector(CosBy.id, cos.getId()));
		final Request request = getSoapClient().newAdminRequest().setCaller(account)
				.setSoapBody(getCosRequest);
		final String soapResponse = request.call().body();
		return SoapUtils.getSoapResponse(soapResponse, AdminConstants.E_GET_COS_RESPONSE, GetCosResponse.class);
	}

}
