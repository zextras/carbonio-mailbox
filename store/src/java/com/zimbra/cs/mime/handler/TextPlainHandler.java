// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.activation.DataSource;

import org.apache.lucene.document.Document;

import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.convert.AttachmentInfo;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.mime.MimeHandler;
import com.zimbra.cs.mime.MimeHandlerException;
import com.zimbra.cs.mime.MimeHandlerManager;

/**
 * {@link MimeHandler} that creates a Lucene document from a {@code text/plain} part.
 *
 * @since Apr 1, 2004
 * @author schemers
 */
public class TextPlainHandler extends MimeHandler {

    private String content;

    @Override
    protected boolean runsExternally() {
        return false;
    }

    @Override
    public void addFields(Document doc) {
        // we add no type-specific fields to the doc
    }

    @Override
    protected String getContentImpl() throws MimeHandlerException {
        if (content == null) {
            DataSource source = getDataSource();
            if (source != null) {
                String ctype = source.getContentType();
                InputStream is = null;
                try {
                    Reader reader = Mime.getTextReader(is = source.getInputStream(), ctype, getDefaultCharset());
                    content = ByteUtil.getContent(reader, MimeHandlerManager.getIndexedTextLimit(), false);
                } catch (IOException e) {
                    throw new MimeHandlerException(e);
                } finally {
                    ByteUtil.closeStream(is);
                }
            }
        }
        if (content == null) {
            content = "";
        }
        return content;
    }

    /** No need to convert plain text document ever. */
    @Override
    public boolean doConversion() {
        return false;
    }

    @Override
    public String convert(AttachmentInfo doc, String baseURL) {
        throw new UnsupportedOperationException();
    }

}
