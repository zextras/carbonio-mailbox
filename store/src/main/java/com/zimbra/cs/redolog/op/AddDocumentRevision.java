// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class AddDocumentRevision extends SaveDocument {
    private int mDocId;

    public AddDocumentRevision() {
        mOperation = MailboxOperation.AddDocumentRevision;
    }

    public AddDocumentRevision(int mailboxId, String digest, long msgSize, int folderId) {
        super(mailboxId, digest, msgSize, folderId, 0);
        mOperation = MailboxOperation.AddDocumentRevision;
    }

    public void setDocId(int docId) {
        mDocId = docId;
    }

    public int getDocId() {
        return mDocId;
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mDocId);
        super.serializeData(out);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        mDocId = in.readInt();
        super.deserializeData(in);
    }

    @Override public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        OperationContext octxt = getOperationContext();
        try {
            mbox.addDocumentRevision(octxt, mDocId, getAuthor(), getFilename(), getDescription(), isDescriptionEnabled(), getAdditionalDataStream());
        } catch (MailServiceException e) {
            if (e.getCode() == MailServiceException.ALREADY_EXISTS) {
                mLog.info("Document revision " + getMessageId() + " is already in mailbox " + mbox.getId());
                return;
            } else {
                throw e;
            }
        }
    }
}
