// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import java.util.ArrayList;
import java.util.List;


import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;

public class ZCalendarItem implements ZItem {

    private String mId;
    private String mFlags;
    private String mTags;
    private String mFolderId;
    private long mDate;
    private long mSize;
    private String mUID;
    private List<ZInvite> mInvites;

    public ZCalendarItem(Element e) throws ServiceException {
        mId = e.getAttribute(MailConstants.A_ID);
        mFlags = e.getAttribute(MailConstants.A_FLAGS, null);
        mTags = e.getAttribute(MailConstants.A_TAGS, null);
        mUID = e.getAttribute(MailConstants.A_UID, null);
        mDate = e.getAttributeLong(MailConstants.A_DATE, 0);
        mFolderId = e.getAttribute(MailConstants.A_FOLDER, null);
        mSize = e.getAttributeLong(MailConstants.A_SIZE);
        mInvites = new ArrayList<>();
        for (Element inviteEl : e.listElements(MailConstants.E_INVITE)) {
            mInvites.add(new ZInvite(inviteEl));
        }
    }

    public List<ZInvite> getInvites() {
        return mInvites;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("[ZCalendarItem %s]", mId);
    }

    public String getFlags() {
        return mFlags;
    }

    public String getTagIds() {
        return mTags;
    }

    public String getFolderId() {
        return mFolderId;
    }

}
