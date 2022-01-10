// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class FixCalendarItemPriority extends RedoableOp {

    private int mId;

    public FixCalendarItemPriority()  {
        super(MailboxOperation.FixCalendarItemPriority);
    }

    public FixCalendarItemPriority(int mailboxId, int itemId) {
        this();
        setMailboxId(mailboxId);
        mId = itemId;
    }

    @Override protected String getPrintableData() {
        StringBuilder sb = new StringBuilder("id=");
        sb.append(mId);
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mId);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        mId = in.readInt();
    }

    @Override public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        OperationContext octxt = getOperationContext();
        CalendarItem calItem = mbox.getCalendarItemById(octxt, mId);
        if (calItem != null)
            mbox.fixCalendarItemPriority(octxt, calItem);
    }
}
