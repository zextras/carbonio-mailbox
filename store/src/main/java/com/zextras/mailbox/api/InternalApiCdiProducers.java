/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import com.zextras.mailbox.api.rest.service.AccountService;
import com.zextras.mailbox.api.rest.service.MailboxService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.MailboxManager;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

/** CDI producers for the internal API service layer. */
@ApplicationScoped
public class InternalApiCdiProducers {

  @Produces
  @Singleton
  public MailboxService mailboxService() {
    return new MailboxService(
        Provisioning::getInstance,
        () -> {
          try {
            return MailboxManager.getInstance();
          } catch (ServiceException e) {
            throw new RuntimeException(e);
          }
        },
        () -> {
          try {
            return SoapProvisioning.getAdminInstance();
          } catch (ServiceException e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Produces
  @Singleton
  public AccountService accountService(MailboxService mailboxService) {
    return new AccountService(Provisioning::getInstance, mailboxService);
  }
}
