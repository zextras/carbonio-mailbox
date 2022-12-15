// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.zimbra.common.service.ServiceException;

/**
 * IncomingBlob implementation which buffers locally using BlobBuilder
 *
 */
public class BufferingIncomingBlob extends IncomingBlob
{
    protected final String id;
    protected BlobBuilder blobBuilder;
    private Object ctx;
    private long expectedSize;
    private boolean expectedSizeSet;
    protected long lastAccessTime;

    protected BufferingIncomingBlob(String id, BlobBuilder blobBuilder, Object ctx)
    {
        this.id = id;
        this.blobBuilder = blobBuilder;
        this.ctx = ctx;

        lastAccessTime = System.currentTimeMillis();
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public OutputStream getAppendingOutputStream() throws IOException
    {
        lastAccessTime = System.currentTimeMillis();
        return new BlobBuilderOutputStream(blobBuilder);
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        assert false : "Not yet implemented";
        return null;
        //return new SizeLimitedInputStream(blobBuilder.getBlob().getInputStream(), getCurrentSize());
    }

    @Override
    public Object getContext()
    {
        return ctx;
    }

    @Override
    public void setContext(Object value)
    {
        ctx = value;
    }

    @Override
    public long getCurrentSize() throws IOException
    {
        return blobBuilder.getTotalBytes();
    }

    @Override
    public boolean hasExpectedSize()
    {
        return expectedSizeSet;
    }

    @Override
    public long getExpectedSize()
    {
        assert expectedSizeSet : "Expected size not set";
        return expectedSize;
    }

    @Override
    public void setExpectedSize(long value)
    {
        assert !expectedSizeSet : "Expected size already set: " + expectedSize;
        expectedSize = value;
        expectedSizeSet = true;
    }

    @Override
    public long getLastAccessTime()
    {
        return lastAccessTime;
    }

    @Override
    public Blob getBlob() throws IOException, ServiceException {
        return blobBuilder.finish();
    }

    @Override
    public void cancel() {
        blobBuilder.dispose();
    }
}
