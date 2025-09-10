package com.zimbra.cs.account;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LdapAttributeInfoTest {

	@Test
	void getCallback_shouldReturnSameInstance_whenCalledTwice() {
		var attributeInfo = Mockito.mock(AttributeInfo.class);
		Mockito.when(attributeInfo.getCallbackClassName()).thenReturn("com.zimbra.cs.account.DataSourceCallback");
		final LdapAttributeInfo ldapAttributeInfo = LdapAttributeInfo.get(attributeInfo);
		final AttributeCallback callback = ldapAttributeInfo.getCallback();
		final AttributeCallback callback1 = ldapAttributeInfo.getCallback();
		Assertions.assertSame(callback, callback1);
	}
}