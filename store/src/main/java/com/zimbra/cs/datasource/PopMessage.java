// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.db.DbDataSource;
import com.zimbra.cs.db.DbDataSource.DataSourceItem;
import com.zimbra.cs.db.DbPop3Message;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PopMessage extends DataSourceMapping {
  public PopMessage(DataSource ds, DataSourceItem dsi) throws ServiceException {
    super(ds, dsi);
  }

  public PopMessage(DataSource ds, int itemId) throws ServiceException {
    super(ds, itemId);
  }

  public PopMessage(DataSource ds, String uid) throws ServiceException {
    super(ds, uid);
  }

  public PopMessage(DataSource ds, int itemId, String uid) throws ServiceException {
    super(ds, ds.getFolderId(), itemId, uid);
  }

  public static Set<PopMessage> getMappings(DataSource ds, String[] remoteIds)
      throws ServiceException {
    Collection<DataSourceItem> mappings =
        DbDataSource.getReverseMappings(ds, Arrays.asList(remoteIds));
    Set<PopMessage> matchingMsgs = new HashSet<PopMessage>();

    if (mappings.isEmpty()) {
      Map<Integer, String> oldMappings =
          DbPop3Message.getMappings(DataSourceManager.getInstance().getMailbox(ds), ds.getId());

      for (Integer itemId : oldMappings.keySet()) {
        String uid = oldMappings.get(itemId);
        PopMessage mapping = new PopMessage(ds, itemId, uid);

        mapping.add();
        for (String remoteId : remoteIds) {
          if (remoteId.equals(uid)) matchingMsgs.add(mapping);
        }
      }
      if (!oldMappings.isEmpty())
        DbPop3Message.deleteUids(DataSourceManager.getInstance().getMailbox(ds), ds.getName());
    } else {
      for (DataSourceItem mapping : mappings) matchingMsgs.add(new PopMessage(ds, mapping));
    }
    return matchingMsgs;
  }

  public static Set<String> getMatchingUids(DataSource ds, String[] remoteIds)
      throws ServiceException {
    Set<PopMessage> matchingMsgs = getMappings(ds, remoteIds);
    Set<String> matchingUids = new HashSet<String>(matchingMsgs.size());

    for (PopMessage msg : matchingMsgs) matchingUids.add(msg.getRemoteId());
    return matchingUids;
  }
}
