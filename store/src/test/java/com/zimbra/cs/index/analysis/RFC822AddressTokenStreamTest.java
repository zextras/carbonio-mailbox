// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.util.Arrays;

import org.apache.lucene.analysis.TokenStream;
import org.junit.jupiter.api.Test;

import com.google.common.base.Strings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.cs.index.ZimbraAnalyzerTest;

/**
 * Unit test for {@link RFC822AddressTokenStream}.
 *
 * @author ysasaki
 */
public final class RFC822AddressTokenStreamTest {

 @Test
 void single() throws Exception {
  TokenStream stream = new RFC822AddressTokenStream("user@domain.com");
  assertEquals(Arrays.asList("user@domain.com", "user", "@domain.com", "domain.com", "domain", "@domain"),
    ZimbraAnalyzerTest.toTokens(stream));

  stream = new RFC822AddressTokenStream("\"Tim Brown\" <first.last@sub.domain.com>");
  assertEquals(Arrays.asList("tim", "brown", "first.last@sub.domain.com", "first.last", "first", "last",
    "@sub.domain.com", "sub.domain.com", "domain", "@domain"), ZimbraAnalyzerTest.toTokens(stream));
 }

 @Test
 void multi() throws Exception {
  TokenStream stream = new RFC822AddressTokenStream(
    "\"User One\" <user.1@zimbra.com>, \"User Two\" <user.2@zimbra.com>, \"User Three\" <user.3@zimbra.com>");
  assertEquals(Arrays.asList(
      "user", "one", "user.1@zimbra.com", "user.1", "user", "1",
      "@zimbra.com", "zimbra.com", "zimbra", "@zimbra",
      "user", "two", "user.2@zimbra.com", "user.2", "user", "2",
      "@zimbra.com", "zimbra.com", "zimbra", "@zimbra",
      "user", "three", "user.3@zimbra.com", "user.3", "user", "3",
      "@zimbra.com", "zimbra.com", "zimbra", "@zimbra"),
    ZimbraAnalyzerTest.toTokens(stream));
 }

 @Test
 void comment() throws Exception {
  TokenStream stream = new RFC822AddressTokenStream(
    "Pete(A wonderful \\) chap) <pete(his account)@silly.test(his host)>");
  assertEquals(Arrays.asList("pete", "a", "wonderful", "chap", "pete", "his", "account", "@silly.test",
      "his", "host", "pete@silly.test", "pete", "@silly.test", "silly.test"),
    ZimbraAnalyzerTest.toTokens(stream));
 }

 @Test
 void topPrivateDomain() throws Exception {
  TokenStream stream = new RFC822AddressTokenStream("support@zimbra.com");
  assertEquals(Arrays.asList("support@zimbra.com", "support", "@zimbra.com", "zimbra.com", "zimbra",
    "@zimbra"), ZimbraAnalyzerTest.toTokens(stream));

  stream = new RFC822AddressTokenStream("support@zimbra.vmware.co.jp");
  assertEquals(Arrays.asList("support@zimbra.vmware.co.jp", "support", "@zimbra.vmware.co.jp",
    "zimbra.vmware.co.jp", "vmware", "@vmware"), ZimbraAnalyzerTest.toTokens(stream));

  stream = new RFC822AddressTokenStream("test@co.jp");
  assertEquals(Arrays.asList("test@co.jp", "test", "@co.jp", "co.jp"),
    ZimbraAnalyzerTest.toTokens(stream));
 }

 @Test
 void reset() throws Exception {
  TokenStream stream = new RFC822AddressTokenStream("user@domain.com");
  stream.reset();
  assertEquals(Arrays.asList("user@domain.com", "user", "@domain.com", "domain.com", "domain", "@domain"),
    ZimbraAnalyzerTest.toTokens(stream));
  stream.reset();
  assertEquals(Arrays.asList("user@domain.com", "user", "@domain.com", "domain.com", "domain", "@domain"),
    ZimbraAnalyzerTest.toTokens(stream));
 }

 @Test
 void limit() throws Exception {
  TokenStream stream = new RFC822AddressTokenStream("<" + Strings.repeat("x.", 600) + "x@zimbra.com>");
  assertEquals(512, ZimbraAnalyzerTest.toTokens(stream).size());
 }

 @Test
 void japanese() throws Exception {
  TokenStream stream = new RFC822AddressTokenStream("=?utf-8?B?5qOu44CA5qyh6YOO?= <jiro.mori@zimbra.com>");
  assertEquals(Arrays.asList("\u68ee", "\u6b21\u90ce", "jiro.mori@zimbra.com", "jiro.mori", "jiro", "mori",
    "@zimbra.com", "zimbra.com", "zimbra", "@zimbra"),  ZimbraAnalyzerTest.toTokens(stream));
 }

}
