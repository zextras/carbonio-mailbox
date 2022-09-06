// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;

import com.zimbra.cs.mailclient.ParseException;
import java.io.IOException;

public final class AppendResult {
  private final long uidValidity;
  private final long[] uids;

  // UIDPLUS (RFC 2359):
  // resp_code_apnd ::= "APPENDUID" SPACE nz_number SPACE set
  public static AppendResult parse(ImapInputStream is) throws IOException {
    is.skipChar(' ');
    long uidValidity = is.readNZNumber();
    is.skipChar(' ');
    is.skipSpaces();
    String uidSet = is.readText(" ]");
    try {
      return new AppendResult(uidValidity, uidSet);
    } catch (IllegalArgumentException e) {
      throw new ParseException("Invalid APPENDUID result");
    }
  }

  private AppendResult(long uidValidity, String uidSet) {
    this.uidValidity = uidValidity;
    this.uids = ImapUtil.parseUidSet(uidSet);
  }

  public long getUidValidity() {
    return uidValidity;
  }

  public long[] getUids() {
    return uids;
  }

  public long getUid() {
    return uids[0];
  }
}
