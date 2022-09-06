// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;

import com.zimbra.cs.mailclient.ParseException;
import java.io.IOException;

public final class CopyResult {
  private final long uidValidity;
  private final long[] fromUids;
  private final long[] toUids;

  public static CopyResult parse(ImapInputStream is) throws IOException {
    is.skipChar(' ');
    long uidValidity = is.readNZNumber();
    is.skipChar(' ');
    is.skipSpaces();
    String fromSet = is.readText(" ");
    is.skipChar(' ');
    is.skipSpaces();
    String toSet = is.readText(" ]");
    try {
      return new CopyResult(uidValidity, fromSet, toSet);
    } catch (IllegalArgumentException e) {
      throw new ParseException("Invalid COPYUID result");
    }
  }

  private CopyResult(long uidValidity, String fromUidSet, String toUidSet) {
    this.uidValidity = uidValidity;
    fromUids = ImapUtil.parseUidSet(fromUidSet);
    toUids = ImapUtil.parseUidSet(toUidSet);
    if (fromUids.length != toUids.length) {
      throw new IllegalArgumentException();
    }
  }

  public long getUidValidity() {
    return uidValidity;
  }

  public long[] getFromUids() {
    return fromUids;
  }

  public long[] getToUids() {
    return toUids;
  }
}
