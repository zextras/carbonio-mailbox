// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;


public class StringUtilTest {

  @Test
  void testFillTemplate() {
    String template = "The quick ${COLOR} ${ANIMAL}\njumped over the ${ADJECTIVE} dogs.\n";
    Map<String, String> vars = new HashMap<String, String>();
    vars.put("COLOR", "brown");
    vars.put("ANIMAL", "fox");
    vars.put("ADJECTIVE", "lazy");
    String result = StringUtil.fillTemplate(template, vars);
    String expected = "The quick brown fox\njumped over the lazy dogs.\n";
    assertEquals(expected, result);
  }

  @Test
  void testFillTemplateWithNewlineValue() {
    String template = "New message received at ${RECIPIENT_ADDRESS}." +
        "${NEWLINE}Sender: ${SENDER_ADDRESS}${NEWLINE}Subject: ${SUBJECT}";

    Map<String, String> vars = new HashMap<String, String>();
    vars.put("SENDER_ADDRESS", "sender@example.zimbra.com");
    vars.put("RECIPIENT_ADDRESS", "recipient@example.zimbra.com");
    vars.put("RECIPIENT_DOMAIN", "example.zimbra.com");
    vars.put("NOTIFICATION_ADDRESS", "notify@example.zimbra.com");
    vars.put("SUBJECT", "Cool stuff");
    vars.put("NEWLINE", "\n");

    String expected = "New message received at recipient@example.zimbra.com." +
        "\nSender: sender@example.zimbra.com\nSubject: Cool stuff";
    String actual = StringUtil.fillTemplate(template, vars);
    assertEquals(expected, actual, "expected: '" + expected + "', actual: '" + actual + "'");
  }

  @Test
  void testFillTemplateWithBraces() {
    String template = "Beginning ${VAR} { end }";
    Map<String, String> vars = new HashMap<String, String>();
    vars.put("VAR", "middle");
    String result = StringUtil.fillTemplate(template, vars);
    String expected = "Beginning middle { end }";
    assertEquals(expected, result);
  }

  @Test
  void testJoin() {
    List<String> list = new ArrayList<String>();
    list.add("a");
    list.add("b");
    list.add("c");
    assertEquals("a,b,c", StringUtil.join(",", list));
    String[] array = new String[list.size()];
    list.toArray(array);
    assertEquals("a,b,c", StringUtil.join(",", array));

    // Make sure things still work if the first element is empty (bug 29513)
    list.set(0, "");
    assertEquals(",b,c", StringUtil.join(",", list));
    list.toArray(array);
    assertEquals(",b,c", StringUtil.join(",", array));
  }

  @Test
  void testStripControlCharacters() {
    assertNull(StringUtil.stripControlCharacters(null), "null string");
    assertEquals(StringUtil.stripControlCharacters(""), "", "empty string");
    assertEquals(StringUtil.stripControlCharacters("ccc"), "ccc", "no stripping");
    assertEquals(StringUtil.stripControlCharacters("\u0000"), "", "one NUL");
    assertEquals(StringUtil.stripControlCharacters("\u0000\u0002"), "", "just strippable chars");
    assertEquals(StringUtil.stripControlCharacters("\u0000v\u0002"), "v", "char between strippable chars");
    assertEquals(StringUtil.stripControlCharacters("c\u0000v\u0002"), "cv", "char, strip, char, strip");
    assertEquals(StringUtil.stripControlCharacters("\u0000v\u0002x"), "vx", "strip, char, strip, char");
    assertEquals(StringUtil.stripControlCharacters("\uDC00\uDBFFv\u0002x"), "vx", "misordered surrogates at start");
    assertEquals(StringUtil.stripControlCharacters("v\u0002x\uDC00\uDBFF"), "vx", "misordered surrogates at end");
    assertEquals(StringUtil.stripControlCharacters("\uDBFF\uDC00v\u0002x"), "\uDBFF\uDC00vx", "surrogates and char, strip, char");
    assertEquals(StringUtil.stripControlCharacters("\uDBFF\uDC00\uFFFFvx"), "\uDBFF\uDC00vx", "surrogates and BOM");
  }

  @Test
  void sanitizeFilename() {
    assertEquals("abc .pdf", StringUtil.sanitizeFilename("abc\t.pdf"));
  }

  @Test
  void testReplaceSurrogates() {
    assertNull(StringUtil.removeSurrogates(null), "null string");
    assertEquals(StringUtil.removeSurrogates(""), "", "empty string");
    assertEquals(StringUtil.removeSurrogates("asda"), "asda", "no surrogates");
    assertEquals(StringUtil.removeSurrogates("\uDBFF\uDC00\uFFFFvx"), "?\uFFFFvx", "leading surrogate");
    assertEquals(StringUtil.removeSurrogates("\uFFFFvx\uDBFF\uDC00"), "\uFFFFvx?", "trailing surrogate");
    assertEquals(StringUtil.removeSurrogates("\uFFFFvx\uDBFF\uDC00\uDBFF\uDC00"), "\uFFFFvx??", "consecutive surrogates");
  }

  /**
   * Tests {@link StringUtil#newMatcher(String, String)} and the regex pattern cache.
   */
  @Test
  void testPatternCache() {
    String s = "abcdefghijklmnopqrstuvwxyz";
    Matcher m = StringUtil.newMatcher("(g.*j)", s);
    assertTrue(m.find());
    assertEquals("ghij", m.group(1));
  }

  @Test
  void testReplaceAll() {
    assertEquals("abc456def456ghi", StringUtil.replaceAll("abc123def12223ghi", "1\\d+3", "456"));
  }

  @Test
  void testLfToCrlf() {
    assertEquals("abc\r\ndef\r\nhij\r\n", StringUtil.lfToCrlf("abc\r\ndef\nhij\n"));
  }

  @Test
  void testEqual() {
    assertEquals(true, StringUtil.equal(null, null));
    assertEquals(false, StringUtil.equal(null, ""));
    assertEquals(false, StringUtil.equal("", null));
    assertEquals(false, StringUtil.equal(null, "  "));
    assertEquals(false, StringUtil.equal("  ", null));
    assertEquals(false, StringUtil.equal("abc", " abc "));
    assertEquals(false, StringUtil.equal(null, "abc"));
    assertEquals(false, StringUtil.equal("a bc", "abc"));
  }
}
