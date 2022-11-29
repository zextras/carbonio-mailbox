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

public class CreateTag extends RedoableOp {

    private int mTagId;
    private String mName;
    private long mColor;

    public CreateTag() {
        super(MailboxOperation.CreateTag);
        mTagId = UNKNOWN_ID;
        mColor = 0;
    }

    public CreateTag(int mailboxId, String name, Color color) {
        this();
        setMailboxId(mailboxId);
        mTagId = UNKNOWN_ID;
        mName = name != null ? name : "";
        mColor = color.getValue();
    }

    public int getTagId() {
        return mTagId;
    }

    public void setTagId(int tagId) {
        mTagId = tagId;
    }

    @Override protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("id=").append(mTagId);
        sb.append(", name=").append(mName).append(", color=").append(mColor);
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mTagId);
        out.writeUTF(mName);
        // mColor from byte to long in Version 1.27
        out.writeLong(mColor);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        mTagId = in.readInt();
        mName = in.readUTF();
        if (getVersion().atLeast(1, 27))
            mColor = in.readLong();
        else
            mColor = in.readByte();
    }

    @Override public void redo() throws Exception {
        int mboxId = getMailboxId();
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(mboxId);

        try {
            mbox.createTag(getOperationContext(), mName, Color.fromMetadata(mColor));
        } catch (MailServiceException e) {
            String code = e.getCode();
            if (code.equals(MailServiceException.ALREADY_EXISTS)) {
                if (mLog.isInfoEnabled())
                    mLog.info("Tag " + mTagId + " already exists in mailbox " + mboxId);
            } else {
                throw e;
            }
        }
    }
}
