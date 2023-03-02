package com.zimbra.soap;

import com.zimbra.cs.service.account.AccountService;
import com.zimbra.cs.service.admin.AdminService;
import com.zimbra.cs.service.mail.MailService;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Soap Servlet for admin that handles mail, account and admin functionalities.
 *
 * @since 23.4.0
 * @author davidefrison
 */
@Singleton
public class AdminSoapServlet extends SoapServlet {

  @Inject
  public AdminSoapServlet(
      AdminService adminService, MailService mailService, AccountService accountService) {
    super(adminService, mailService, accountService);
  }
}
