// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.io.StringReader;
import java.util.Collections;

import org.apache.lucene.analysis.TokenFilter;
import org.junit.jupiter.api.Test;

import com.zimbra.cs.index.ZimbraAnalyzerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link ContactTokenFilter}.
 *
 * @author ysasaki
 */
public class ContactTokenFilterTest {

 @Test
 void contactDataFilter() throws Exception {
  AddrCharTokenizer tokenizer = new AddrCharTokenizer(new StringReader("all-snv"));
  TokenFilter filter = new ContactTokenFilter(tokenizer);
  assertEquals(Collections.singletonList("all-snv"),
    ZimbraAnalyzerTest.toTokens(filter));

  tokenizer.reset(new StringReader("."));
  assertEquals(Collections.EMPTY_LIST,
    ZimbraAnalyzerTest.toTokens(filter));

  tokenizer.reset(new StringReader(".. ."));
  assertEquals(Collections.singletonList(".."),
    ZimbraAnalyzerTest.toTokens(filter));

  tokenizer.reset(new StringReader(".abc"));
  assertEquals(Collections.singletonList(".abc"),
    ZimbraAnalyzerTest.toTokens(filter));

  tokenizer.reset(new StringReader("a"));
  assertEquals(Collections.singletonList("a"),
    ZimbraAnalyzerTest.toTokens(filter));

  tokenizer.reset(new StringReader("test.com"));
  assertEquals(Collections.singletonList("test.com"),
    ZimbraAnalyzerTest.toTokens(filter));

  tokenizer.reset(new StringReader("user1@zim"));
  assertEquals(Collections.singletonList("user1@zim"),
    ZimbraAnalyzerTest.toTokens(filter));

  tokenizer.reset(new StringReader("user1@zimbra.com"));
  assertEquals(Collections.singletonList("user1@zimbra.com"),
    ZimbraAnalyzerTest.toTokens(filter));
 }

}
