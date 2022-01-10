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
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class RevokeAccess extends RedoableOp {

    private int folderId;
    private String grantee;
    boolean dueToExpiry = false;

    public RevokeAccess() {
        super(MailboxOperation.RevokeAccess);
        folderId = UNKNOWN_ID;
        grantee = "";
    }

    public RevokeAccess(int mailboxId, int folderId, String grantee) {
        this(false, mailboxId, folderId, grantee);
    }

    public RevokeAccess(boolean dueToExpiry, int mailboxId, int folderId, String grantee) {
        this();
        if (dueToExpiry) {
            this.dueToExpiry = dueToExpiry;
            mOperation = MailboxOperation.ExpireAccess;
        }
        setMailboxId(mailboxId);
        this.folderId = folderId;
        this.grantee = grantee == null ? "" : grantee;
    }

    @Override protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("id=").append(folderId);
        sb.append(", grantee=").append(grantee);
        if (dueToExpiry) {
            sb.append(", dueToExpiry=").append(dueToExpiry);
        }
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(folderId);
        out.writeUTF(grantee);
        out.writeBoolean(dueToExpiry);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        folderId = in.readInt();
        grantee = in.readUTF();
        if (getVersion().atLeast(1, 39)) {
            dueToExpiry = in.readBoolean();
            if (dueToExpiry) {
                mOperation = MailboxOperation.ExpireAccess;
            }
        }
    }

    @Override public void redo() throws ServiceException {
        if (dueToExpiry) {
            // no need of redoing the op since expire access op is invoked by a system scheduled task
            return;
        }
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        mbox.revokeAccess(getOperationContext(), dueToExpiry, folderId, grantee);
    }
}
