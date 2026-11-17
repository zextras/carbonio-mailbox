// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.client.event.ZModifyConversationEvent;
import com.zimbra.client.event.ZModifyEvent;

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
        mMessageIds = new ArrayList<>();
        for (Element m: e.listElements(MailConstants.E_MSG)) {
            mMessageIds.add(m.getAttribute(MailConstants.A_ID));
        }

        mRecipients = new ArrayList<>();
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
    public ObjectNode toObjectNode() {
        ObjectNode jo = JsonNodeFactory.instance.objectNode();
        if (mId != null) jo.put("id", mId);
        if (mFlags != null) jo.put("flags", mFlags);
        if (mTags != null) jo.put("tags", mTags);
        if (mFragment != null) jo.put("fragment", mFragment);
        if (mSubject != null) jo.put("subject", mSubject);
        jo.put("date", mDate);
        if (mSortField != null) jo.put("sortField", mSortField);
        jo.put("messageCount", mMessageCount);
        ZObjectNode.putList(jo, "messageIds", mMessageIds);
        ZObjectNode.put(jo, "recipients", mRecipients);
        return jo;
    }

    @Override
    public String toString() {
        return String.format("[ZConversationHit %s]", mId);
    }

	public long getDate() {
        return mDate;
    }

	@Override
    public String getSortField() {
        return mSortField;
    }

    public String getSubject() {
        return mSubject;
    }

	public int getMessageCount() {
        return mMessageCount;
    }

	public List<ZEmailAddress> getRecipients() {
        return mRecipients;
    }


}
