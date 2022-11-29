// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 12. 14.
 */
package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.common.mailbox.Color;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.Note;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class CreateNote extends RedoableOp {

    private int mId;
    private int mFolderId;
    private String mContent;
    private long mColor;
    private Note.Rectangle mBounds;

    public CreateNote() {
        super(MailboxOperation.CreateNote);
        mId = UNKNOWN_ID;
        mFolderId = UNKNOWN_ID;
    }

    public CreateNote(int mailboxId, int folderId,
                      String content, Color color, Note.Rectangle bounds) {
        this();
        setMailboxId(mailboxId);
        mId = UNKNOWN_ID;
        mFolderId = folderId;
        mContent = content != null ? content : "";
        mColor = color.getValue();
        mBounds = bounds;
    }

    public int getNoteId() {
        return mId;
    }

    public void setNoteId(int id) {
        mId = id;
    }

    @Override protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("id=").append(mId);
        sb.append(", folder=").append(mFolderId);
        sb.append(", content=").append(mContent);
        sb.append(", color=").append(mColor);
        if (mBounds != null)
            sb.append(", bounds=(").append(mBounds).append(")");
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mId);
        out.writeInt(mFolderId);
        out.writeShort((short) -1);
        out.writeUTF(mContent);
        // mColor from byte to long in Version 1.27
        out.writeLong(mColor);
        out.writeInt(mBounds.x);
        out.writeInt(mBounds.y);
        out.writeInt(mBounds.width);
        out.writeInt(mBounds.height);
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        mId = in.readInt();
        mFolderId = in.readInt();
        in.readShort();
        mContent = in.readUTF();
        if (getVersion().atLeast(1, 27))
            mColor = in.readLong();
        else
            mColor = in.readByte();
        int x = in.readInt();
        int y = in.readInt();
        int w = in.readInt();
        int h = in.readInt();
        mBounds = new Note.Rectangle(x, y, w, h);
    }

    @Override public void redo() throws Exception {
        int mboxId = getMailboxId();
        Mailbox mailbox = MailboxManager.getInstance().getMailboxById(mboxId);

        try {
            mailbox.createNote(getOperationContext(), mContent, mBounds, Color.fromMetadata(mColor), mFolderId);
        } catch (MailServiceException e) {
            String code = e.getCode();
            if (code.equals(MailServiceException.ALREADY_EXISTS)) {
                if (mLog.isInfoEnabled())
                    mLog.info("Note " + mId + " already exists in mailbox " + mboxId);
            } else
                throw e;
        }
    }
}
