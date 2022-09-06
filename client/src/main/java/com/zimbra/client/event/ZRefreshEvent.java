// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.client.ToZJSONObject;
import com.zimbra.client.ZFolder;
import com.zimbra.client.ZJSONObject;
import com.zimbra.client.ZTag;
import java.util.List;
import org.json.JSONException;

public class ZRefreshEvent implements ToZJSONObject {

  private long mSize;
  private ZFolder mUserRoot;
  private List<ZTag> mTags;

  public ZRefreshEvent(long size, ZFolder userRoot, List<ZTag> tags) {
    mSize = size;
    mUserRoot = userRoot;
    mTags = tags;
  }

  /**
   * @return size of mailbox in bytes
   */
  public long getSize() {
    return mSize;
  }

  /**
   * return the root user folder
   *
   * @return user root folder
   */
  public ZFolder getUserRoot() {
    return mUserRoot;
  }

  public List<ZTag> getTags() {
    return mTags;
  }

  public ZJSONObject toZJSONObject() throws JSONException {
    ZJSONObject zjo = new ZJSONObject();
    zjo.put("size", getSize());
    zjo.put("userRoot", mUserRoot);
    zjo.put("tags", mTags);
    return zjo;
  }

  public String toString() {
    return "[ZRefreshEvent]";
  }

  public String dump() {
    return ZJSONObject.toString(this);
  }
}
