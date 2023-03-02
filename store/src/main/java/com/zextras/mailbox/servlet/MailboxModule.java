package com.zextras.mailbox.servlet;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;
import com.zextras.carbonio.files.FilesClient;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.cs.service.MailboxAttachmentService;
import com.zimbra.soap.AdminSoapServlet;
import com.zimbra.soap.UserSoapServlet;

/**
 * Mailbox Servlets configurations
 *
 * @since 23.4.0
 * @author davidefrison
 */
public class MailboxModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/soap").with(UserSoapServlet.class);
    serve("/admin/soap").with(AdminSoapServlet.class);
  }

  @Provides
  private AttachmentService provideAttachmentService() {
    return new MailboxAttachmentService();
  }

  @Provides
  private FilesClient provideFilesClient() {
    return FilesClient.atURL("http://127.78.0.7:20002");
  }
}
