// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * Wraps another <tt>DataSource</tt> and allows the caller to
 * override the content type and name.
 */
public class DataSourceWrapper implements DataSource {

    private final DataSource ds;
    private String ctype;
    private String name;

    public DataSourceWrapper(DataSource dataSource) {
        if (dataSource == null) {
            throw new NullPointerException("dataSource cannot be null");
        }
        this.ds = dataSource;
    }

    public DataSourceWrapper setContentType(String contentType) {
        this.ctype = contentType;
        return this;
    }

    public DataSourceWrapper setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getContentType() {
        String ct = ctype != null ? ctype : ds.getContentType();
        return new ContentType(ct).cleanup().toString();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return ds.getInputStream();
    }

    @Override
    public String getName() {
        return name != null ? name : ds.getName();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return ds.getOutputStream();
    }
}
