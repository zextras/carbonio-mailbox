// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.service.ServiceException;
import com.zimbra.client.event.ZModifyItemEvent;
import com.zimbra.client.event.ZModifyConversationEvent;
import com.zimbra.client.event.ZCreateItemEvent;
import com.zimbra.client.event.ZCreateMessageEvent;
import com.zimbra.client.ZSearchResult.ZConversationSummary;
import com.zimbra.client.ZSearchParams.Cursor;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class ZSearchCache {

    static class ZSearchCacheHit {
        ZSearchHit hit;
        int index;
    }

    private List<ZSearchCacheHit> mHits;
    private ZSearchParams mParams;
    private Map<String, ZSearchCacheHit> mHitMap;
    private String mConvId;
    private ZConversationSummary mConvSummary;
    private boolean mDirty;
    private boolean mHasMore;
    private int mSize;
    private ZSearchHit mLastHit;

    ZSearchCache(ZSearchParams params) {
        mParams = new ZSearchParams(params);
        mHitMap = new HashMap<>();
        mHasMore = true;
    }

    synchronized ZSearchResult search(ZMailbox mailbox, int page) throws ServiceException {
        int limit = mParams.getLimit();
        int offset = page * limit;

        List<ZSearchHit> hits = new ArrayList<>();

        int actualIndex = 0;
        int nonDeletedIndex = -1;

        while(hits.size() < limit && mHasMore) {
            if (actualIndex >= mHits.size()) fetchMoreHits(mailbox);
            ZSearchCacheHit hit = mHits.get(actualIndex++);
            if (hit != null) {
                nonDeletedIndex++;
                if (nonDeletedIndex >= offset) hits.add(hit.hit);
            }
        }
        return new ZSearchResult(hits, mConvSummary, mHasMore, mParams.getSortBy().name(), offset);
    }

    private void fetchMoreHits(ZMailbox mailbox) throws ServiceException {
        if (!mHasMore) return;

        if (mLastHit == null) {
            mParams.setCursor(null);
            mParams.setOffset(0);
        } else {
            mParams.setCursor(new Cursor(mLastHit.getId(), mLastHit.getSortField()));
        }

        ZSearchResult result = mParams.getConvId() == null ? mailbox.search(mParams) : mailbox.searchConversation(mParams.getConvId(), mParams);
        int i = mHits.size();

        for (ZSearchHit hit : result.getHits()) {
            ZSearchCacheHit ch = new ZSearchCacheHit();
            ch.hit = hit;
            ch.index = i++;
            mHits.add(ch);
            mHitMap.put(hit.getId(), ch);
            mLastHit = hit;
        }
        mConvSummary = result.getConversationSummary();
        if (mConvSummary != null) mConvId = mConvSummary.getId();
        mHasMore = result.hasMore();
    }

    /**
     *
     * @return conversation id if this pager holds result of a SearchConv
     */
    public String getConversationId() {
        return mConvId;
    }


    public boolean getDirty() {
        return mDirty;
    }
    
    /**
     * @param event modify event
     * @throws com.zimbra.common.service.ServiceException on error
     */
    void modifyNotification(ZModifyItemEvent event) throws ServiceException {
        if (mConvId != null && event instanceof ZModifyConversationEvent) {
            ZModifyConversationEvent mce = (ZModifyConversationEvent) event;
            if (mce.getMessageCount(-1) != -1) {
                mDirty = true;
            }
            if (mce.getId().equals(mConvId)) {
                mConvSummary.modifyNotification(event);
            }
        }
        ZSearchCacheHit hit = mHitMap.get(event.getId());
        if (hit != null)
            hit.hit.modifyNotification(event);
    }

    /**
     * @param event create item event
     * @throws com.zimbra.common.service.ServiceException on error
     */
    void createNotification(ZCreateItemEvent event) throws ServiceException {
        if (mConvId != null && event instanceof ZCreateMessageEvent) {
            ZCreateMessageEvent cme = (ZCreateMessageEvent) event;
            if (mConvId.equals(cme.getConversationId(null)))
                mDirty = true;
        }
    }
}
