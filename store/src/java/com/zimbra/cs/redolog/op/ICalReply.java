// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.common.calendar.ICalTimeZone;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.Metadata;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mailbox.calendar.Util;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class ICalReply extends RedoableOp {

    private Invite mInvite;
    private String mSender;
    
    public ICalReply()  {
        super(MailboxOperation.ICalReply);
    }

    public ICalReply(int mailboxId, Invite inv, String sender) {
        this();
        setMailboxId(mailboxId);
        mInvite = inv;
        mSender = sender;
    }

    @Override protected String getPrintableData() {
        StringBuilder sb = new StringBuilder();
        ICalTimeZone localTz = mInvite.getTimeZoneMap().getLocalTimeZone();
        sb.append("localTZ=").append(Util.encodeAsMetadata(localTz).toString());
        sb.append(", inv=").append(Invite.encodeMetadata(mInvite).toString());
        sb.append(", sender=").append(mSender);
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        ICalTimeZone localTz = mInvite.getTimeZoneMap().getLocalTimeZone();
        out.writeUTF(Util.encodeAsMetadata(localTz).toString());
        out.writeUTF(Invite.encodeMetadata(mInvite).toString());
        out.writeUTF(mSender);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        try {
            ICalTimeZone localTz = Util.decodeTimeZoneFromMetadata(new Metadata(in.readUTF()));
            mInvite = Invite.decodeMetadata(getMailboxId(), new Metadata(in.readUTF()), null, localTz);
            mSender = in.readUTF();
        } catch (ServiceException ex) {
            ex.printStackTrace();
            throw new IOException("Cannot read serialized entry for ICalReply " + ex.toString());
        }
    }
    
    @Override public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        mbox.processICalReply(getOperationContext(), mInvite, mSender);
    }
}
