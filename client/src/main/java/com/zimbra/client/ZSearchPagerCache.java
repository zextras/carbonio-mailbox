// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import java.util.Iterator;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.MapUtil;
import com.zimbra.client.event.ZCreateEvent;
import com.zimbra.client.event.ZCreateItemEvent;
import com.zimbra.client.event.ZCreateMessageEvent;
import com.zimbra.client.event.ZDeleteEvent;
import com.zimbra.client.event.ZEventHandler;
import com.zimbra.client.event.ZModifyEvent;
import com.zimbra.client.event.ZModifyItemEvent;
import com.zimbra.client.event.ZModifyItemFolderEvent;
import com.zimbra.client.event.ZRefreshEvent;

class ZSearchPagerCache extends ZEventHandler {

    Map<ZSearchParams, ZSearchPager> mSearchPagerCache;
    private boolean mClearOnModifyItemFolder;

    ZSearchPagerCache(int maxItems, boolean clearOnModifyItemFolder) {
        mSearchPagerCache = MapUtil.newLruMap(maxItems);
        mClearOnModifyItemFolder = clearOnModifyItemFolder;
    }
    
    public synchronized ZSearchPagerResult search(ZMailbox mailbox, ZSearchParams params, int page, boolean useCache, boolean useCursor) throws ServiceException {
        ZSearchPager pager = mSearchPagerCache.get(params);
        if (pager != null && (!useCache || pager.isDirty())) {
            mSearchPagerCache.remove(params);
            pager = null;
        }
        if (pager == null) {
            pager = new ZSearchPager(params);
            mSearchPagerCache.put(params, pager);
        }
        ZSearchResult result = pager.search(mailbox, page, useCursor);
        return new ZSearchPagerResult(result,
                page,
                page >= pager.getNumberOfPages() ?  pager.getNumberOfPages()-1 : page,
                params.getLimit());
    }

    public synchronized void clear(String type) {
        if (type == null) {
            mSearchPagerCache.clear();
        } else {
            Iterator<Map.Entry<ZSearchParams, ZSearchPager>> mi = mSearchPagerCache.entrySet().iterator();
            while(mi.hasNext()) {
                ZSearchParams params = mi.next().getKey();
                if (params.getTypes() != null && params.getTypes().contains(type))
                    mi.remove();
            }
        }
    }
    
    public synchronized void handleRefresh(ZRefreshEvent refreshEvent, ZMailbox mailbox) throws ServiceException {
        // clear cache on a refresh
        mSearchPagerCache.clear();
    }

    public synchronized void handleCreate(ZCreateEvent event, ZMailbox mailbox) throws ServiceException {
        // TODO: continue to use cache as long as possible?
        // For now, just mark conv searches dirty when
        // a new message gets created in a cached search conv.
        //mSearchPagerCache.clear();
        if (event instanceof ZCreateMessageEvent) {
            for (Object obj : mSearchPagerCache.values()) {
                ((ZSearchPager)obj).createNotification((ZCreateItemEvent)event);
            }
        }
    }


    public synchronized void handleModify(ZModifyEvent event, ZMailbox mailbox) throws ServiceException {
		if (mClearOnModifyItemFolder && event instanceof ZModifyItemFolderEvent) {
			ZModifyItemFolderEvent mif = (ZModifyItemFolderEvent) event;
			if (mif.getFolderId(null) != null)
				mSearchPagerCache.clear();
		}
		if (event instanceof ZModifyItemEvent) {
            for (Object obj : mSearchPagerCache.values()) {
                ((ZSearchPager)obj).modifyNotification((ZModifyItemEvent)event);
            }
        }
    }

    public synchronized void handleDelete(ZDeleteEvent event, ZMailbox mailbox) throws ServiceException {
        // only delete cached pagers that contain an item?
        // easier to just clear the whole cache?
        mSearchPagerCache.clear();
    }
    
}
