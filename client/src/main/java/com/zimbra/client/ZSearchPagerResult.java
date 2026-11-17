// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;


public class ZSearchPagerResult {

    private ZSearchResult mSearchResult;
    private int mActualPage;
    private int mRequstedPage;
    private int mLimit;

    ZSearchPagerResult(ZSearchResult result, int requestedPage, int actualPage, int limit) {
        mSearchResult = result;
        mActualPage = actualPage;
        mRequstedPage = requestedPage;
        mLimit = limit;
    }

    public ZSearchResult getResult() { return mSearchResult; }
    public int getActualPage() { return mActualPage; }

	public String toString() {
        return String.format("[ZSearchPagerResult %s]", mActualPage);
    }
}
