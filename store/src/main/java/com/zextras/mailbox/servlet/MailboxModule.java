package com.zextras.mailbox.servlet;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;
import com.zextras.carbonio.files.FilesClient;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.service.AttachmentService;
import com.zimbra.cs.service.MailboxAttachmentService;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.soap.AdminSoapServlet;
import com.zimbra.soap.UserSoapServlet;
import javax.inject.Singleton;

/**
 * Mailbox Servlets configurations
 *
 * @since 23.4.0
 * @author davidefrison
 */
public class MailboxModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/soap/*").with(UserSoapServlet.class);
    serve("/admin/soap/*").with(AdminSoapServlet.class);
  }

  @Provides
  private AttachmentService provideAttachmentService() {
    return new MailboxAttachmentService();
  }

  @Provides
  private FilesClient provideFilesClient() {
    return FilesClient.atURL("http://127.78.0.7:20002");
  }

  /**
   * Provides {@link RightManager} as it was done by {@link
   * com.zimbra.cs.util.Zimbra#startup(boolean)}
   *
   * @return
   * @throws ServiceException
   */
  @Provides
  @Singleton
  private RightManager provideRightManager() throws ServiceException {
    return RightManager.getInstance();
  }

  @Provides
  @Singleton
  private StoreManager provideFileBlobStoreManager() throws Exception {
    final StoreManager storeManager = StoreManager.getInstance();
    storeManager.startup();
    return storeManager;
  }
}
