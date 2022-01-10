// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.google.common.base.MoreObjects;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;

/**
 * @since Feb 15, 2005
 */
public class CalendarItemHit extends ZimbraHit {

    protected int id;
    protected CalendarItem item;

    CalendarItemHit(ZimbraQueryResultsImpl results, Mailbox mbx, int id, CalendarItem cal, Object sortValue) {
        super(results, mbx, sortValue);
        this.id = id;
        item = cal;
    }

    @Override
    public MailItem getMailItem() throws ServiceException {
        return getCalendarItem();
    }

    public CalendarItem getCalendarItem() throws ServiceException {
        if (item == null) {
            item = getMailbox().getCalendarItemById(null, id);
        }
        return item;
    }

    @Override
    public int getConversationId() {
        assert(false);
        return 0;
    }

    @Override
    public int getItemId() {
        return id;
    }

    @Override
    void setItem(MailItem value) {
        item = (CalendarItem) value;
    }

    @Override
    boolean itemIsLoaded() {
        return (id == 0) || (item != null);
    }

    @Override
    public String getName() throws ServiceException {
        return getCalendarItem().getSubject();
    }

    @Override
    public String toString() {
        try {
            return MoreObjects.toStringHelper(this).add("name", getName()).toString();
        } catch (Exception e) {
            return e.toString();
        }
    }
}
