// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import com.zimbra.common.mime.MimeConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class ParsedMessageDataSource implements DataSource {
    
    private ParsedMessage mParsedMessage;
    
    public ParsedMessageDataSource(ParsedMessage pm) {
        if (pm == null) {
            throw new NullPointerException();
        }
        mParsedMessage = pm;
    }

    public String getContentType() {
        return MimeConstants.CT_MESSAGE_RFC822;
    }

    public InputStream getInputStream() throws IOException {
        return mParsedMessage.getRawInputStream();
    }

    public String getName() {
        return mParsedMessage.getSubject();
    }

    public OutputStream getOutputStream() throws IOException {
        throw new IOException("not supported");
    }
}
