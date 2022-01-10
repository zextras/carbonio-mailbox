// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;

import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.FileSegmentDataSource;
import com.zimbra.common.mime.MimeConstants;

class RedoableOpData {
    private int mLength;
    private DataSource mDataSource;
    
    RedoableOpData(byte[] data) {
        mDataSource = new ByteArrayDataSource(data, MimeConstants.CT_APPLICATION_OCTET_STREAM);
        mLength = data.length;
    }
    
    RedoableOpData(File file) {
        mDataSource = new FileDataSource(file);
        mLength = (int) file.length();
    }
    
    RedoableOpData(File file, long offset, int length) {
        mDataSource = new FileSegmentDataSource(file, offset, length);
        mLength = length;
    }
    
    RedoableOpData(DataSource dataSource, int length) {
        mDataSource = dataSource;
        mLength = length;
    }
    
    int getLength() {
        return mLength;
    }
    
    byte[] getData()
    throws IOException {
        return ByteUtil.getContent(mDataSource.getInputStream(), mLength);
    }
    
    InputStream getInputStream()
    throws IOException {
        return mDataSource.getInputStream();
    }
}
