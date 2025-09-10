package com.zimbra.cs.account;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LdapAttributeInfoTest  extends MailboxTestSuite {

	@Test
	void getCallback_shouldReturnSameInstance_whenCalledTwice() {
		var attributeInfo = Mockito.mock(AttributeInfo.class);
		Mockito.when(attributeInfo.getCallbackClassName()).thenReturn("com.zimbra.cs.account.DataSourceCallback");

		final LdapAttributeInfo ldapAttributeInfo = LdapAttributeInfo.get(attributeInfo);
		final AttributeCallback callback = ldapAttributeInfo.getCallback();
		final AttributeCallback callback1 = ldapAttributeInfo.getCallback();
		Assertions.assertSame(callback, callback1);
	}

	@Test
	void verify_accountStatusCallbackIsLoaded() throws ServiceException {
		final AttributeManager attributeManager = AttributeManager.getInstance();
		final AttributeInfo accountStatusAttribute = attributeManager.getAttributeInfo(ZAttrProvisioning.A_zimbraAccountStatus);
		final AttributeCallback callback = LdapAttributeInfo.get(accountStatusAttribute).getCallback();
		Assertions.assertEquals("com.zimbra.cs.account.callback.AccountStatus", callback.getClass().getName());
	}
}