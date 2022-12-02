// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 7. 21.
 */
package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.common.mailbox.Color;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class CreateSavedSearch extends RedoableOp {

    private int mSearchId;
    private String mUuid;
    private String mName;
    private String mQuery;
    private String mTypes;
    private String mSort;
    private int mFolderId;
    private int mFlags;
    private long mColor;

    public CreateSavedSearch() {
        super(MailboxOperation.CreateSavedSearch);
        mSearchId = UNKNOWN_ID;
    }

    public CreateSavedSearch(int mailboxId, int folderId, String name, String query, String types, String sort, int flags, Color color) {
        this();
        setMailboxId(mailboxId);
        mSearchId = UNKNOWN_ID;
        mName = name != null ? name : "";
        mQuery = query != null ? query : "";
        mTypes = types != null ? types : "";
        mSort = sort != null ? sort : "";
        mFolderId = folderId;
        mFlags = flags;
        mColor = color.getValue();
    }

    public int getSearchId() {
        return mSearchId;
    }

    public String getUuid() {
        return mUuid;
    }

    public void setSearchIdAndUuid(int searchId, String uuid) {
        mSearchId = searchId;
        mUuid = uuid;
    }

    @Override protected String getPrintableData() {
        StringBuilder sb = new StringBuilder("id=").append(mSearchId);
        sb.append(", uuid=").append(mUuid);
        sb.append(", name=").append(mName).append(", query=").append(mQuery);
        sb.append(", types=").append(mTypes).append(", sort=").append(mSort);
        sb.append(", flags=").append(mFlags);
        sb.append(", color=").append(mColor);
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mSearchId);
        if (getVersion().atLeast(1, 37)) {
            out.writeUTF(mUuid);
        }
        out.writeUTF(mName);
        out.writeUTF(mQuery);
        out.writeUTF(mTypes);
        out.writeUTF(mSort);
        out.writeInt(mFolderId);
        out.writeInt(mFlags);
        // mColor from byte to long in Version 1.27
        out.writeLong(mColor);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        mSearchId = in.readInt();
        if (getVersion().atLeast(1, 37)) {
            mUuid = in.readUTF();
        }
        mName = in.readUTF();
        mQuery = in.readUTF();
        mTypes = in.readUTF();
        mSort = in.readUTF();
        mFolderId = in.readInt();
        mFlags = getVersion().atLeast(1, 28) ? in.readInt() : 0;
        if (getVersion().atLeast(1, 27))
            mColor = in.readLong();
        else
            mColor = in.readByte();
    }

    @Override public void redo() throws Exception {
        int mboxId = getMailboxId();
        Mailbox mailbox = MailboxManager.getInstance().getMailboxById(mboxId);

        try {
            mailbox.createSearchFolder(getOperationContext(), mFolderId, mName,
                mQuery, mTypes, mSort, mFlags, Color.fromMetadata(mColor));
        } catch (MailServiceException e) {
            String code = e.getCode();
            if (code.equals(MailServiceException.ALREADY_EXISTS)) {
                if (mLog.isInfoEnabled())
                    mLog.info("Search " + mSearchId + " already exists in mailbox " + mboxId);
            } else
                throw e;
        }
    }
}
