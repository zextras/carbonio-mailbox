/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;

import java.util.function.Supplier;

public class MailboxService {

  private final Supplier<MailboxManager> mailboxManagerSupplier;
  private final Supplier<SoapProvisioning> soapProvisioningSupplier;

  public MailboxService(Supplier<MailboxManager> mailboxManagerSupplier,
                        Supplier<SoapProvisioning> soapProvisioningSupplier) {
    this.mailboxManagerSupplier = mailboxManagerSupplier;
    this.soapProvisioningSupplier = soapProvisioningSupplier;
  }

  public long getMailboxUsage(Account account) throws ServiceException {
    if (account.isOnLocalServer()) {
      return getLocalMailbox(account).getSize();
    } else {
      return getRemoteMailbox(account).getUsed();
    }
  }

  private Mailbox getLocalMailbox(Account account) throws ServiceException {
    return mailboxManagerSupplier.get().getMailboxByAccount(account);
  }

  private SoapProvisioning.MailboxInfo getRemoteMailbox(Account account) throws ServiceException {
    return soapProvisioningSupplier.get().getMailbox(account);
  }
}
