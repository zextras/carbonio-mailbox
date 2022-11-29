// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Apr 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.zimbra.cs.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.StoreManager;

public class BlobDataSource implements DataSource {

    private Blob mBlob;
    private String mContentType;

    public BlobDataSource(Blob blob) {
        mBlob = blob;
    }

    public BlobDataSource(Blob blob, String ct) {
        this(blob);
        mContentType = ct;
    }

    @Override
    public String getContentType() {
        if (mContentType != null)
            return mContentType;
        return "message/rfc822";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return StoreManager.getInstance().getContent(mBlob);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }
}
