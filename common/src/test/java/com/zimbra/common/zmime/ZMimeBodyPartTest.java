// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.zmime;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.common.util.ByteUtil;

public class ZMimeBodyPartTest {
    @BeforeClass
    public static void init() {
        // throw this here so that it gets set in the static initializers of MimeBodyPart
        System.setProperty("mail.mime.ignoremultipartencoding", "false");
    }

    @Test
    public void setStringContent() throws Exception {
        String content = "these are the times\nthat try men's souls";
        ZMimeBodyPart mbp = new ZMimeBodyPart();

        mbp.setContent(content, "text/plain");
        Assert.assertArrayEquals(content.getBytes(), ByteUtil.getContent(mbp.getInputStream(), -1));

        mbp.setText(content);
        Assert.assertArrayEquals(content.getBytes(), ByteUtil.getContent(mbp.getInputStream(), -1));

        mbp.setContent(content, "text/plain; charset=utf-8");
        Assert.assertArrayEquals(content.getBytes("utf-8"), ByteUtil.getContent(mbp.getInputStream(), -1));

        mbp.setText(content, "utf-8");
        Assert.assertArrayEquals(content.getBytes("utf-8"), ByteUtil.getContent(mbp.getInputStream(), -1));

        mbp.setHeader("Content-Type", "xml/x-share");
        Assert.assertArrayEquals(content.getBytes(), ByteUtil.getContent(mbp.getInputStream(), -1));

        mbp.writeTo(System.out);
    }

    @Test
    public void shareContent() throws Exception {
        String content = "\u30d6\u30ea\u30fc\u30d5\u30b1\u30fc\u30b9";
        ZMimeBodyPart mbp = new ZMimeBodyPart();

        mbp.setContent(content, "xml/x-share;charset=ISO-2022-JP");
        mbp.setHeader("Content-Transfer-Encoding", "base64");
        mbp.updateHeaders();
        mbp.writeTo(System.out);
        Assert.assertEquals(ZTransferEncoding.BASE64.toString(), mbp.getHeader("Content-Transfer-Encoding", null));
        Assert.assertArrayEquals(content.getBytes("ISO-2022-JP"), ByteUtil.getContent(mbp.getInputStream(), -1));

    }

    private void testEncodingSelection(String msg, String content, ZTransferEncoding cteExpected) throws Exception {
        ZMimeBodyPart mbp = new ZMimeBodyPart();
        mbp.setText(content);
        mbp.updateHeaders();
        Assert.assertEquals(msg, cteExpected.toString(), mbp.getHeader("Content-Transfer-Encoding", null));
    }

    @Test
    public void encoding() throws Exception {
        testEncodingSelection("empty content", "", ZTransferEncoding.SEVEN_BIT);
        testEncodingSelection("short content", "these are the days", ZTransferEncoding.SEVEN_BIT);
        testEncodingSelection("multiline content", "who\r\nare\r\nyou?", ZTransferEncoding.SEVEN_BIT);

        testEncodingSelection("single non-ASCII", "cyb\u00c8le illus.", ZTransferEncoding.QUOTED_PRINTABLE);
        testEncodingSelection("all non-ASCII", "\u00c8", ZTransferEncoding.EIGHT_BIT);
        testEncodingSelection("single NUL", "cyb\u0000le illus.", ZTransferEncoding.QUOTED_PRINTABLE);
        testEncodingSelection("single control char", "ESCAPE ME \u001B", ZTransferEncoding.SEVEN_BIT);

        testEncodingSelection("line too long (ascii)", StringUtils.leftPad("", 1000, "X"), ZTransferEncoding.QUOTED_PRINTABLE);
        testEncodingSelection("line too long (JIS)", new String(StringUtils.leftPad("", 1000, "あ").getBytes("ISO-2022-JP")), ZTransferEncoding.QUOTED_PRINTABLE);
        testEncodingSelection("line too long (utf-8)", new String(StringUtils.leftPad("", 1000, "あ").getBytes("UTF-8")), ZTransferEncoding.BASE64);
        testEncodingSelection("line too long (shift-jis)", new String(StringUtils.leftPad("", 1000, "あ").getBytes("Shift-JIS")), ZTransferEncoding.BASE64);
    }
}
