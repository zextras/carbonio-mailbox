// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.AbstractModule;
import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.mailbox.service.MailboxManagerFactory;
import com.zextras.mailbox.service.OperationContextFactory;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.cs.service.LocalMailboxAttachmentService;

public class PreviewServletModule extends AbstractModule {

  @Override
  protected void configure() {
    super.configure();
    bind(PreviewClient.class).toInstance(PreviewClient.atURL("http", "127.78.0.7", 20001));
    bind(AttachmentService.class)
        .toInstance(
            new LocalMailboxAttachmentService(
                new OperationContextFactory(), new MailboxManagerFactory()));
  }
}
