/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ShareInfo;
import com.zimbra.cs.account.ShareInfoData;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.OperationContext;

import java.util.ArrayList;
import java.util.List;
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

  public List<ShareInfoData> getShareInfo(Account account) throws ServiceException {
    if (account.isOnLocalServer()) {
      return getLocalShareInfo(account);
    } else {
      return getRemoteShareInfo(account);
    }
  }

  private List<ShareInfoData> getLocalShareInfo(Account account) throws ServiceException {
    final Mailbox mailbox = getLocalMailbox(account);
    final var octxt = new OperationContext(account);
    final List<ShareInfoData> result = new ArrayList<>();
    for (MailItem item : mailbox.getItemList(octxt, MailItem.Type.MOUNTPOINT, -1, SortBy.NONE)) {
      Mountpoint mp = (Mountpoint) item;
      ShareInfoData sid = new ShareInfoData();
      sid.setOwnerAcctId(mp.getOwnerId());
      sid.setItemId(mp.getRemoteId());
      sid.setItemUuid(mp.getRemoteUuid());
      sid.setPath(mp.getPath());
      result.add(sid);
    }
    return result;
  }

  private List<ShareInfoData> getRemoteShareInfo(Account account) throws ServiceException {
    final List<ShareInfoData> shares = new ArrayList<>();
    soapProvisioningSupplier.get().getShareInfo(account, shares::add);
    return shares;
  }

  private Mailbox getLocalMailbox(Account account) throws ServiceException {
    return mailboxManagerSupplier.get().getMailboxByAccount(account);
  }

  private SoapProvisioning.MailboxInfo getRemoteMailbox(Account account) throws ServiceException {
    return soapProvisioningSupplier.get().getMailbox(account);
  }
}
