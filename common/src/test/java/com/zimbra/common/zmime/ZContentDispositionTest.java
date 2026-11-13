// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.zmime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ZContentDispositionTest {

  @Test
  void decodesQuotedEncodedValue() {
    ZContentDisposition disposition =
        new ZContentDisposition("attachment; filename*=\"UTF-8''%D1%82%D0%B5%D1%81%D1%82.txt\"; foo=bar");

    assertEquals("\u0442\u0435\u0441\u0442.txt", disposition.getParameter("filename"));
    assertEquals("bar", disposition.getParameter("foo"));
  }

  @Test
  void decodesQuotedEncodedContinuations() {
    ZContentDisposition disposition =
        new ZContentDisposition(
            """
                attachment;
                 filename*0*="UTF-8''%D0%A2%D0%B5%D1%81%D1%82%D0%BE%D0%B2%D1%8B%D0%B9%20";
                 filename*1*="%D0%B4%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%2Edoc";
                 filename*2*="%78\"""");

    assertEquals(
        "\u0422\u0435\u0441\u0442\u043e\u0432\u044b\u0439 \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442.docx",
        disposition.getParameter("filename"));
  }

  @Test
  void decodesQuotedAndUnquotedContinuations() {
    ZContentDisposition disposition =
        new ZContentDisposition("attachment; filename*0*=UTF-8''%D0%A2; filename*1*=\"%D0%B4\"");

    assertEquals("\u0422\u0434", disposition.getParameter("filename"));
  }

  @Test
  void decodesQuotedValueWithoutCharset() {
    ZContentDisposition disposition = new ZContentDisposition("attachment; foo*=\"big%20dog\"; bar=baz");

    assertEquals("big dog", disposition.getParameter("foo"));
    assertEquals("baz", disposition.getParameter("bar"));
  }

  @Test
  void endsQuotedValueAfterLanguage() {
    ZContentDisposition disposition = new ZContentDisposition("attachment; foo*=\"UTF-8'en\"; bar=baz");

    assertEquals("", disposition.getParameter("foo"));
    assertEquals("baz", disposition.getParameter("bar"));
  }
}
