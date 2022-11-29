// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.service.UserServletContext;
import com.zimbra.cs.service.UserServletException;
import com.zimbra.cs.service.formatter.FormatterFactory.FormatType;

public class TgzFormatter extends TarFormatter {
    @Override public String[] getDefaultMimeTypes() {
        return new String[] { "application/x-compressed-tar" };
    }

    @Override 
    public FormatType getType() { 
        return FormatType.TGZ;
    }
    
    protected ArchiveInputStream getInputStream(UserServletContext context,
        String charset) throws IOException, ServiceException, UserServletException {
        return new TarArchiveInputStream(new GZIPInputStream(
            context.getRequestInputStream(-1)), charset);
    }

    protected ArchiveOutputStream getOutputStream(UserServletContext context, String
        charset) throws IOException {
        return new TarArchiveOutputStream(new GZIPOutputStream(
            context.resp.getOutputStream()), charset);
    }
}
