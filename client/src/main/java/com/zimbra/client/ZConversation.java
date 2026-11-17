// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;

public class ZConversation implements ZItem, ToZJSONObject {

    public enum Flag {
        unread('u'),
        draft('d'),
        flagged('f'),
        highPriority('!'),
        lowPriority('?'),
        sentByMe('s'),
        replied('r'),
        forwarded('w'),
        attachment('a');

        private char mFlagChar;

        public char getFlagChar() { return mFlagChar; }

        public static String toNameList(String flags) {
            if (flags == null || flags.length() == 0) return "";
            StringBuilder sb = new StringBuilder();
            for (int i=0; i < flags.length(); i++) {
                String v = null;
                for (Flag f : Flag.values()) {
                    if (f.getFlagChar() == flags.charAt(i)) {
                        v = f.name();
                        break;
                    }
                }
                if (sb.length() > 0) sb.append(", ");
                sb.append(v == null ? flags.substring(i, i+1) : v);
            }
            return sb.toString();
        }

        Flag(char flagChar) {
            mFlagChar = flagChar;

        }
    }

    private String mId;
    private String mFlags;
    private String mSubject;
    private String mTags;
    private int mMessageCount;
    private List<ZMessageSummary> mMessageSummaries;
    private ZMailbox mMailbox;

