// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;

import com.zimbra.common.util.Log;
import com.zimbra.cs.mailclient.MailOutputStream;
import java.io.OutputStream;

/** An output stream for writing IMAP request data. */
public class ImapOutputStream extends MailOutputStream {
  public ImapOutputStream(OutputStream os) {
    super(os);
  }

  public ImapOutputStream(OutputStream os, Log log) {
    super(os, log);
  }
}
