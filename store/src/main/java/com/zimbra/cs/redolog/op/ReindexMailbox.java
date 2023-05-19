// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

/**
 * @since 2005. 4. 4.
 */
public class ReindexMailbox extends RedoableOp {

    private Set<MailItem.Type> types = null;
    private Set<Integer> mItemIds = null;
    private int mCompletionId = 0;
    @Deprecated private boolean mSkipDelete = false;

    public ReindexMailbox() {
        super(MailboxOperation.ReindexMailbox);
    }

    public ReindexMailbox(int mailboxId, Set<MailItem.Type> types, Set<Integer> itemIds, int completionId, boolean skipDelete) {
        this();
        setMailboxId(mailboxId);
        assert(types == null || itemIds == null);
        this.types = types;
        mItemIds = itemIds;
        mCompletionId = completionId;
        mSkipDelete = skipDelete;
    }

    @Override
    public boolean deferCrashRecovery() {
        return true;
    }

    @Override
    public void redo() throws Exception {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        if (types != null) {
            mbox.index.startReIndexByType(types);
        } else if (mItemIds != null) {
            mbox.index.startReIndexById(mItemIds);
        } else {
            mbox.index.startReIndex();
        }
    }

    @Override
    protected String getPrintableData() {
        StringBuilder sb = new StringBuilder("Completion="+mCompletionId);
        sb.append(" SkipDelete="+(mSkipDelete?"TRUE":"FALSE"));
        if (mItemIds != null) {
            sb.append(" ITEMIDS[");
            boolean atStart = true;
            for (Integer i : mItemIds) {
                if (!atStart)
                    sb.append(',');
                else
                    atStart = false;
                sb.append(i);
            }
            sb.append(']');

            return sb.toString();
        } else if (types != null) {
            sb.append(" TYPES[");
            sb.append(types);
            sb.append(']');

            return sb.toString();
        } else {
            return null;
        }
    }

    @Override
    protected void serializeData(RedoLogOutput out) throws IOException {
        if (getVersion().atLeast(1,9)) {
            // completion ID
            out.writeInt(mCompletionId);

            // types
            if (types != null) {
                out.writeBoolean(true);
                int count = types.size();
                out.writeInt(count);
                for (MailItem.Type type : types) {
                    out.writeByte(type.toByte());
                    count--;
                }
                assert(count == 0);
            } else {
                out.writeBoolean(false);
            }

            // itemIds
            if (getVersion().atLeast(1,10)) {
                if (mItemIds != null) {
                    out.writeBoolean(true);
                    int count = mItemIds.size();
                    out.writeInt(count);
                    for (Integer i : mItemIds) {
                        out.writeInt(i);
                        count--;
                    }
                    assert(count == 0);
                } else {
                    out.writeBoolean(false);
                }

                if (getVersion().atLeast(1,20)) {
                    out.writeBoolean(mSkipDelete);
                }

            } // v10
        } // v9
    }

    @Override
    protected void deserializeData(RedoLogInput in) throws IOException {
        if (getVersion().atLeast(1,9)) {
            // completionId
            mCompletionId = in.readInt();

            // types
            if (in.readBoolean()) {
                types = EnumSet.noneOf(MailItem.Type.class);
                for (int count = in.readInt(); count > 0; count--) {
                    types.add(MailItem.Type.of(in.readByte()));
                }
            } else {
                types = null;
            }

            // itemIds
            if (getVersion().atLeast(1,10)) {
                if (in.readBoolean()) {
                    mItemIds = new HashSet<>();
                    for (int count = in.readInt(); count > 0; count--) {
                        mItemIds.add(in.readInt());
                    }
                }
                if (getVersion().atLeast(1,20)) {
                    mSkipDelete = in.readBoolean();
                }
            } else {
                mItemIds = null;
                mSkipDelete = false;
            } // v10

        } // v9
    }
}
