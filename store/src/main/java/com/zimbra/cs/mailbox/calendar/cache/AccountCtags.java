// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.calendar.cache;

import com.zimbra.cs.mailbox.Mailbox;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// for CalDAV
// AccountCtags is a convenience class that aggregates all ctag-related info for an account.
// It combines data from CalListCache and CtagInfoCache.
public class AccountCtags {
  private CalList mCalList;
  private Map<Integer /* cal folder id */, CtagInfo> mCtagMap;

  AccountCtags(CalList calList, Collection<CtagInfo> ctags) {
    mCalList = calList;
    mCtagMap = new HashMap<Integer, CtagInfo>(ctags.size());
    for (CtagInfo ctag : ctags) {
      mCtagMap.put(ctag.getId(), ctag);
    }
  }

  /**
   * Returns a version tag that changes on any calendar-related update in the account.
   *
   * @return
   */
  public String getVersion() {
    return mCalList.getVersion();
  }

  /**
   * Returns the ctag for calendar folder with given id.
   *
   * @param calFolderId
   * @return
   */
  public CtagInfo getById(int calFolderId) {
    return mCtagMap.get(calFolderId);
  }

  /**
   * Returns the ctag for calendar folder with given path.
   *
   * @param path
   * @return
   */
  public CtagInfo getByPath(String path) {
    for (CtagInfo calInfo : mCtagMap.values()) {
      if (path.equalsIgnoreCase(calInfo.getPath())) return calInfo;
    }
    return null;
  }

  /**
   * Returns ctags for calendar folder under the given parent folder.
   *
   * @param parentFolderId
   * @return
   */
  public Collection<CtagInfo> getChildren(int parentFolderId) {
    Collection<CtagInfo> allFolders = mCtagMap.values();
    if (parentFolderId == Mailbox.ID_FOLDER_USER_ROOT) return allFolders;
    List<CtagInfo> children = new ArrayList<CtagInfo>();
    for (CtagInfo calInfo : allFolders) {
      if (calInfo.getFolderId() == parentFolderId) children.add(calInfo);
    }
    return children;
  }
}
