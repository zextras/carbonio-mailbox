// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.mail.MessagingException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mime.Mime;

public class MessageDataSource implements DataSource {
    
    private Message mMessage;
    private String mContentType;

    public MessageDataSource(Message message)
    throws MessagingException, ServiceException {
        if (message == null) {
            throw new NullPointerException("message cannot be null");
        }
        mMessage = message;
        mContentType = message.getMimeMessage().getContentType();
    }
    
    public String getContentType() {
        return mContentType;
    }

    public InputStream getInputStream() throws IOException {
        try {
            return Mime.getInputStream(mMessage.getMimeMessage());
        } catch (Exception e) {
            ZimbraLog.mailbox.error("Unable to get stream to message " + mMessage.getId(), e);
            throw new IOException(e.toString());
        }
    }

    public String getName() {
        return mMessage.getSubject();
    }

    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }
}
