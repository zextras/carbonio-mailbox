// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

import org.apache.lucene.analysis.Tokenizer;
import org.junit.jupiter.api.Test;

import com.zimbra.cs.index.ZimbraAnalyzerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link AddrCharTokenizer}.
 *
 * @author ysasaki
 */
public class AddrCharTokenizerTest {

 @Test
 void addrCharTokenizer() throws Exception {
  Tokenizer tokenizer = new AddrCharTokenizer(new StringReader("all-snv"));
  assertEquals(Collections.singletonList("all-snv"), ZimbraAnalyzerTest.toTokens(tokenizer));

  tokenizer.reset(new StringReader("."));
  assertEquals(Collections.singletonList("."), ZimbraAnalyzerTest.toTokens(tokenizer));

  tokenizer.reset(new StringReader(".. ."));
  assertEquals(Arrays.asList("..", "."), ZimbraAnalyzerTest.toTokens(tokenizer));

  tokenizer.reset(new StringReader(".abc"));
  assertEquals(Collections.singletonList(".abc"), ZimbraAnalyzerTest.toTokens(tokenizer));

  tokenizer.reset(new StringReader("a"));
  assertEquals(Collections.singletonList("a"), ZimbraAnalyzerTest.toTokens(tokenizer));

  tokenizer.reset(new StringReader("test.com"));
  assertEquals(Collections.singletonList("test.com"), ZimbraAnalyzerTest.toTokens(tokenizer));

  tokenizer.reset(new StringReader("user1@zim"));
  assertEquals(Collections.singletonList("user1@zim"), ZimbraAnalyzerTest.toTokens(tokenizer));

  tokenizer.reset(new StringReader("user1@zimbra.com"));
  assertEquals(Collections.singletonList("user1@zimbra.com"), ZimbraAnalyzerTest.toTokens(tokenizer));
 }

 /**
  * Bug 79103 tab was getting included at start of a token instead of being ignored.
  */
 @Test
 void multiLineWithTabs() throws Exception {
  Tokenizer tokenizer = new AddrCharTokenizer(
    new StringReader("one name <one@example.net>\n\ttwo <two@example.net>"));
  assertEquals(Arrays.asList("one", "name", "one@example.net", "two", "two@example.net"),
    ZimbraAnalyzerTest.toTokens(tokenizer),
    "Token list");
 }

 @Test
 void japanese() throws Exception {
  Tokenizer tokenizer = new AddrCharTokenizer(new StringReader("\u68ee\u3000\u6b21\u90ce"));
  assertEquals(Arrays.asList("\u68ee", "\u6b21\u90ce"), ZimbraAnalyzerTest.toTokens(tokenizer));
 }

}
