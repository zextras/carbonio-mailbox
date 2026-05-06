/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ShareInfo;
import com.zimbra.cs.account.ShareInfoData;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;

import java.util.List;
import java.util.function.Supplier;

public class MailboxService {

  private final Supplier<Provisioning> provisioningSupplier;
  private final Supplier<MailboxManager> mailboxManagerSupplier;
  private final Supplier<SoapProvisioning> soapProvisioningSupplier;

  public MailboxService(Supplier<Provisioning> provisioningSupplier,
                        Supplier<MailboxManager> mailboxManagerSupplier,
                        Supplier<SoapProvisioning> soapProvisioningSupplier) {
    this.provisioningSupplier = provisioningSupplier;
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

  public List<ShareInfoData> getShareInfo(Account account) throws ServiceException {
    return ShareInfo.Published.getAllShares(provisioningSupplier.get(), account);
  }

  private Mailbox getLocalMailbox(Account account) throws ServiceException {
    return mailboxManagerSupplier.get().getMailboxByAccount(account);
  }

  private SoapProvisioning.MailboxInfo getRemoteMailbox(Account account) throws ServiceException {
    return soapProvisioningSupplier.get().getMailbox(account);
  }
}
