// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.zimbra.cs.mailbox.MailboxTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link ZimbraAnalyzer}.
 *
 * @author ysasaki
 */
public final class ZimbraAnalyzerTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

 @Test
 void size() throws Exception {
  String src = "123 26 1000000 100000000 1,000,000,000 1,000,000,000,000,000";
  TokenStream stream = ZimbraAnalyzer.getInstance().tokenStream(LuceneFields.L_SORT_SIZE, new StringReader(src));
  assertEquals(Arrays.asList("123", "26", "1000000", "100000000", "1000000000", "1000000000000000"),
    toTokens(stream));
 }

 @Test
 void filename() throws Exception {
  String src = "This is my-filename.test.pdf";
  TokenStream stream = ZimbraAnalyzer.getInstance().tokenStream(LuceneFields.L_FILENAME, new StringReader(src));
  assertEquals(Arrays.asList("this", "is", "my-filename", "test", "pdf"), toTokens(stream));
 }

 /**
  * We intentionally disable the positionIncrement because we want phrases to match across removed stop words.
  *
  * @see PositionIncrementAttribute
     */
 @Test
 void positionIncrement() throws Exception {
  TokenStream stream = ZimbraAnalyzer.getInstance().tokenStream(
    LuceneFields.L_H_SUBJECT, new StringReader("It's a test."));
  PositionIncrementAttribute posIncrAtt = stream.addAttribute(PositionIncrementAttribute.class);
  while (stream.incrementToken()) {
   assertEquals(posIncrAtt.getPositionIncrement(), 1);
  }
  stream.end();
  stream.close();
 }

 @Test
 void phraseQuery() throws Exception {
  String src = "three^two";
  TokenStream stream = ZimbraAnalyzer.getInstance().tokenStream(LuceneFields.L_CONTENT, new StringReader(src));
  assertEquals(Arrays.asList("three", "two"), toTokens(stream));
 }

    public static List<String> toTokens(TokenStream stream) throws IOException {
        List<String> result = new ArrayList<String>();
        CharTermAttribute termAttr = stream.addAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            result.add(termAttr.toString());
        }
        stream.end();
        return result;
    }

}
