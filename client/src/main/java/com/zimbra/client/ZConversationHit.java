// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.client.event.ZModifyConversationEvent;
import com.zimbra.client.event.ZModifyEvent;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class ZConversationHit implements ZSearchHit {

    private String mId;
    private String mFlags;
    private String mFragment;
    private String mSubject;
    private String mSortField;
    private String mTags;
    private int mMessageCount;
    private long mDate;
    private List<String> mMessageIds;
    private List<ZEmailAddress> mRecipients;

    public ZConversationHit(Element e) throws ServiceException {
        mId = e.getAttribute(MailConstants.A_ID);
        mFlags = e.getAttribute(MailConstants.A_FLAGS, null);
        mDate = e.getAttributeLong(MailConstants.A_DATE);
        mTags = e.getAttribute(MailConstants.A_TAGS, null);
        mFragment = e.getAttribute(MailConstants.E_FRAG, null);
        mSubject = e.getAttribute(MailConstants.E_SUBJECT, null);
        mSortField = e.getAttribute(MailConstants.A_SORT_FIELD, null);
        mMessageCount = (int) e.getAttributeLong(MailConstants.A_NUM);
        mMessageIds = new ArrayList<String>();
        for (Element m: e.listElements(MailConstants.E_MSG)) {
            mMessageIds.add(m.getAttribute(MailConstants.A_ID));
        }

        mRecipients = new ArrayList<ZEmailAddress>();
        for (Element emailEl: e.listElements(MailConstants.E_EMAIL)) {
            mRecipients.add(new ZEmailAddress(emailEl));
        }
    }

    @Override
    public void modifyNotification(ZModifyEvent event) throws ServiceException {
        if (event instanceof ZModifyConversationEvent) {
            ZModifyConversationEvent cevent = (ZModifyConversationEvent) event;
            mFlags = cevent.getFlags(mFlags);
            mTags = cevent.getTagIds(mTags);
            mSubject = cevent.getSubject(mSubject);
            mFragment = cevent.getFragment(mFragment);
            mMessageCount = cevent.getMessageCount(mMessageCount);
            mRecipients = cevent.getRecipients(mRecipients);
        }
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public ZJSONObject toZJSONObject() throws JSONException {
        ZJSONObject jo = new ZJSONObject();
        jo.put("id", mId);
        jo.put("flags", mFlags);
        jo.put("tags", mTags);
        jo.put("fragment", mFragment);
        jo.put("subject", mSubject);
        jo.put("date", mDate);
        jo.put("sortField", mSortField);
        jo.put("messageCount", mMessageCount);
        jo.putList("messageIds", mMessageIds);
        jo.put("recipients", mRecipients);
        return jo;
    }

    @Override
    public String toString() {
        return String.format("[ZConversationHit %s]", mId);
    }

    public String dump() {
        return ZJSONObject.toString(this);
    }

    public String getFlags() {
        return mFlags;
    }

    public long getDate() {
        return mDate;
    }

    public String getFragment() {
        return mFragment;
    }

    @Override
    public String getSortField() {
        return mSortField;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getTagIds() {
        return mTags;
    }

    public int getMessageCount() {
        return mMessageCount;
    }

    public List<String> getMatchedMessageIds() {
        return mMessageIds;
    }

    public List<ZEmailAddress> getRecipients() {
        return mRecipients;
    }

    public boolean hasFlags() {
        return mFlags != null && mFlags.length() > 0;
    }

    public boolean hasTags() {
        return mTags != null && mTags.length() > 0;
    }

    public boolean hasAttachment() {
        return hasFlags() && mFlags.indexOf(ZConversation.Flag.attachment.getFlagChar()) != -1;
    }

    public boolean isFlagged() {
        return hasFlags() && mFlags.indexOf(ZConversation.Flag.flagged.getFlagChar()) != -1;
    }

    public boolean isHighPriority() {
        return hasFlags() && mFlags.indexOf(ZConversation.Flag.highPriority.getFlagChar()) != -1;
    }

    public boolean isLowPriority() {
        return hasFlags() && mFlags.indexOf(ZConversation.Flag.lowPriority.getFlagChar()) != -1;
    }

    public boolean isSentByMe() {
        return hasFlags() && mFlags.indexOf(ZConversation.Flag.sentByMe.getFlagChar()) != -1;
    }

    public boolean isUnread() {
        return hasFlags() && mFlags.indexOf(ZConversation.Flag.unread.getFlagChar()) != -1;
    }

    public boolean isDraft() {
        return hasFlags() && mFlags.indexOf(ZConversation.Flag.draft.getFlagChar()) != -1;
    }


}
