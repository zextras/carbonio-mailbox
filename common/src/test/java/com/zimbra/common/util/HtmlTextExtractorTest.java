// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.Assert.*;

import java.io.StringReader;
import org.junit.Test;

public class HtmlTextExtractorTest {

  @Test
  public void extract() throws Exception {
    String html =
        "<html><head>"
            + "<title>Where It's At</title>"
            + "<style>style</style>"
            + "<script>script</script>"
            + "</head>"
            + "<body>I got two turntables and a microphone.</body></html>";
    String text = HtmlTextExtractor.extract(new StringReader(html), Integer.MAX_VALUE);
    assertTrue(text.contains("Where"));
    assertTrue(text.contains("microphone"));
    assertFalse(text.contains("script"));
    assertFalse(text.contains("style"));
  }
}
