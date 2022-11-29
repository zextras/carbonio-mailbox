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
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.MailItem.TargetConstraint;
import com.zimbra.cs.mailbox.util.TagUtil;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

/**
 * @since 2004. 9. 13.
 */
public class SetItemTags extends RedoableOp {

    private int[] mIds;
    private MailItem.Type type;
    private int mFlags;
    private String[] mTags;
    private long mTagBitmask;
    private String mConstraint;

    public SetItemTags() {
        super(MailboxOperation.SetItemTags);
        type = MailItem.Type.UNKNOWN;
        mConstraint = null;
    }

    public SetItemTags(int mailboxId, int[] itemIds, MailItem.Type type, int flags, String[] tags, TargetConstraint tcon) {
        this();
        setMailboxId(mailboxId);
        mIds = itemIds;
        this.type = type;
        mFlags = flags;
        mTags = tags;
        mConstraint = (tcon == null ? null : tcon.toString());
    }

    @Override
    protected String getPrintableData() {
        StringBuilder sb = new StringBuilder("ids=");
        sb.append(Arrays.toString(mIds)).append(", type=").append(type);
        sb.append(", flags=[").append(mFlags);
        sb.append("], tags=[").append(TagUtil.encodeTags(mTags)).append("]");
        if (mConstraint != null) {
            sb.append(", constraint=").append(mConstraint);
        }
        return sb.toString();
    }

    @Override
    protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(-1);
        out.writeByte(type.toByte());
        out.writeInt(mFlags);
        if (getVersion().atLeast(1, 33)) {
            out.writeUTFArray(mTags);
        } else {
            out.writeLong(mTagBitmask);
        }
        boolean hasConstraint = mConstraint != null;
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
        mFlags = in.readInt();
        if (getVersion().atLeast(1, 33)) {
            mTags = in.readUTFArray();
        } else {
            mTagBitmask = in.readLong();
        }
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

        if (mTags == null && mTagBitmask != 0) {
            mTags = TagUtil.tagBitmaskToNames(mbox, octxt, mTagBitmask);
        }

        TargetConstraint tcon = null;
        if (mConstraint != null) {
            try {
                tcon = TargetConstraint.parseConstraint(mbox, mConstraint);
            } catch (ServiceException e) {
                mLog.warn(e);
            }
        }

        mbox.setTags(octxt, mIds, type, mFlags, mTags, tcon);
    }
}
