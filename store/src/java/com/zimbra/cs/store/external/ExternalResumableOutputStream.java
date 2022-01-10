// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import java.io.IOException;

import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.BlobBuilderOutputStream;

/**
 * OutputStream used to write to an external store during resumable upload.
 *
 */
public abstract class ExternalResumableOutputStream extends BlobBuilderOutputStream {

    protected ExternalResumableOutputStream(BlobBuilder blobBuilder) {
        super(blobBuilder);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        writeToExternal(b, off, len);
        super.write(b, off, len);
    }

    /**
     * Append data to remote upload location
     * @param b: byte array holding the data to upload
     * @param off: offset to start the upload from
     * @param len: length of the data to copy from the byte array
     * @throws IOException
     */
    protected abstract void writeToExternal(byte[] b, int off, int len) throws IOException;
}
