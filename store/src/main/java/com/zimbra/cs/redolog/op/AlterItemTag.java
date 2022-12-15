// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.IOException;
import java.util.Arrays;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailItem.TargetConstraint;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.util.TagUtil;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

/**
 * @since 2004. 7. 21.
 */
public class AlterItemTag extends RedoableOp {

    private int[] mIds;
    private MailItem.Type type;
    private String mTagName;
    private int mTagId;
    private boolean mTagged;
    private String mConstraint;

    public AlterItemTag() {
        super(MailboxOperation.AlterItemTag);
        this.type = MailItem.Type.UNKNOWN;
        mTagId = UNKNOWN_ID;
        mTagged = false;
        mConstraint = null;
    }

    public AlterItemTag(int mailboxId, int[] ids, MailItem.Type type, String tag, boolean tagged, TargetConstraint tcon) {
        this();
        setMailboxId(mailboxId);
        mIds = ids;
        this.type = type;
        mTagName = tag;
        mTagged = tagged;
        mConstraint = (tcon == null ? null : tcon.toString());
    }

    @Override
    protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("ids=");
        sb.append(Arrays.toString(mIds)).append(", type=").append(type);
        sb.append(", tag=").append(mTagName == null ? "" + mTagId : mTagName).append(", tagged=").append(mTagged);
        if (mConstraint != null) {
            sb.append(", constraint=").append(mConstraint);
        }
        return sb.toString();
    }

    @Override
    protected void serializeData(RedoLogOutput out) throws IOException {
        boolean hasConstraint = mConstraint != null;
        out.writeInt(-1);
        out.writeByte(type.toByte());
        if (getVersion().atLeast(1, 33)) {
            out.writeUTF(mTagName);
        } else {
            out.writeInt(mTagId);
        }
        out.writeBoolean(mTagged);
        out.writeBoolean(hasConstraint);
        if (hasConstraint) {
            out.writeUTF(mConstraint);
        }
        out.writeInt(mIds == null ? 0 : mIds.length);
        if (mIds != null) {
            for (int i = 0; i < mIds.length; i++) {
                out.writeInt(mIds[i]);
            }
        }
    }

    @Override
    protected void deserializeData(RedoLogInput in) throws IOException {
        int id = in.readInt();
        if (id > 0) {
            mIds = new int[] { id };
        }
        type = MailItem.Type.of(in.readByte());
        if (getVersion().atLeast(1, 33)) {
            mTagName = in.readUTF();
        } else {
            mTagId = in.readInt();
        }
        mTagged = in.readBoolean();
        if (in.readBoolean()) {
            mConstraint = in.readUTF();
        }
        if (id <= 0) {
            mIds = new int[in.readInt()];
            for (int i = 0; i < mIds.length; i++) {
                mIds[i] = in.readInt();
            }
        }
    }

    @Override
    public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());

        OperationContext octxt = getOperationContext();

        TargetConstraint tcon = null;
        if (mConstraint != null) {
            try {
                tcon = TargetConstraint.parseConstraint(mbox, mConstraint);
            } catch (ServiceException e) {
                mLog.warn(e);
            }
        }

        if (mTagName == null && mTagId != 0) {
            mTagName = TagUtil.tagIdToName(mbox, octxt, mTagId);
        }

        mbox.alterTag(octxt, mIds, type, mTagName, mTagged, tcon);
    }
}
