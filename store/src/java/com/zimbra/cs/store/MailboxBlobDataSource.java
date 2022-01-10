// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.zimbra.common.mime.MimeConstants;

// TODO: Consolidate this class with com.zimbra.cs.mime.MailboxBlobDataSource.
// Not doing it now to minimize impact on 6.0.x. 
public class MailboxBlobDataSource implements DataSource {
    
    private MailboxBlob mBlob;
    
    public MailboxBlobDataSource(MailboxBlob blob) {
        if (blob == null) {
            throw new NullPointerException();
        }
        mBlob = blob;
    }

    public String getContentType() {
        return MimeConstants.CT_APPLICATION_OCTET_STREAM;
    }

    public InputStream getInputStream() throws IOException {
        return StoreManager.getInstance().getContent(mBlob);
    }

    public String getName() {
        return null;
    }

    public OutputStream getOutputStream() throws IOException {
        throw new IOException("not supported");
    }
}
