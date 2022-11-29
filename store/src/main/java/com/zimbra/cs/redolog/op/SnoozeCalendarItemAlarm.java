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

public class SnoozeCalendarItemAlarm extends RedoableOp {

    private int id;
    private long snoozeUntil;

    public SnoozeCalendarItemAlarm() {
        super(MailboxOperation.SnoozeCalendarItemAlarm);
        id = UNKNOWN_ID;
    }

    public SnoozeCalendarItemAlarm(int mailboxId, int id, long snoozeUntil) {
        this();
        setMailboxId(mailboxId);
        this.id = id;
        this.snoozeUntil = snoozeUntil;
    }

    @Override protected String getPrintableData() {
        StringBuilder sb = new StringBuilder("id=");
        sb.append(id).append(", snoozeUntil=").append(snoozeUntil);
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(id);
        out.writeLong(snoozeUntil);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        id = in.readInt();
        snoozeUntil = in.readLong();
    }

    @Override public void redo() throws Exception {
        int mboxId = getMailboxId();
        Mailbox mailbox = MailboxManager.getInstance().getMailboxById(mboxId);
        mailbox.snoozeCalendarItemAlarm(getOperationContext(), id, snoozeUntil);
    }
}
