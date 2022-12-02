// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.google.common.base.MoreObjects;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Note;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailItem;

/**
 * @since Nov 9, 2004
 * @author tim
 */
public final class NoteHit extends ZimbraHit {
    private Note note ;
    private final int itemId;

    NoteHit(ZimbraQueryResultsImpl results, Mailbox mbx, int id, Note note, Object sortValue) {
        super(results, mbx, sortValue);
        itemId = id;
        this.note = note;
    }

    @Override
    public MailItem getMailItem() throws ServiceException {
        return getNote();
    }

    public Note getNote() throws ServiceException {
        if (note == null) {
            note = getMailbox().getNoteById(null, getItemId());
        }
        return note;
    }

    @Override
    void setItem(MailItem item) {
        note = (Note) item;
    }

    @Override
    boolean itemIsLoaded() {
        return note != null;
    }

    @Override
    public String getName() throws ServiceException {
        if (cachedName == null) {
            cachedName = getNote().getSubject();
        }
        return cachedName;
    }

    @Override
    public int getConversationId() {
        return 0;
    }

    @Override
    public int getItemId() {
        return itemId;
    }

    @Override
    public String toString() {
        try {
            return MoreObjects.toStringHelper(this)
                .add("id", getItemId())
                .add("conv", getConversationId())
                .add("note", getNote())
                .addValue(super.toString())
                .toString();
        } catch (ServiceException e) {
            return e.toString();
        }
    }

    public int getHitType() {
        return 4;
    }

    public int doitVirt() {
        return 0;
    }

}
