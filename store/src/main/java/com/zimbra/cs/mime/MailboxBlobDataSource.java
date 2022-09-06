// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Apr 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.zimbra.cs.mime;

import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StoreManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;

// TODO: Consolidate this class with com.zimbra.cs.store.MailboxBlobDataSource.
// Not doing it now to minimize impact on 6.0.x.
public class MailboxBlobDataSource implements DataSource {

  private MailboxBlob mBlob;
  private String mContentType;

  public MailboxBlobDataSource(MailboxBlob blob) {
    mBlob = blob;
  }

  public MailboxBlobDataSource(MailboxBlob blob, String ct) {
    this(blob);
    mContentType = ct;
  }

  public String getContentType() {
    if (mContentType != null) return mContentType;
    return "message/rfc822";
  }

  public InputStream getInputStream() throws IOException {
    return StoreManager.getInstance().getContent(mBlob);
  }

  public String getName() {
    // TODO should we just return null?
    return mBlob.toString();
  }

  public OutputStream getOutputStream() {
    throw new UnsupportedOperationException();
  }
}
