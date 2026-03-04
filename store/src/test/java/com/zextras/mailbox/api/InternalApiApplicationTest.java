/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.api.rest.PingResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.junit.jupiter.api.Test;

class InternalApiApplicationTest {

	@Test
	void shouldRegisterOpenApiResource() {
		assertTrue(new InternalApiApplication().getClasses().contains(OpenApiResource.class));
	}

	@Test
	void shouldRegisterPingResource() {
		assertTrue(new InternalApiApplication().getClasses().contains(PingResource.class));
	}
}
