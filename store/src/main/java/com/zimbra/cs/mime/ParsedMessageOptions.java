// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import java.io.File;
import java.io.IOException;

import javax.mail.internet.MimeMessage;

import com.zimbra.cs.store.Blob;

public class ParsedMessageOptions {

    private MimeMessage mMimeMessage;
    private byte[] mRawData;
    private File mFile;
    private String mRawDigest;
    private Long mRawSize;
    private Long mReceivedDate;
    private Boolean mIndexAttachments;

    public ParsedMessageOptions() {
    }

    public ParsedMessageOptions(Blob blob, byte[] buffer) throws IOException {
        this(blob, buffer, null, null);
    }

    public ParsedMessageOptions(Blob blob, byte[] buffer, Long receivedDate,
        Boolean indexAttachments) throws IOException {
        if (buffer == null)
            setContent(blob.getFile());
        else
            setContent(buffer);
        setDigest(blob.getDigest()).setSize(blob.getRawSize());
        if (receivedDate != null)
            setReceivedDate(receivedDate);
        if (indexAttachments != null)
            setAttachmentIndexing(indexAttachments);
    }

    public ParsedMessageOptions setContent(MimeMessage mimeMessage) {
        if (mRawData != null || mFile != null) {
            throw new IllegalArgumentException("Content can only come from one source.");
        }
        mMimeMessage = mimeMessage;
        return this;
    }

    public ParsedMessageOptions setContent(byte[] rawData) {
        if (mMimeMessage != null || mFile != null) {
            throw new IllegalArgumentException("Content can only come from one source.");
        }
        mRawData = rawData;
        return this;
    }

    public ParsedMessageOptions setContent(File file) {
        if (mRawData != null || mMimeMessage != null) {
            throw new IllegalArgumentException("Content can only come from one source.");
        }
        mFile = file;
        return this;
    }

    public ParsedMessageOptions setDigest(String digest) {
        mRawDigest = digest;
        return this;
    }

    public ParsedMessageOptions setSize(long size) {
        mRawSize = size;
        return this;
    }

    public ParsedMessageOptions setReceivedDate(long receivedDate) {
        mReceivedDate = receivedDate;
        return this;
    }

    public ParsedMessageOptions setAttachmentIndexing(boolean enabled) {
        mIndexAttachments = enabled;
        return this;
    }

    public MimeMessage getMimeMessage() { return mMimeMessage; }
    public byte[] getRawData() { return mRawData; }
    public File getFile() { return mFile; }
    public String getDigest() { return mRawDigest; }
    public Long getSize() { return mRawSize; }
    public Long getReceivedDate() { return mReceivedDate; }
    public Boolean getAttachmentIndexing() { return mIndexAttachments; }
}
