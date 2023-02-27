package com.zextras.guice;

import com.google.inject.AbstractModule;
import com.zextras.carbonio.files.FilesClient;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.cs.service.MailboxAttachmentService;

public class MailboxModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(AttachmentService.class).toInstance(new MailboxAttachmentService());
    bind(FilesClient.class).toInstance(FilesClient.atURL("http://127.78.0.7:20002"));
  }
}
