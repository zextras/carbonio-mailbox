// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class DeleteConfig extends RedoableOp {

    private String sectionPart;

    protected DeleteConfig() {
        super(MailboxOperation.DeleteConfig);
    }

    public DeleteConfig(int mboxId, String section) {
        this();
        setMailboxId(mboxId);
        this.sectionPart = section;
    }

    @Override
    public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        mbox.deleteConfig(getOperationContext(), this.sectionPart);
    }

    @Override
    protected String getPrintableData() {
        StringBuilder sb = new StringBuilder("sectionPart=").append(this.sectionPart);
        return sb.toString();
    }

    @Override
    protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeUTF(this.sectionPart);
    }

    @Override
    protected void deserializeData(RedoLogInput in) throws IOException {
        this.sectionPart = in.readUTF();
    }
}
