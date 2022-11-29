// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Charsets;

/**
 * Unit test for {@link ImapRequest}.
 *
 * @author ysasaki
 */
public final class ImapRequestTest {

    @Test
    public void readNonAsciiAstring() throws Exception {
        NioImapRequest req = new NioImapRequest(null);
        String result = "\u65e5\u672c\u8a9e";
        String raw = new String(result.getBytes("ISO-2022-JP"), Charsets.ISO_8859_1);
        req.parse("\"" + raw.replace("\\", "\\\\") + "\"\r\n");
        Assert.assertEquals(result, req.readAstring(Charset.forName("ISO-2022-JP")));
    }

}
