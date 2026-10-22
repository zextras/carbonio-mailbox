/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import com.zextras.mailbox.api.rest.service.AccountService;
import com.zextras.mailbox.api.rest.service.CosService;
import com.zextras.mailbox.api.rest.service.MailboxService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.MailboxManager;

public final class InternalApiServices {

  private InternalApiServices() {}

  public static MailboxService mailboxService() {
    return new MailboxService(
        Provisioning::getInstance,
        () -> {
          try {
            return MailboxManager.getInstance();
          } catch (ServiceException e) {
            throw new InternalApiInitializationException(e);
          }
        },
        () -> {
          try {
            return SoapProvisioning.getAdminInstance();
          } catch (ServiceException e) {
            throw new InternalApiInitializationException(e);
          }
        });
  }

  public static AccountService accountService(MailboxService mailboxService) {
    return new AccountService(Provisioning::getInstance, mailboxService);
  }

  public static CosService cosService() {
    return new CosService(Provisioning::getInstance);
  }
}
