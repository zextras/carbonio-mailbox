// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.client.ZFolder;
import com.zimbra.client.ZTag;

import java.util.List;

public class ZRefreshEvent {

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
     * @return user root folder
     */
    public ZFolder getUserRoot() {
        return mUserRoot;
    }

    public List<ZTag> getTags() {
        return mTags;
    }

    public String toString() {
        return "[ZRefreshEvent]";
    }
}
