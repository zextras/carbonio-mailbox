// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import java.io.IOException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.service.UserServletContext;
import com.zimbra.cs.service.UserServletException;
import com.zimbra.cs.service.formatter.FormatterFactory.FormatType;

public class TarFormatter extends ArchiveFormatter {
    @Override
    public String[] getDefaultMimeTypes() {
        return new String[] { "application/x-tar" };
    }

    @Override
    public FormatType getType() {
        return FormatType.TAR;
     }

    @Override
    protected ArchiveInputStream getInputStream(UserServletContext context,
        String charset) throws IOException, ServiceException, UserServletException {

        return new TarArchiveInputStream(context.getRequestInputStream(-1),
            charset);
    }

    @Override
    protected ArchiveOutputStream getOutputStream(UserServletContext context, String
        charset) throws IOException {
        return new TarArchiveOutputStream(context.resp.getOutputStream(), charset);
    }
}
