// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

/**
 * This class is deprecated and obsolete.
 */
public class IndexDeferredItems extends RedoableOp {
    
    private int[] mItemIds = null;
    private byte[] mItemTypes = null;

    public IndexDeferredItems() {
        super(MailboxOperation.IndexDeferredItems);
    }
    
    public void setIds(int[] itemIds, byte[] itemTypes) {
        mItemIds = itemIds;
        mItemTypes = itemTypes;
        if (mItemIds.length != mItemTypes.length)
            throw new IllegalArgumentException("ItemIds and ItemTypes arrays must be same size");
    }
    
    @Override
    protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mItemIds.length);
        for (int i = 0; i < mItemIds.length; i++)
            out.writeInt(mItemIds[i]);
        for (int i = 0; i < mItemIds.length; i++)
            out.writeByte(mItemTypes[i]);
    }
    
    @Override
    protected void deserializeData(RedoLogInput in) throws IOException {
        int count = in.readInt();
        mItemIds = new int[count];
        mItemTypes = new byte[count];
        for (int i = 0; i < count; i++)
            mItemIds[i] = in.readInt();
        for (int i = 0; i < count; i++)
            mItemTypes[i] = in.readByte();
    }
    
    public boolean deferCrashRecovery() {
        return true;
    }
    
    public int[] getItemIds() { return mItemIds; }
    public byte[] getItemTypes() { return mItemTypes; }

    @Override
    protected String getPrintableData() {
        StringBuilder sb = new StringBuilder();
        for (Integer i : mItemIds) {
            sb.append(i).append(',');
        }
        return sb.toString();
    }

    @Override
    public void redo() throws Exception {
        // do nothing.
    }

}
