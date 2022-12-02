// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Nov 12, 2005
 */
package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class GrantAccess extends RedoableOp {

    private int mFolderId;
    private String mGrantee;
    private byte mGranteeType;
    private short mRights;
    private String mPassword;
    private long mExpiry;

    public GrantAccess() {
        super(MailboxOperation.GrantAccess);
        mFolderId = UNKNOWN_ID;
        mGrantee = "";
    }

    public GrantAccess(int mailboxId, int folderId, String grantee, byte granteeType, short rights, String password,
            long expiry) {
        this();
        setMailboxId(mailboxId);
        mFolderId = folderId;
        mGrantee = grantee == null ? "" : grantee;
        mGranteeType = granteeType;
        mRights = rights;
        mPassword = password == null ? "" : password;
        mExpiry = expiry;
    }

    @Override protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("id=").append(mFolderId);
        sb.append(", grantee=").append(mGrantee);
        sb.append(", type=").append(mGranteeType);
        sb.append(", rights=").append(ACL.rightsToString(mRights));
        sb.append(", args=").append(mPassword);
        sb.append(", expiry=").append(mExpiry);
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mFolderId);
        out.writeUTF(mGrantee);
        out.writeByte(mGranteeType);
        out.writeShort(mRights);
        out.writeBoolean(true);
        out.writeUTF(mPassword);
        out.writeLong(mExpiry);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        mFolderId = in.readInt();
        mGrantee = in.readUTF();
        mGranteeType = in.readByte();
        mRights = in.readShort();
        in.readBoolean();  // "INHERIT", deprecated as of 02-Apr-2006
        if (getVersion().atLeast(1, 2)) {
        	mPassword = in.readUTF();
        }
        if (getVersion().atLeast(1, 36)) {
            mExpiry = in.readLong();
        }
    }

    @Override public void redo() throws ServiceException {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        mbox.grantAccess(getOperationContext(), mFolderId, mGrantee, mGranteeType, mRights, mPassword, mExpiry);
    }
}
