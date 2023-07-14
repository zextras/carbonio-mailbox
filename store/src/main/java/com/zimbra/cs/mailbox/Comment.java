// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.google.common.base.MoreObjects;
import com.zimbra.common.mailbox.Color;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbMailItem;
import com.zimbra.cs.mailbox.MailItem.CustomMetadata.CustomMetadataList;

public class Comment extends MailItem {

    Comment(Mailbox mbox, UnderlyingData data) throws ServiceException {
        this(mbox, data, false);
    }
    
    Comment(Mailbox mbox, UnderlyingData data, boolean skipCache) throws ServiceException {
        super(mbox, data, skipCache);
    }

    @Override
    public String getSender() {
        return "";
    }

    @Override
    boolean isTaggable() {
        return true;
    }

    @Override
    boolean isCopyable() {
        return true;
    }

    @Override
    boolean isMovable() {
        return true;
    }

    @Override
    boolean isMutable() {
        return false;
    }

    @Override
    boolean canHaveChildren() {
        // return true if we want comments to be made on comments
        return false;
    }

    /**
     * Content of the comment with the maximum of 1024 bytes.
     *
     * @return
     */
    public String getText() {
        return getSubject();
    }

    public String getCreatorAccountId() {
        return mCreatorId;
    }

    public Account getCreatorAccount() throws ServiceException {
        Provisioning prov = Provisioning.getInstance();
        return prov.getAccountById(mCreatorId);
    }

    /**
     * The creator of the comment has full permission.
     */
    @Override
    protected boolean hasFullPermission(Account authuser) {
        if (authuser != null && authuser.getId().equals(mCreatorId))
            return true;
        return super.hasFullPermission(authuser);
    }

    private String mCreatorId;

    @Override
    Metadata encodeMetadata(Metadata meta) {
        return encodeMetadata(meta, mRGBColor, mMetaVersion, mVersion, mCreatorId, mExtendedData);
    }

    private static String encodeMetadata(Color color, int metaVersion, int version, String accountId, CustomMetadata custom) {
        CustomMetadataList extended = (custom == null ? null : custom.asList());
        return encodeMetadata(new Metadata(), color, metaVersion, version, accountId, extended).toString();
    }

    private static Metadata encodeMetadata(Metadata meta, Color color, int metaVersion, int version, String accountId, CustomMetadataList extended) {
        meta.put(Metadata.FN_CREATOR, accountId);
        return encodeMetadata(meta, color, null, metaVersion, version, extended);
    }

    @Override
    void decodeMetadata(Metadata meta) throws ServiceException {
        super.decodeMetadata(meta);
        if (meta == null)
            return;
        mCreatorId = meta.get(Metadata.FN_CREATOR);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
        appendCommonMembers(helper);
        helper.add("type", getType());
        helper.add("creator", mCreatorId);
        helper.add("text", getSubject());
        try {
            helper.add("parent", getParent());
        } catch (ServiceException e) {
        }
        return helper.toString();
    }
}
