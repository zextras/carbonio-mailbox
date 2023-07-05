// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import com.google.common.base.Charsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link ImapRequest}.
 *
 * @author ysasaki
 */
public final class ImapRequestTest {

 @Test
 void readNonAsciiAstring() throws Exception {
  NioImapRequest req = new NioImapRequest(null);
  String result = "\u65e5\u672c\u8a9e";
  String raw = new String(result.getBytes("ISO-2022-JP"), Charsets.ISO_8859_1);
  req.parse("\"" + raw.replace("\\", "\\\\") + "\"\r\n");
  assertEquals(result, req.readAstring(Charset.forName("ISO-2022-JP")));
 }

}
