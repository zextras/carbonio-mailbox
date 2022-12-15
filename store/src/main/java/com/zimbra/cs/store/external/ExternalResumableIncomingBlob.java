// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import java.io.IOException;
import java.io.OutputStream;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.BufferingIncomingBlob;

/**
 * IncomingBlob implementation which streams data directly to external store during upload
 * The store must support resumable upload, otherwise it should use the default BufferingIncomingBlob implementation
 *
 */
public abstract class ExternalResumableIncomingBlob extends BufferingIncomingBlob {

    public ExternalResumableIncomingBlob(String id, BlobBuilder blobBuilder, Object ctx) throws ServiceException, IOException {
        super(id, blobBuilder, ctx);
    }

    @Override
    public OutputStream getAppendingOutputStream() throws IOException {
        lastAccessTime = System.currentTimeMillis();
        return getAppendingOutputStream(blobBuilder);
    }

    @Override
    public long getCurrentSize() throws IOException {
        long internalSize = super.getCurrentSize();
        long remoteSize = getRemoteSize();
        if (remoteSize != internalSize) {
            throw new IOException("mismatch between local (" + internalSize + ") and remote (" + remoteSize + ") " +
                "content sizes. Client must restart upload", null);
        } else {
            return internalSize;
        }
    }

    @Override
    public Blob getBlob() throws IOException, ServiceException {
        return new ExternalUploadedBlob(blobBuilder.finish(), id);
    }

    /**
     * Retrieve an OutputStream which can be used to write data to the remote upload location
     * @param blobBuilder: Used to create local Blob instance inline with upload. Must be passed to super constructor
     * @return ExternalResumableOutputStream instance which can write data to the upload session/location encapsulated by this IncomingBlob instance
     * @throws IOException
     */
    protected abstract ExternalResumableOutputStream getAppendingOutputStream(BlobBuilder blobBuilder) throws IOException;

    /**
     * Query the remote store for the size of the upload received so far. Used for consistency checking during resume
     * @return: The number of bytes which have been stored remotely.
     * @throws IOException
     * @throws ServiceException
     */
    protected abstract long getRemoteSize() throws IOException;
}
