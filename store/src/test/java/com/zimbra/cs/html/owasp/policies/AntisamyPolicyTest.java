// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp.policies;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.Policy;

/**
 * Checks the AntiSamy policies used by {@link StyleTagReceiver} to sanitize style elements.
 *
 * <p>AntiSamy looks up the "cssPseudoElementExclusion" regexp to validate pseudo-class
 * selectors. When the policy does not define it (it used to be misspelled
 * "cssPsuedoElementExclusion", the name AntiSamy itself used up to 1.7.6), scanning any
 * selector like "a:hover" throws a NullPointerException and the whole HTML body is dropped.
 */
class AntisamyPolicyTest {

	private static final String STYLE_WITH_PSEUDO_CLASS =
			"<style>a:hover { text-decoration: underline !important; }</style>";

	static Stream<Arguments> policies() throws Exception {
		URL productionPolicy = Path.of(System.getProperty("server.dir", "."), "conf", "antisamy.xml")
				.toUri().toURL();
		URL testPolicy = AntisamyPolicyTest.class.getResource("/antisamy.xml");
		return Stream.of(
				Arguments.of("store/conf/antisamy.xml", productionPolicy),
				Arguments.of("test resources antisamy.xml", testPolicy));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("policies")
	void shouldSanitizeStyleWithPseudoClassSelector(String name, URL policyUrl) throws Exception {
		Policy policy = Policy.getInstance(policyUrl);

		String cleanHtml = new AntiSamy()
				.scan(STYLE_WITH_PSEUDO_CLASS, policy, AntiSamy.DOM)
				.getCleanHTML();

		assertTrue(cleanHtml.contains("a:hover"), "pseudo-class rule should be kept: " + cleanHtml);
	}
}
