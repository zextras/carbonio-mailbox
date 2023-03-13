// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.stats;

import com.zimbra.common.stats.RealtimeStatsCallback;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.mailbox.MessageCache;
import com.zimbra.cs.store.BlobInputStream;
import com.zimbra.cs.store.FileDescriptorCache;
import java.util.HashMap;
import java.util.Map;

public class ServerStatsCallback implements RealtimeStatsCallback {

  public Map<String, Integer> getStatData() {
    Map<String, Integer> data = new HashMap<>();
    data.put(ZimbraPerf.RTS_MBOX_CACHE_SIZE, ZimbraPerf.getMailboxCacheSize());
    data.put(ZimbraPerf.RTS_MSG_CACHE_SIZE, MessageCache.getSize());

    FileDescriptorCache fdc = BlobInputStream.getFileDescriptorCache();
    data.put(ZimbraPerf.RTS_FD_CACHE_SIZE, fdc.getSize());

    Provisioning prov = Provisioning.getInstance();
    if (prov instanceof LdapProv) {
      LdapProv ldap = (LdapProv) prov;
      data.put(ZimbraPerf.RTS_ACCOUNT_CACHE_SIZE, ldap.getAccountCacheSize());
      data.put(ZimbraPerf.RTS_COS_CACHE_SIZE, ldap.getCosCacheSize());
      data.put(ZimbraPerf.RTS_DOMAIN_CACHE_SIZE, ldap.getDomainCacheSize());
      data.put(ZimbraPerf.RTS_SERVER_CACHE_SIZE, ldap.getServerCacheSize());
      data.put(ZimbraPerf.RTS_UCSERVICE_CACHE_SIZE, ldap.getUCServiceCacheSize());
      data.put(ZimbraPerf.RTS_ZIMLET_CACHE_SIZE, ldap.getZimletCacheSize());
      data.put(ZimbraPerf.RTS_GROUP_CACHE_SIZE, ldap.getGroupCacheSize());
      data.put(ZimbraPerf.RTS_XMPP_CACHE_SIZE, ldap.getXMPPCacheSize());
    }
    return data;
  }
}
