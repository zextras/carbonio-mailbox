// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.zextras.carbonio.preview.PreviewClient;
import com.zextras.mailbox.client.MailboxHttpClient;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.cs.service.MailboxHttpAttachmentService;

public class PreviewServletModule extends AbstractModule {

  @Provides
  public PreviewClient providePreviewClient() {
    return PreviewClient.atURL("http", "127.78.0.7", 20001);
  }

  @Provides
  public Provisioning provideProvisioning() {
    return Provisioning.getInstance();
  }

  @Provides
  public MailboxHttpClient provideMailboxHttpClient(Provisioning provisioning) {
    return new MailboxHttpClient(provisioning);
  }

  @Provides
  public AttachmentService provideHttpAttachmentService(MailboxHttpClient client) {
    return new MailboxHttpAttachmentService(client);
  }
}
