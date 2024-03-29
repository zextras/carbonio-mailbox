// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.calendar.cache;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.util.memcached.BigByteArrayMemcachedMap;
import com.zimbra.common.util.memcached.ByteArraySerializer;
import com.zimbra.common.util.memcached.ZimbraMemcachedClient;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Metadata;
import com.zimbra.cs.memcached.MemcachedConnector;
import com.zimbra.cs.session.PendingLocalModifications;
import com.zimbra.cs.session.PendingModifications.Change;
import com.zimbra.cs.session.PendingModifications.ModificationKey;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalSummaryMemcachedCache {

  private BigByteArrayMemcachedMap<CalSummaryKey, CalendarData> mMemcachedLookup;

  CalSummaryMemcachedCache() {
    ZimbraMemcachedClient memcachedClient = MemcachedConnector.getClient();
    CalSummarySerializer serializer = new CalSummarySerializer();
    mMemcachedLookup =
        new BigByteArrayMemcachedMap<>(memcachedClient, serializer);
  }

  private static class CalSummarySerializer implements ByteArraySerializer<CalendarData> {
    CalSummarySerializer() {}

    @Override
    public byte[] serialize(CalendarData value) {
      return value.encodeMetadata().toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public CalendarData deserialize(byte[] bytes) throws ServiceException {
      if (bytes != null) {
        String encoded;
        encoded = new String(bytes, StandardCharsets.UTF_8);
        Metadata meta = new Metadata(encoded);
        return new CalendarData(meta);
      } else {
        return null;
      }
    }
  }

  public CalendarData getForRange(CalSummaryKey key, long rangeStart, long rangeEnd)
      throws ServiceException {
    CalendarData calData = mMemcachedLookup.get(key);
    if (calData != null
        && rangeStart >= calData.getRangeStart()
        && rangeEnd <= calData.getRangeEnd()) return calData.getSubRange(rangeStart, rangeEnd);
    else return null;
  }

  public void put(CalSummaryKey key, CalendarData calData) throws ServiceException {
    mMemcachedLookup.put(key, calData);
  }

  void purgeMailbox(Mailbox mbox) throws ServiceException {
    String accountId = mbox.getAccountId();
    List<Folder> folders = mbox.getCalendarFolders(null, SortBy.NONE);
    List<CalSummaryKey> keys = new ArrayList<>(folders.size());
    for (Folder folder : folders) {
      CalSummaryKey key = new CalSummaryKey(accountId, folder.getId());
      keys.add(key);
    }
    mMemcachedLookup.removeMulti(keys);
  }

  void notifyCommittedChanges(PendingLocalModifications mods, int changeId) {
    Set<CalSummaryKey> keysToInvalidate = new HashSet<>();
    if (mods.modified != null) {
      for (Map.Entry<ModificationKey, Change> entry : mods.modified.entrySet()) {
        Change change = entry.getValue();
        Object whatChanged = change.what;
        if (whatChanged instanceof Folder) {
          Folder folder = (Folder) whatChanged;
          MailItem.Type viewType = folder.getDefaultView();
          if (viewType == MailItem.Type.APPOINTMENT) {
            CalSummaryKey key =
                new CalSummaryKey(folder.getMailbox().getAccountId(), folder.getId());
            keysToInvalidate.add(key);
          }
        }
      }
    }
    if (mods.deleted != null) {
      for (Map.Entry<ModificationKey, Change> entry : mods.deleted.entrySet()) {
        MailItem.Type type = (MailItem.Type) entry.getValue().what;
        if (type == MailItem.Type.FOLDER) {
          // We only have item id.  Assume it's a folder id and issue a delete.
          String acctId = entry.getKey().getAccountId();
          if (acctId == null) continue; // just to be safe
          CalSummaryKey key = new CalSummaryKey(acctId, entry.getKey().getItemId());
          keysToInvalidate.add(key);
        }
        // Let's not worry about hard deletes of invite/reply emails.  It has no practical benefit.
      }
    }
    try {
      mMemcachedLookup.removeMulti(keysToInvalidate);
    } catch (ServiceException e) {
      ZimbraLog.calendar.warn(
          "Unable to notify ctag info cache.  Some cached data may become stale.", e);
    }
  }
}
