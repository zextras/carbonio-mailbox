// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 7. 21.
 */
package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class ModifySavedSearch extends RedoableOp {

    private int mSearchId;
    private String mQuery;
    private String mTypes;
    private String mSort;

    public ModifySavedSearch() {
        super(MailboxOperation.ModifySavedSearch);
        mSearchId = UNKNOWN_ID;
    }

    public ModifySavedSearch(int mailboxId, int searchId, String query, String types, String sort) {
        this();
        setMailboxId(mailboxId);
        mSearchId = searchId;
        mQuery = query != null ? query : "";
        mTypes = types != null ? types : "";
        mSort = sort != null ? sort : "";
    }

    @Override protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("id=");
        sb.append(mSearchId).append(", query=").append(mQuery);
        sb.append(", types=").append(mTypes).append(", sort=").append(mSort);
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mSearchId);
        out.writeUTF(mQuery);
        out.writeUTF(mTypes);
        out.writeUTF(mSort);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        mSearchId = in.readInt();
        mQuery = in.readUTF();
        mTypes = in.readUTF();
        mSort = in.readUTF();
    }

    @Override public void redo() throws Exception {
        Mailbox mailbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        mailbox.modifySearchFolder(getOperationContext(), mSearchId, mQuery, mTypes, mSort);
    }
}
