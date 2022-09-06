// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource.imap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ImapMessageCollection implements Iterable<ImapMessage> {
  private Map<Integer, ImapMessage> mByItemId = new HashMap<Integer, ImapMessage>();
  private Map<Long, ImapMessage> mByUid = new HashMap<Long, ImapMessage>();
  // Tracked message with a UID of 0 did not return a UID when appended to a
  // remote folder. In this case, we will try to fill in the correct uid
  // when we fetch the message later (see bug 26347).
  private Map<Integer, ImapMessage> mNoUid = new HashMap<Integer, ImapMessage>();

  public void add(ImapMessage msg) {
    mByItemId.put(msg.getItemId(), msg);
    long uid = msg.getUid();
    if (uid > 0) {
      mByUid.put(msg.getUid(), msg);
    } else {
      mNoUid.put(msg.getItemId(), msg);
    }
  }

  public ImapMessage getByItemId(int itemId) {
    return mByItemId.get(itemId);
  }

  public ImapMessage getByUid(long uid) {
    return mByUid.get(uid);
  }

  public Collection<ImapMessage> getNoUid() {
    return mNoUid.values();
  }

  public boolean containsItemId(int itemId) {
    return mByItemId.containsKey(itemId);
  }

  public boolean containsUid(long uid) {
    return mByUid.containsKey(uid);
  }

  public int size() {
    return mByItemId.size();
  }

  public Iterator<ImapMessage> iterator() {
    return mByItemId.values().iterator();
  }

  public Set<Long> getUids() {
    return mByUid.keySet();
  }

  public Set<Integer> getItemIds() {
    return mByItemId.keySet();
  }

  public long getLastUid() {
    long maxUid = 0;
    for (long uid : mByUid.keySet()) {
      if (uid > maxUid) maxUid = uid;
    }
    return maxUid;
  }
}
