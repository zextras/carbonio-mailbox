// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Dec 9, 2004
 */
package com.zimbra.cs.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.zimbra.common.mime.ContentType;
import com.zimbra.cs.service.FileUploadServlet.Upload;

/**
 * @author dkarp
 */
public class UploadDataSource implements DataSource {

    private Upload mUpload;
    private ContentType mContentType;

    public UploadDataSource(Upload up) {
        mUpload = up;
    }

    public void setContentType(ContentType ctype) {
        mContentType = ctype;
    }

    @Override public String getContentType() {
        if (mContentType == null) {
            return new ContentType(mUpload.getContentType()).cleanup().toString();
        } else {
            return mContentType.cleanup().toString();
        }
    }

    @Override public InputStream getInputStream() throws IOException {
        return mUpload.getInputStream();
    }

    @Override public String getName() {
        return mUpload.getName();
    }

    @Override public OutputStream getOutputStream() {
        return null;
    }
}
