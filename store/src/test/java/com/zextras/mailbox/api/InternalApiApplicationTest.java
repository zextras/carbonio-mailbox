/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.junit.jupiter.api.Test;

class InternalApiApplicationTest {

	@Test
	void shouldRegisterOpenApiResource() {
		final InternalApiApplication app = new InternalApiApplication();

		assertTrue(app.isRegistered(OpenApiResource.class),
				"OpenApiResource should be registered");
	}

	@Test
	void shouldHaveRegisteredClasses() {
		final InternalApiApplication app = new InternalApiApplication();

		assertFalse(app.getClasses().isEmpty() && app.getSingletons().isEmpty(),
				"Application should have registered classes or singletons");
	}
}
