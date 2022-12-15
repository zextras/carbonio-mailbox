// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream which writes to a BlobBuilder
 *
 */
public class BlobBuilderOutputStream extends OutputStream {
    protected BlobBuilder blobBuilder;

    protected BlobBuilderOutputStream(BlobBuilder blobBuilder) {
        super();
        this.blobBuilder = blobBuilder;
    }

    @Override
    public void write(int b) throws IOException {
        // inefficient, but we don't expect this to be used
        byte[] tmp = new byte[1];
        tmp[0] = (byte) b;
        this.write(tmp);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        blobBuilder.append(b, off, len);
    }
}
