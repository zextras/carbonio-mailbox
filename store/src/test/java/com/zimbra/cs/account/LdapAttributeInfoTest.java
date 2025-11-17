package com.zimbra.cs.account;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LdapAttributeInfoTest extends MailboxTestSuite {

	@Test
	void getCallback_shouldReturnSameInstance_whenCalledTwice() throws ServiceException {
		final AttributeManager attributeManager = StoreAttributeManager.getInstance();
		final AttributeInfo accountStatusAttribute = attributeManager.getAttributeInfo(ZAttrProvisioning.A_zimbraAccountStatus);
		final AttributeCallback callback = LdapAttributeInfo.get(accountStatusAttribute).getCallback();
		final AttributeCallback callback1 = LdapAttributeInfo.get(accountStatusAttribute).getCallback();
		Assertions.assertSame(callback, callback1);
	}

	@Test
	void verify_accountStatusCallbackIsLoaded() throws ServiceException {
		final AttributeManager attributeManager = StoreAttributeManager.getInstance();
		final AttributeInfo accountStatusAttribute = attributeManager.getAttributeInfo(ZAttrProvisioning.A_zimbraAccountStatus);
		final AttributeCallback callback = LdapAttributeInfo.get(accountStatusAttribute).getCallback();
		Assertions.assertEquals("com.zimbra.cs.account.callback.AccountStatus", callback.getClass().getName());
	}

	@Test
	void shouldNotLoadCallbackIfAttributeDoesNotHaveOne() throws ServiceException {
		final AttributeManager attributeManager = StoreAttributeManager.getInstance();
		final AttributeInfo accountStatusAttribute = attributeManager.getAttributeInfo(ZAttrProvisioning.A_zimbraMailTransport);
		final AttributeCallback callback = LdapAttributeInfo.get(accountStatusAttribute).getCallback();
		Assertions.assertNull(callback);
	}
}