// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.client.event.ZModifyContactEvent;
import com.zimbra.common.mailbox.MailItemType;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ZimbraLog;

public class ZContact extends ZBaseItem implements ToZJSONObject {

    public static class ZContactAttachmentInfo {
        private final String mContentType;
        private final String mFileName;
        private final String mPart;
        private final long mLength;

        public ZContactAttachmentInfo(String part, String fileName, String contentType, long length) {
            mPart = part;
            mFileName = fileName;
            mContentType = contentType;
            mLength = length;
        }

    }

    private String mRefId;
    private String mFolderId;
    private String mRevision;
    private long mDate;
    private long mMetaDataChangedDate;
    private Map<String, String> mAttrs;
    private Map<String, ZContact> mMembers;
    private Map<String, ZContactAttachmentInfo> mAttachments;
    private boolean mGalContact;
    private boolean isDirty;
    private ContactMemberType mContactMemberType;

    /* Note for contacts, use a more limited set of flags than in ZItem */
    public enum Flag {
        flagged('f'),
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

    public enum ContactMemberType {
        contact("C"),
        galContact("G"),
        inlineContact("I");

        private String contactMemberType;

			ContactMemberType(String type) {
            contactMemberType = type;
        }
    }

	/**
     * Constructor called only in case of inline contacts
     * @param id email address of the inline contact
     * @throws ServiceException
     */
    public ZContact(String id) throws ServiceException {
        super(id);
        isDirty = false;
        mContactMemberType = ContactMemberType.inlineContact;
    }

    public ZContact(Element e, boolean galContact, ZMailbox mailbox) throws ServiceException {
        this(e, mailbox);
        mGalContact = galContact;
        mContactMemberType = galContact ? ContactMemberType.galContact : ContactMemberType.contact;
    }

    public ZContact(Element e, ZMailbox zmailbox) throws ServiceException {
        super(e.getAttribute(MailConstants.A_ID),
                e.getAttributeInt(MailConstants.A_IMAP_UID, -1),
                e.getAttributeInt(MailConstants.A_MODIFIED_SEQUENCE, 0));
        isDirty = false;
        mMailbox = zmailbox;
        mRefId = e.getAttribute(MailConstants.A_REF, null);
        mFolderId = e.getAttribute(MailConstants.A_FOLDER, null);
        mFlags = e.getAttribute(MailConstants.A_FLAGS, null);
        mTagIds = e.getAttribute(MailConstants.A_TAGS, null);
        mRevision = e.getAttribute(MailConstants.A_REVISION, null);
        mDate = e.getAttributeLong(MailConstants.A_DATE, 0);
        mMetaDataChangedDate = e.getAttributeLong(MailConstants.A_CHANGE_DATE, 0) * 1000;

        HashMap<String, String> attrs = new HashMap<>();
        HashMap<String, ZContactAttachmentInfo> attachments = new HashMap<>();

        for (Element attrEl : e.listElements(MailConstants.E_ATTRIBUTE)) {
            String name = attrEl.getAttribute(MailConstants.A_ATTRIBUTE_NAME);
            String part = attrEl.getAttribute(MailConstants.A_PART, null);
            if (part != null) {
                String fileName = attrEl.getAttribute(MailConstants.A_CONTENT_FILENAME, null);
                String contentType = attrEl.getAttribute(MailConstants.A_CONTENT_TYPE, null);
                long size = attrEl.getAttributeLong(MailConstants.A_SIZE, 0);
                attachments.put(name, new ZContactAttachmentInfo(part, fileName, contentType, size));
            } else {
                attrs.put(name, attrEl.getText());
            }
        }

        mAttrs = Collections.unmodifiableMap(attrs);
        mAttachments = Collections.unmodifiableMap(attachments);

        HashMap<String, ZContact> members = new HashMap<>();
        for (Element memberEl : e.listElements(MailConstants.E_CONTACT_GROUP_MEMBER)) {
            String id = memberEl.getAttribute(MailConstants.A_CONTACT_GROUP_MEMBER_VALUE);
            String type = memberEl.getAttribute(MailConstants.A_CONTACT_GROUP_MEMBER_TYPE);
            Element cnEl = memberEl.getOptionalElement(MailConstants.E_CONTACT);
            ZContact contact = null;
            if (cnEl != null)
                contact = new ZContact(cnEl, type.equals("G") ? true : false, zmailbox);
            else
                /**
                 * Inline contacts only have the email address as value and type as I.
                 */
                contact = new ZContact(id);
            members.put(id, contact);
        }
        mMembers = Collections.unmodifiableMap(members);
    }

