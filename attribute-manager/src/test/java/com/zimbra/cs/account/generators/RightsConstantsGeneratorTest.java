package com.zimbra.cs.account.generators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RightsConstantsGeneratorTest {

	private static RightsConstantsGenerator generator;

	@BeforeAll
	static void setUp() throws Exception {
		generator = RightsConstantsGenerator.getInstance();
	}

	@Test
	void shouldGenerateRightsConstantJavaClass() {
		final String rightConstsJava = generator.genRightConstsJava();
		Assertions.assertTrue(rightConstsJava.contains("public class RightConsts {"));
	}

	@Test
	void shouldGenerateUserRightsJavaClass() {
		final String rightConstsJava = generator.genUserRights();
		Assertions.assertTrue(rightConstsJava.contains("public class UserRights {"));
	}

	@Test
	void shouldGenerateAdminRightsJavaClass() {
		final String rightConstsJava = generator.genAdminRightsJava();
		Assertions.assertTrue(rightConstsJava.contains("public class AdminRights {"));
	}

}