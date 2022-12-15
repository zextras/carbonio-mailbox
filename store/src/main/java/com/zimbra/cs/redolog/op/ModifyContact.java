// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 12. 13.
 */
package com.zimbra.cs.redolog.op;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mime.ParsedContact;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class ModifyContact extends RedoableOp {

    private int mId;
    private Map<String, String> mFields;
    
    /** Used when this op is created from a <tt>ParsedContact</tt>. */
    private ParsedContact mParsedContact;

    /** Used when this op is read from the redolog. */
    private RedoableOpData mRedoLogContent;

    public ModifyContact() {
        super(MailboxOperation.ModifyContact);
        mId = UNKNOWN_ID;
    }

    public ModifyContact(int mailboxId, int id, ParsedContact pc) {
        this();
        setMailboxId(mailboxId);
        mId = id;
        mFields = pc.getFields();
        mParsedContact = pc;
    }

    @Override
    protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("id=");
        sb.append(mId);
        if (mFields != null && mFields.size() > 0) {
            sb.append(", attrs={");
            for (Map.Entry<String, String> entry : mFields.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append("\n    ").append(key).append(": ").append(value);
            }
            sb.append("\n}");
        }
        return sb.toString();
    }

    @Override
    protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mId);
//        out.writeBoolean(mReplace);
        int numAttrs = mFields != null ? mFields.size() : 0;
        out.writeShort((short) numAttrs);
        if (numAttrs > 0) {
            for (Map.Entry<String, String> entry : mFields.entrySet()) {
                out.writeUTF(entry.getKey());
                String value = entry.getValue();
                out.writeUTF(value != null ? value : "");
            }
        }
        if (getVersion().atLeast(1, 14)) {
            out.writeShort((short) -1);
            out.writeInt((int) mParsedContact.getSize());
        }
    }

    @Override
    public InputStream getAdditionalDataStream() throws IOException {
        if (getVersion().atLeast(1, 14)) {
            if (mParsedContact != null) {
                return mParsedContact.getContentStream();
            } else if (mRedoLogContent != null) {
                return mRedoLogContent.getInputStream();
            }
        }
        return null;
    }
    
    @Override
    protected void deserializeData(RedoLogInput in) throws IOException {
        mId = in.readInt();
        if (!getVersion().atLeast(1, 14))
            in.readBoolean();
        int numAttrs = in.readShort();
        if (numAttrs > 0) {
            mFields = new HashMap<String, String>(numAttrs);
            for (int i = 0; i < numAttrs; i++) {
                String key = in.readUTF();
                String value = in.readUTF();
                mFields.put(key, value);
            }
        }
        if (getVersion().atLeast(1, 14)) {
            in.readShort();
            int length = in.readInt();
            if (length > 0) {
                mRedoLogContent = new RedoableOpData(new File(in.getPath()), in.getFilePointer(), length);

                // Now that we have a stream to the data, skip to the next op.
                long pos = in.getFilePointer();
                int numSkipped = in.skipBytes(length);
                if (numSkipped != length) {
                    String msg = String.format("Attempted to skip %d bytes at position %d in %s, but actually skipped %d.",
                            length, pos, in.getPath(), numSkipped);
                    throw new IOException(msg);
                }
            }
        }
    }

    @Override
    public void redo() throws ServiceException {
        Mailbox mailbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
        
        InputStream in = null;
        try {
            in = getAdditionalDataStream();
            ParsedContact pc = new ParsedContact(mFields, in);
            mailbox.modifyContact(getOperationContext(), mId, pc);
        } catch (IOException e) {
            throw ServiceException.FAILURE("Redo error", e);
        } finally {
            ByteUtil.closeStream(in);
        }
    }
}