    public String getFolderId() {
        return mFolderId;
    }

    @Override
    public int getFolderIdInMailbox() throws ServiceException {
        return getIdInMailbox(getFolderId());
    }

	@Override
    public String getId() {
        return mId;
    }

	public boolean isGalContact() {
        return mGalContact;
    }

    public boolean isGroup() { return (getAttrs().get("type") != null && getAttrs().get("type").equals("group")); }

	@Deprecated
    public List<ZEmailAddress> getGroupMembers() throws ServiceException {
        return ZEmailAddress.parseAddresses(getAttrs().get("dlist"), ZEmailAddress.EMAIL_TYPE_TO);
    }

	@Override
    public ObjectNode toObjectNode() {
        ObjectNode jo = JsonNodeFactory.instance.objectNode();
        if (mId != null) jo.put("id", mId);
        if (imapUid >= 0) {
            jo.put("imapUid", imapUid);
        }
        if (mFolderId != null) jo.put("folderId", mFolderId);
        if (mFlags != null) jo.put("flags", mFlags);
        if (mTagIds != null) jo.put("tagIds", mTagIds);
        jo.put("date", mDate);
        jo.put("metaDataChangedDate", mMetaDataChangedDate);
        if (mRevision != null) jo.put("revision", mRevision);
        jo.put("isFlagged", isFlagged());
        jo.put("isGalContact", isGalContact());
        jo.put("isGroup", isGroup());
        jo.put("hasFlags", hasFlags());
        jo.put("hasTags", hasTags());
        ZObjectNode.putMap(jo, "attrs", mAttrs);
        return jo;
    }

    @Override
    public String toString() {
        return String.format("[ZContact %s]", mId);
    }

    public String dump() {
        return ToZJSONObject.toString(this);
    }

    public Map<String, String> getAttrs() {
        return mAttrs;
    }

    public Map<String, ZContact> getMembers() {
        return mMembers;
    }

	@Override
    public long getDate() {
        return mDate;
    }

    public long getMetaDataChangedDate() {
        return mMetaDataChangedDate;
    }

    public String getRevision() {
        return mRevision;
    }

	public boolean isFlagged() {
        return hasFlags() && mFlags.indexOf(Flag.flagged.getFlagChar()) != -1;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void modifyNotification(ZModifyContactEvent cevent) throws ServiceException {
        if (cevent.getId().equals(mId)) {
            mTagIds = cevent.getTagIds(mTagIds);
            mFolderId = cevent.getFolderId(mFolderId);
            mFlags = cevent.getFlags(mFlags);
            mRevision = cevent.getRevision(mRevision);
            mMetaDataChangedDate = cevent.getDate(mDate);
            mMetaDataChangedDate = cevent.getMetaDataChangedDate(mMetaDataChangedDate);
            mAttrs = cevent.getAttrs(mAttrs);
            if(isGroup())
                isDirty = true;
        }
	}

    @Override
    public MailItemType getMailItemType() {
        return MailItemType.CONTACT;
    }

    @Override
    public long getSize() {
        //currently there is no code path that calls this method
        throw new UnsupportedOperationException("ZContact method not supported yet");
    }

    @Override
    public InputStream getContentStream() throws ServiceException {
        return mMailbox.getRESTResource(String.format("?id=%s", this.getId()));
    }

    /**
     * @return the UID the item is referenced by in the IMAP server.  Returns <tt>0</tt> for items that require
     * renumbering because of moves.
     * The "IMAP UID" will be the same as the item ID unless the item has been moved after the mailbox owner's first
     * IMAP session. */
    @Override
    public int getImapUid() {
        if (imapUid >= 0) {
            return imapUid;
        }
        if (isGalContact()) {
            return 0;  /* TODO: is this the best thing to do? */
        }
        ZimbraLog.mailbox.debug("ZContact getImapUid() - regetting UID");
        ZContact zc = null;
        try {
            /* Perhaps, this ZContact object was not created in a way which included Imap UID information (or
             * a renumber is under way).  Using this mechanism guarantees that the Imap UID will be asked for,
             * or if there is a cache hit, the mechanism that put the entry in the cache should have ensured
             * Imap UID info was provided. */
            zc = getMailbox().getContact(mId);
        } catch (ServiceException e) {
            ZimbraLog.mailbox.debug("ZContact getImapUid() - getContact failed", e);
            return 0;
        }
        if (null == zc) {
            return 0;
        }
        imapUid = Math.max(zc.imapUid, 0);
        return imapUid;
    }
}
