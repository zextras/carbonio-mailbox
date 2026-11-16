// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import java.util.Arrays;

import java.util.ArrayList;
import java.util.List;

public class ZDeleteEvent {

    private String mIds;
    private List<String> mList;

    public ZDeleteEvent(String ids) {
        mIds = ids;
    }

    public String getIds() {
        return mIds;
    }

    public synchronized List<String> toList() {
        if (mList == null) {
            mList = new ArrayList<>();
            if (mIds != null && mIds.length() > 0)
              mList.addAll(Arrays.asList(mIds.split(",")));
        }
        return mList;
    }
    
    public String toString() {
        return String.format("[ZDeleteEvent %s]", getIds());
    }
}
