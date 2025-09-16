package com.zimbra.cs.account.generators;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RightDomainAdminResourceGeneratorTest {

	@Test
	void shouldGenerateRightDomainAdminResource() throws Exception {
		RightDomainAdminResourceGenerator generator = new RightDomainAdminResourceGenerator();
		final String output = generator.genDomainAdminSetAttrsRights();
		Assertions.assertNotNull(output);
	}

}