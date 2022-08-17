// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import java.util.HashMap;
import java.util.Map;

public class AlwaysOnCluster extends ZAttrAlwaysOnCluster {

  private final Map<String, Object> serverOverrides = new HashMap<>();

  public AlwaysOnCluster(
      String name,
      String id,
      Map<String, Object> attrs,
      Map<String, Object> defaults,
      Provisioning prov) {
    super(name, id, attrs, defaults, prov);
    try {
      getDefaults(AttributeFlag.serverPreferAlwaysOn, serverOverrides);
    } catch (ServiceException se) {
      ZimbraLog.account.warn("error while calculating server overrides", se);
    }
  }

  @Override
  public EntryType getEntryType() {
    return EntryType.ALWAYSONCLUSTER;
  }

  @Override
  public synchronized void resetData() {
    super.resetData();
    try {
      getDefaults(AttributeFlag.serverPreferAlwaysOn, serverOverrides);
    } catch (ServiceException e) {
      ZimbraLog.account.warn("error while calculating server overrides", e);
    }
  }

  public Map<String, Object> getServerOverrides() {
    return serverOverrides;
  }
}
