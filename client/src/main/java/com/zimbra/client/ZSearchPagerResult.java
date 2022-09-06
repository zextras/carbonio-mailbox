// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import org.json.JSONException;

public class ZSearchPagerResult implements ToZJSONObject {

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

  public ZSearchResult getResult() {
    return mSearchResult;
  }

  public int getActualPage() {
    return mActualPage;
  }

  public int getRequestedPage() {
    return mRequstedPage;
  }

  public int getOffset() {
    return mLimit * mActualPage;
  }

  public ZJSONObject toZJSONObject() throws JSONException {
    ZJSONObject zjo = new ZJSONObject();
    zjo.put("result", mSearchResult);
    zjo.put("requestedPage", mRequstedPage);
    zjo.put("actualPage", mActualPage);
    zjo.put("offset", getOffset());
    return zjo;
  }

  public String toString() {
    return String.format("[ZSearchPagerResult %s]", mActualPage);
  }

  public String dump() {
    return ZJSONObject.toString(this);
  }
}
