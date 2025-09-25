// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Maps;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetDomainEncodingTest extends MailboxTestSuite {

	private static Account acct;

	@BeforeEach
	public void setUp() throws Exception {
		Provisioning prov = Provisioning.getInstance();
		Map<String, Object> attrs = Maps.newHashMap();
		String[] values = new String[2];
		values[0] = "ldap://ldap1.com";
		values[1] = "ldap://ldap2.com";
		attrs.put("zimbraAuthLdapURL", values);
		final Domain domain = prov.createDomain(UUID.randomUUID() + ".com", attrs);
		acct = createAccount().withDomain(domain.getName()).create();
	}

	@Test
	void testZBUG201() throws Exception {
		Domain domain = Provisioning.getInstance().getDomain(acct);
		Map<String, Object> context = ServiceTestUtil.getRequestContext(acct);
		ZimbraSoapContext zsc = (ZimbraSoapContext) context.get(SoapEngine.ZIMBRA_CONTEXT);
		Element response = zsc.createElement(AdminConstants.GET_DOMAIN_RESPONSE);
		GetDomain.encodeDomain(response, domain, true, null, null);
		// check that the response contains single space separated value for zimbraAuthLdapURL
		assertTrue(response.prettyPrint()
				.contains("<a n=\"zimbraAuthLdapURL\">ldap://ldap1.com ldap://ldap2.com</a>"));
	}
}