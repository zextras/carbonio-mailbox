// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import static org.mockito.Mockito.mock;

import com.zextras.mailbox.resource.acceptance.MailboxJerseyTest;
import com.zimbra.cs.account.Provisioning;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.test.DeploymentContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MailboxResourceConfigTest extends MailboxJerseyTest {

  @BeforeAll
  public static void setUpAll() {
    Provisioning.setInstance(mock(Provisioning.class));
  }

  @Override
  protected DeploymentContext configureDeployment() {
    return DeploymentContext.builder(PreviewResourceConfig.class).build();
  }

  @Test
  void shouldInitializePreviewController() {
    final Response response = target("/preview/document/1/2").request().get();
    Assertions.assertEquals(403, response.getStatus());
  }
}
