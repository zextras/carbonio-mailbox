// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap;

import com.zimbra.cs.service.account.AccountService;
import com.zimbra.cs.service.mail.MailService;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class UserSoapServlet extends SoapServlet {

  private static final long serialVersionUID = 7679256696900215308L;

  @Inject
  public UserSoapServlet(
      MailService mailService,
      AccountService accountService,
      @Named("userSOAPPorts") List<Integer> allowedPorts) {
    super(List.of(mailService, accountService), allowedPorts);
  }
}
