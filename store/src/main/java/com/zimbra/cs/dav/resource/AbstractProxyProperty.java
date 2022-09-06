// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.resource;

import com.zimbra.client.ZFolder;
import com.zimbra.client.ZMailbox;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.property.ResourceProperty;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.service.AuthProvider;
import java.util.ArrayList;
import org.dom4j.QName;

public abstract class AbstractProxyProperty extends ResourceProperty {
  protected final Account account;

  public AbstractProxyProperty(QName name, Account account) {
    super(name);
    this.account = account;
  }

  protected ArrayList<Pair<Mountpoint, ZFolder>> getMountpoints(DavContext ctxt) {
    ArrayList<Pair<Mountpoint, ZFolder>> mps = new ArrayList<Pair<Mountpoint, ZFolder>>();
    try {
      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
      for (MailItem item : mbox.getItemList(ctxt.getOperationContext(), MailItem.Type.MOUNTPOINT)) {
        Mountpoint mp = (Mountpoint) item;
        // skip non-calendar mountpoints
        if (mp.getDefaultView() != MailItem.Type.APPOINTMENT
            && mp.getDefaultView() != MailItem.Type.TASK) {
          continue;
        }
        ZAuthToken zat = AuthProvider.getAuthToken(ctxt.getAuthAccount()).toZAuthToken();
        ZMailbox zmbx = RemoteCollection.getRemoteMailbox(zat, mp.getOwnerId());
        // skip dangling mountpoints
        if (zmbx == null) {
          continue;
        }
        try {
          ZFolder folder = zmbx.getFolderById(mp.getTarget().toString(account));
          // skip dangling mountpoints
          if (folder == null) {
            continue;
          }
          mps.add(new Pair<Mountpoint, ZFolder>(mp, folder));
        } catch (ServiceException se) {
          ZimbraLog.dav.warn("can't get remote folder", se);
        }
      }
    } catch (ServiceException se) {
      ZimbraLog.dav.warn("can't get mailbox", se);
    }
    return mps;
  }
}
