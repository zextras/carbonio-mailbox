// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap;

import com.zimbra.cs.service.admin.AdminService;
import com.zimbra.cs.service.mail.MailService;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class AdminSoapServlet extends SoapServlet {

  private static final long serialVersionUID = 5242741895888594479L;

  @Inject
  public AdminSoapServlet(
      MailService mailService,
      AdminService adminService,
      @Named("adminSOAPPorts") List<Integer> allowedPorts) {
    super(List.of(mailService, adminService), allowedPorts);
  }
}
