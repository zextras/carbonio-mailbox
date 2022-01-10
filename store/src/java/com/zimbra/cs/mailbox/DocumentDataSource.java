// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;


public class DocumentDataSource implements DataSource {

    private Document mDocument;
    
    public DocumentDataSource(Document document) {
        if (document == null) {
            throw new NullPointerException("document cannot be null");
        }
        mDocument = document;
    }
    
    public String getContentType() {
        return mDocument.getContentType();
    }

    public InputStream getInputStream() throws IOException {
        try {
            return mDocument.getContentStream();
        } catch (ServiceException e) {
            ZimbraLog.mailbox.error("Unable to get document content", e);
            throw new IOException(e.toString());
        }
    }

    public String getName() {
        return mDocument.getName();
    }

    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }

}
