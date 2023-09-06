// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.Version;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link UniversalAnalyzer}.
 *
 * @author ysasaki
 */
public final class UniversalAnalyzerTest {
  private UniversalAnalyzer universalAnalyzer = new UniversalAnalyzer();
  // for backward compatibility
  private StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_24);
  private CJKAnalyzer cjkAnalyzer = new CJKAnalyzer(Version.LUCENE_31);
  // See https://issues.apache.org/jira/browse/LUCENE-1068
  private boolean assertOffset = true;

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @BeforeEach
  public void setUp() {
    assertOffset = true;
  }

  @Test
  void variousText() throws Exception {
    testSTD("C embedded developers wanted");
    testSTD("foo bar FOO BAR");
    testSTD("foo      bar .  FOO <> BAR");
    testSTD("\"QUOTED\" word");

    testSTD("Zimbra is awesome.");
  }

  @Test
  void acronym() throws Exception {
    testSTD("U.S.A.");
  }

  @Test
  void alphanumeric() throws Exception {
    testSTD("B2B");
    testSTD("2B");
  }

  @Test
  void underscore() throws Exception {
    testSTD("word_having_underscore");
    testSTD("word_with_underscore_and_stopwords");
  }

  @Test
  void delimiter() throws Exception {
    testSTD("some-dashed-phrase");
    testSTD("dogs,chase,cats");
    testSTD("ac/dc");
  }

  @Test
  void apostrophe() throws Exception {
    testSTD("O'Reilly");
    testSTD("you're");
    testSTD("she's");
    testSTD("Jim's");
    testSTD("don't");
    testSTD("O'Reilly's");
  }

  @Test
  void tsa() throws Exception {
    // t and s had been stopwords in Lucene <= 2.0, which made it impossible
    // to correctly search for these terms:
    testSTD("s-class");
    testSTD("t-com");
    // 'a' is still a stopword:
    testSTD("a-class");
  }

  @Test
  void company() throws Exception {
    testSTD("AT&T");
    testSTD("Excite@Home");
  }

  @Test
  void domain() throws Exception {
    testSTD("www.nutch.org");
    assertOffset = false;
    testSTD("www.nutch.org.");
  }

  @Test
  void email() throws Exception {
    testSTD("test@example.com");
    testSTD("first.lastname@example.com");
    testSTD("first-lastname@example.com");
    testSTD("first_lastname@example.com");
  }

  @Test
  void number() throws Exception {
    // floating point, serial, model numbers, ip addresses, etc.
    // every other segment must have at least one digit
    testSTD("21.35");
    testSTD("R2D2 C3PO");
    testSTD("216.239.63.104");
    testSTD("1-2-3");
    testSTD("a1-b2-c3");
    testSTD("a1-b-c3");
  }

  @Test
  void textWithNumber() throws Exception {
    testSTD("David has 5000 bones");
  }

  @Test
  void cPlusPlusHash() throws Exception {
    testSTD("C++");
    testSTD("C#");
  }

  @Test
  void filename() throws Exception {
    testSTD("2004.jpg");
  }

  @Test
  void numericIncorrect() throws Exception {
    testSTD("62.46");
  }

  @Test
  void numericLong() throws Exception {
    testSTD("978-0-94045043-1");
  }

  @Test
  void numericFile() throws Exception {
    testSTD("78academyawards/rules/rule02.html");
  }

  @Test
  void numericWithUnderscores() throws Exception {
    testSTD("2006-03-11t082958z_01_ban130523_rtridst_0_ozabs");
  }

  @Test
  void numericWithDash() throws Exception {
    testSTD("mid-20th");
  }

  @Test
  void manyTokens() throws Exception {
    testSTD(
        "/money.cnn.com/magazines/fortune/fortune_archive/2007/03/19/8402357/index.htm "
            + "safari-0-sheikh-zayed-grant-mosque.jpg");
  }

  @Test
  @Disabled("Fix me!")
  void wikipedia() throws Exception {
    String src =
        new String(
            ByteStreams.toByteArray(getClass().getResourceAsStream("wikipedia.txt")),
            Charsets.ISO_8859_1);
    assertOffset = false;
    testSTD(src);
  }

  @Test
  void japanese() throws Exception {
    testCJK("\u4e00");

    testCJK("\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341");
    testCJK("\u4e00 \u4e8c\u4e09\u56db \u4e94\u516d\u4e03\u516b\u4e5d \u5341");

    testCJK("\u3042\u3044\u3046\u3048\u304aabc\u304b\u304d\u304f\u3051\u3053");
    testCJK("\u3042\u3044\u3046\u3048\u304aab\u3093c\u304b\u304d\u304f\u3051 \u3053");
  }

  @Test
  void jaPunc() throws Exception {
    testCJK("\u4e00\u3001\u4e8c\u3001\u4e09\u3001\u56db\u3001\u4e94");
  }

  @Test
  void fullwidth() throws Exception {
    testCJK("\uff34\uff45\uff53\uff54 \uff11\uff12\uff13\uff14");
  }

  private void testSTD(String src) throws IOException {
    TokenStream std = standardAnalyzer.tokenStream(null, new StringReader(src));
    CharTermAttribute stdTermAttr = std.addAttribute(CharTermAttribute.class);
    OffsetAttribute stdOffsetAttr = std.addAttribute(OffsetAttribute.class);
    PositionIncrementAttribute stdPosIncAttr = std.addAttribute(PositionIncrementAttribute.class);

    TokenStream uni = universalAnalyzer.tokenStream(null, new StringReader(src));
    CharTermAttribute uniTermAttr = uni.addAttribute(CharTermAttribute.class);
    OffsetAttribute uniOffsetAttr = uni.addAttribute(OffsetAttribute.class);
    PositionIncrementAttribute uniPosIncAttr = uni.addAttribute(PositionIncrementAttribute.class);

    while (true) {
      boolean result = std.incrementToken();
      assertEquals(result, uni.incrementToken());
      if (!result) {
        break;
      }
      String term = stdTermAttr.toString();
      assertEquals(stdTermAttr, uniTermAttr);
      if (assertOffset) {
        assertEquals(stdOffsetAttr, uniOffsetAttr, term);
      }
      assertEquals(stdPosIncAttr, uniPosIncAttr, term);
    }
  }

  private void testCJK(String src) throws IOException {
    TokenStream cjk = cjkAnalyzer.tokenStream(null, new StringReader(src));
    CharTermAttribute cjkTermAttr = cjk.addAttribute(CharTermAttribute.class);
    OffsetAttribute cjkOffsetAttr = cjk.addAttribute(OffsetAttribute.class);
    PositionIncrementAttribute cjkPosIncAttr = cjk.addAttribute(PositionIncrementAttribute.class);

    TokenStream uni = universalAnalyzer.tokenStream(null, new StringReader(src));
    CharTermAttribute uniTermAttr = uni.addAttribute(CharTermAttribute.class);
    OffsetAttribute uniOffsetAttr = uni.addAttribute(OffsetAttribute.class);
    PositionIncrementAttribute uniPosIncAttr = uni.addAttribute(PositionIncrementAttribute.class);

    while (true) {
      boolean result = cjk.incrementToken();
      assertEquals(result, uni.incrementToken());
      if (!result) {
        break;
      }
      String term = cjkTermAttr.toString();
      assertEquals(cjkTermAttr, uniTermAttr);
      if (assertOffset) {
        assertEquals(cjkOffsetAttr, uniOffsetAttr, term);
      }
      assertEquals(cjkPosIncAttr, uniPosIncAttr, term);
    }
  }
}
