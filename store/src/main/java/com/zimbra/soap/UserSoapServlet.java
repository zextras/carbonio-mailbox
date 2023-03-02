package com.zimbra.soap;

import com.zimbra.cs.service.account.AccountService;
import com.zimbra.cs.service.mail.MailService;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserSoapServlet extends SoapServlet {

  @Inject
  public UserSoapServlet(MailService mailService, AccountService accountService) {
    super(mailService, accountService);
  }
}
