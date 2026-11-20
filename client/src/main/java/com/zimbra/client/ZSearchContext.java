// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;


public class ZSearchContext {

    private ZMailbox mMailbox;
    private ZSearchParams mParams;
    private ZSearchResult mResult;
    private int mIndex;
    private int mCount;
    private int mMaxHits = Integer.MAX_VALUE;
    private boolean mByOffset = false;
    private boolean mHasMore;

    public String toString() {
       return String.format("[ZSearchContext count=%d more=%s]", mCount, mHasMore);
    }
}