    public ZConversation(Element e, ZMailbox mailbox) throws ServiceException {
        mId = e.getAttribute(MailConstants.A_ID);
        mFlags = e.getAttribute(MailConstants.A_FLAGS, null);
        mTags = e.getAttribute(MailConstants.A_TAGS, null);
        mSubject = e.getAttribute(MailConstants.E_SUBJECT, null);
        mMessageCount = (int) e.getAttributeLong(MailConstants.A_NUM);

        mMessageSummaries = new ArrayList<>();
        for (Element msgEl: e.listElements(MailConstants.E_MSG)) {
            mMessageSummaries.add(new ZMessageSummary(msgEl));
        }
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
    public ObjectNode toObjectNode() {
        ObjectNode jo = JsonNodeFactory.instance.objectNode();
        if (mId != null) jo.put("id", mId);
        if (mTags != null) jo.put("tagIds", mTags);
        if (mFlags != null) jo.put("flags", mFlags);
        if (mSubject != null) jo.put("subject", mSubject);
        jo.put("messageCount", mMessageCount);
        ZObjectNode.put(jo, "messageSummaries", mMessageSummaries);
        return jo;
    }

    @Override
    public String toString() {
        return String.format("[ZConversation %s]", mId);
    }

    public String dump() {
        return ToZJSONObject.toString(this);
    }

    public String getFlags() {
        return mFlags;
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

    public List<ZMessageSummary> getMessageSummaries() {
        return mMessageSummaries;
    }

    public static class ZMessageSummary implements ZItem, ToZJSONObject {

        private long mDate;
        private String mFlags;
        private String mTags;
        private String mFragment;
        private String mId;
        private String mFolderId;
        private ZEmailAddress mSender;
        private long mSize;
        private Element mElement;
        private String mSubject;

        public ZMessageSummary(Element e) throws ServiceException {
            mElement = e;
            mId = e.getAttribute(MailConstants.A_ID);
            mFlags = e.getAttribute(MailConstants.A_FLAGS, null);
            mDate = e.getAttributeLong(MailConstants.A_DATE);
            mTags = e.getAttribute(MailConstants.A_TAGS, null);
            mFolderId = e.getAttribute(MailConstants.A_FOLDER, null);
            mFragment = e.getAttribute(MailConstants.E_FRAG, null);
            mSize = e.getAttributeLong(MailConstants.A_SIZE);
            Element emailEl = e.getOptionalElement(MailConstants.E_EMAIL);
            if (emailEl != null) mSender = new ZEmailAddress(emailEl);
            mSubject = e.getAttribute(MailConstants.E_SUBJECT, null);
        }

			@Override
        public ObjectNode toObjectNode() {
            ObjectNode jo = JsonNodeFactory.instance.objectNode();
            if (mId != null) jo.put("id", mId);
            if (mFolderId != null) jo.put("folderId", mFolderId);
            if (mFlags != null) jo.put("flags", mFlags);
            if (mFragment != null) jo.put("fragment", mFragment);
            if (mTags != null) jo.put("tags", mTags);
            jo.put("size", mSize);
            if (mSender != null) jo.set("sender", mSender.toObjectNode());
            jo.put("date", mDate);
            jo.put("hasAttachment", hasAttachment());
            jo.put("hasFlags", hasFlags());
            jo.put("hasTags", hasTags());
            jo.put("isDeleted", isDeleted());
            jo.put("isDraft", isDraft());
            jo.put("isFlagged", isFlagged());
            jo.put("isHighPriority", isHighPriority());
            jo.put("isLowPriority", isLowPriority());
            jo.put("isForwarded", isForwarded());
            jo.put("isNotificationSent", isNotificationSent());
            jo.put("isRepliedTo", isRepliedTo());
            jo.put("isSentByMe", isSentByMe());
            jo.put("isUnread", isUnread());
            if (mSubject != null) jo.put("subject", mSubject);
            return jo;
        }

        @Override
        public String toString() {
            return String.format("[ZMessageSummary %s]", mId);
        }

        public long getDate() {
            return mDate;
        }

			public String getFragment() {
            return mFragment;
        }

        @Override
        public String getId() {
            return mId;
        }

        @Override
        public String getUuid() {
            return null;
        }

        public ZEmailAddress getSender() {
            return mSender;
        }

			public boolean hasFlags() {
            return mFlags != null && mFlags.length() > 0;
        }

        public boolean hasTags() {
            return mTags != null && mTags.length() > 0;
        }

        public boolean hasAttachment() {
            return hasFlags() && mFlags.indexOf(ZMessage.Flag.ATTACHED.getFlagChar()) != -1;
        }

        public boolean isDeleted() {
            return hasFlags() && mFlags.indexOf(ZMessage.Flag.DELETED.getFlagChar()) != -1;
        }

        public boolean isDraft() {
            return hasFlags() && mFlags.indexOf(ZMessage.Flag.DRAFT.getFlagChar()) != -1;
        }

        public boolean isFlagged() {
            return hasFlags() && mFlags.indexOf(ZMessage.Flag.FLAGGED.getFlagChar()) != -1;
        }

        public boolean isHighPriority() {
            return hasFlags() && mFlags.indexOf(ZMessage.Flag.HIGH_PRIORITY.getFlagChar()) != -1;
        }

        public boolean isLowPriority() {
            return hasFlags() && mFlags.indexOf(ZMessage.Flag.LOW_PRIORITY.getFlagChar()) != -1;
        }

        public boolean isForwarded() {
            return hasFlags() && mFlags.indexOf(ZMessage.Flag.FORWARDED.getFlagChar()) != -1;
        }

        public boolean isNotificationSent() {
            return hasFlags() && mFlags.indexOf(ZMessage.Flag.NOTIFIED.getFlagChar()) != -1;
        }

        public boolean isRepliedTo() {
            return hasFlags() && mFlags.indexOf(ZMessage.Flag.REPLIED.getFlagChar()) != -1;
        }

        public boolean isSentByMe() {
            return hasFlags() && mFlags.indexOf(ZMessage.Flag.FROM_ME.getFlagChar()) != -1;
        }

        public boolean isUnread() {
            return hasFlags() && mFlags.indexOf(ZMessage.Flag.UNREAD.getFlagChar()) != -1;
        }
    }

    public boolean hasFlags() {
        return mFlags != null && mFlags.length() > 0;
    }

    public boolean hasTags() {
        return mTags != null && mTags.length() > 0;
    }

}
