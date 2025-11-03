// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import com.zimbra.common.service.ServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public interface BlobBuilder<T extends Blob> {

    long getSizeHint();

    long getTotalBytes();

    /**
     * This method is called by the redolog code, so that we don't double-compress
     * blobs that are already stored in compressed format in the redolog.  In this
     * case we write the data directly to disk and don't calculate the size or digest.
     */
    BlobBuilder<T> disableCompression(boolean disable);

    int getCompressionThreshold();

    BlobBuilder<T> disableDigest(boolean disable);
    BlobBuilder<T> init() throws IOException, ServiceException;

    BlobBuilder<T> append(InputStream in) throws IOException;

    BlobBuilder<T> append(byte[] b) throws IOException;

    BlobBuilder<T> append(byte[] b, int off, int len) throws IOException;

    BlobBuilder<T> append(ByteBuffer bb) throws IOException;

    @SuppressWarnings("unused")
    T finish() throws IOException, ServiceException;

    T getBlob();

    boolean isFinished();

    // Clean up and dispose of blob file
    void dispose();
}
