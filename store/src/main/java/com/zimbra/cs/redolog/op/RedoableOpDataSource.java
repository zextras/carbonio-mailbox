// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.op;

import com.zimbra.common.mime.MimeConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;

public class RedoableOpDataSource implements DataSource {

  private RedoableOpData mData;

  public RedoableOpDataSource(RedoableOpData data) {
    if (data == null) {
      throw new NullPointerException();
    }
    mData = data;
  }

  public String getContentType() {
    return MimeConstants.CT_APPLICATION_OCTET_STREAM;
  }

  public InputStream getInputStream() throws IOException {
    return mData.getInputStream();
  }

  public String getName() {
    return null;
  }

  public OutputStream getOutputStream() throws IOException {
    throw new IOException("not supported");
  }
}
