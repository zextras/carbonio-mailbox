// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2005. 6. 7.
 */
package com.zimbra.cs.store;

import io.vavr.collection.Array;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

import javax.activation.DataSource;
import org.apache.commons.io.FileUtils;

import com.google.common.base.MoreObjects;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.FileUtil;
import com.zimbra.common.zmime.ZSharedFileInputStream;

/**
 * Represents a blob in blob store incoming directory.  An incoming blob
 * does not belong to any mailbox.  When a message is delivered to a mailbox,
 * message is saved in the incoming directory and a link to it is created
 * in the mailbox's directory.  The linked blob in mailbox directory
 * is represented by a MailboxBlob object.
 */
public abstract class Blob {
    private Boolean compressed = null;
    private String digest;
    protected Long rawSize;

    public String getPath() {
        return path;
    }

    private final String path;

    public abstract InputStream getInputStream() throws IOException;
    public abstract byte[] getContent() throws IOException;

    protected Blob(String path) {
        this.path = path;
        // TODO: check if it is really required to have a file, else we need to define another structure
    }

    protected Blob(String path, long rawSize, String digest) {
        this(path);
        this.rawSize = rawSize;
        this.digest = digest;
    }

    public void copy(Blob blob) throws IOException {
        setDigest(blob.getDigest());
        setRawSize(blob.getRawSize());
    }



    /** Returns the SHA-256 digest of this blob's uncompressed data,
     *  encoded in base64. */
    public String getDigest() throws IOException {
        if (digest == null) {
            initializeSizeAndDigest();
        }
        return digest;
    }

    /** Returns the size of the blob's data.  If the blob is compressed,
     *  returns the uncompressed size. */
    public abstract long getRawSize() throws IOException;

    protected void initializeSizeAndDigest() throws IOException {
        InputStream in = null;
        try {
            // Get the stream using the local method.  FileBlobStore.getContent()
            // can call getDigest(), which could result in an infinite loop.
            in = getInputStream();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[1024];
            int numBytes;
            long totalBytes = 0;
            while ((numBytes = in.read(buffer)) >= 0) {
                md.update(buffer, 0, numBytes);
                totalBytes += numBytes;
            }
            this.digest = ByteUtil.encodeFSSafeBase64(md.digest());
            this.rawSize = totalBytes;
        } catch (NoSuchAlgorithmException e) {
            // this should never happen unless the JDK is foobar
            //  e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            ByteUtil.closeStream(in);
        }
    }

    public Blob setCompressed(final boolean isCompressed) {
        this.compressed = isCompressed;
        return this;
    }

    public Blob setDigest(final String digest) {
        this.digest = digest;
        return this;
    }

    public Blob setRawSize(final long rawSize) {
        this.rawSize = rawSize;
        return this;
    }


    public Blob copyCachedDataFrom(final Blob other) {
        if (compressed == null && other.compressed != null) {
            this.compressed = other.compressed;
        }
        if (digest == null && other.digest != null) {
            this.digest = other.digest;
        }
        if (rawSize == null && other.rawSize != null) {
            this.rawSize = other.rawSize;
        }
        return this;
    }
//
    // Checkme: was unused
//    public void renameTo(final String newPath) throws IOException {
//        if (!path.equals(newPath)) {
//            File newFile = new File(newPath);
//            FileUtils.moveFile(file, newFile);
//            this.path = newPath;
//            this.file = newFile;
//        }
//    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("path", path)
            .add("size", rawSize)
            .add("compressed", compressed).toString();
    }

    public abstract String getName();
}
