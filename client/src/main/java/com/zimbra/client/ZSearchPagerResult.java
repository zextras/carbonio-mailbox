// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;


public class ZSearchPagerResult {

    private ZSearchResult mSearchResult;
    private int mActualPage;

    ZSearchPagerResult(ZSearchResult result, int requestedPage, int actualPage) {
        mSearchResult = result;
        mActualPage = actualPage;
    }

    public ZSearchResult getResult() { return mSearchResult; }
    public int getActualPage() { return mActualPage; }

	public String toString() {
        return String.format("[ZSearchPagerResult %s]", mActualPage);
    }
}
