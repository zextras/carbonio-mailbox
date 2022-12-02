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
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.util.TagUtil;
import com.zimbra.cs.mime.ParsedContact;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class CreateContact extends RedoableOp {

    private int mId;
    private int mFolderId;
    private Map<String, String> mFields;

    /** Used when this op is created from a <tt>ParsedContact</tt>. */
    private ParsedContact mParsedContact;

    /** Used when this op is read from the redolog. */
    private RedoableOpData mRedoLogContent;

    private String[] mTags;
    private String mTagIds;

    public CreateContact() {
        super(MailboxOperation.CreateContact);
        mId = UNKNOWN_ID;
        mFolderId = UNKNOWN_ID;
    }

    public CreateContact(int mailboxId, int folderId, ParsedContact pc, String[] tags) {
        this();
        setMailboxId(mailboxId);
        mId = UNKNOWN_ID;
        mFolderId = folderId;
        mFields = pc.getFields();
        mParsedContact = pc;
        mTags = tags != null ? tags : new String[0];
    }

    public void setContactId(int id) {
        mId = id;
    }

    public int getContactId() {
        return mId;
    }

    @Override
    protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("folder=").append(mFolderId);
        sb.append(", tags=\"").append(TagUtil.encodeTags(mTags)).append("\"");
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
        out.writeInt(mFolderId);
        out.writeShort((short) -1);
        if (getVersion().atLeast(1, 33)) {
            out.writeUTFArray(mTags);
        } else {
            out.writeUTF(mTagIds);
        }
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
            out.writeInt((int) mParsedContact.getSize());
        }
    }


    @Override public InputStream getAdditionalDataStream() throws IOException {
        if (getVersion().atLeast(1, 14)) {
            if (mParsedContact != null) {
                return mParsedContact.getContentStream();
            } else if (mRedoLogContent != null) {
                return mRedoLogContent.getInputStream();
            }
        }
        return null;
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        mId = in.readInt();
        mFolderId = in.readInt();
        in.readShort();
        if (getVersion().atLeast(1, 33)) {
            mTags = in.readUTFArray();
        } else {
            mTagIds = in.readUTF();
        }
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
            int length = in.readInt();
            if (length > StoreIncomingBlob.MAX_BLOB_SIZE)
                throw new IOException("deserialized message size too large (" + length + " bytes)");
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

    @Override public void redo() throws Exception {
        int mboxId = getMailboxId();
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(mboxId);
        OperationContext octxt = getOperationContext();

        if (mTags == null && mTagIds != null) {
            mTags = TagUtil.tagIdStringToNames(mbox, octxt, mTagIds);
        }

        InputStream in = null;
        try {
            in = getAdditionalDataStream();
            ParsedContact pc = new ParsedContact(mFields, in);
            mbox.createContact(octxt, pc, mFolderId, mTags);
        } catch (ServiceException e) {
            String code = e.getCode();
            if (code.equals(MailServiceException.ALREADY_EXISTS)) {
                mLog.info("Contact %d already exists in mailbox %d", mId, mboxId);
            } else {
                throw e;
            }
        } finally {
            ByteUtil.closeStream(in);
        }
    }
}
