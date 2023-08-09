// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.zextras.mailbox.resource.acceptance.MailboxJerseyTest;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.test.DeploymentContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MailboxResourceConfigTest extends MailboxJerseyTest {

  @Override
  protected DeploymentContext configureDeployment() {
    return DeploymentContext.builder(MailboxResourceConfig.class).build();
  }

  @Test
  void shouldInitializePreviewController() {
    final Response response = target("/pdf/1/2").request().get();
    Assertions.assertEquals(500, response.getStatus());
  }
}
