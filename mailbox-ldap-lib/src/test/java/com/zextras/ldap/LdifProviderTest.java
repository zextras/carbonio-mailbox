package com.zextras.ldap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LdifProviderTest {

	@Test
	void getConfigLdif() {
		Assertions.assertNotNull(new LdifProvider().getConfigLdif());
	}
}