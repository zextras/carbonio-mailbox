// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


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
      assertEquals(true, Objects.equals(null, null));
      assertEquals(false, Objects.equals(null, ""));
      assertEquals(false, Objects.equals("", null));
      assertEquals(false, Objects.equals(null, "  "));
      assertEquals(false, Objects.equals("  ", null));
      assertEquals(false, Objects.equals("abc", " abc "));
      assertEquals(false, Objects.equals(null, "abc"));
      assertEquals(false, Objects.equals("a bc", "abc"));
  }

  @Test
  void equalIgnoreCase() {
      assertTrue(Strings.CI.equals(null, null));
      assertFalse(Strings.CI.equals(null, "a"));
      assertFalse(Strings.CI.equals("a", null));
      assertTrue(Strings.CI.equals("ABC", "abc"));
      assertFalse(Strings.CI.equals("abc", "abd"));
  }

  @Test
  void compareToSortsNullFirst() {
      assertEquals(0, Strings.CS.compare(null, null));
      assertEquals(-1, Strings.CS.compare(null, "a"));
      assertEquals(1, Strings.CS.compare("a", null));
      assertTrue(Strings.CS.compare("a", "b") < 0);
      assertTrue(Strings.CS.compare("B", "a") < 0);
  }

  @Test
  void compareToIgnoreCaseSortsNullFirst() {
      assertEquals(0, Strings.CI.compare(null, null));
      assertEquals(-1, Strings.CI.compare(null, "a"));
      assertEquals(1, Strings.CI.compare("a", null));
      assertEquals(0, Strings.CI.compare("ABC", "abc"));
      assertTrue(Strings.CI.compare("a", "B") < 0);
  }

  @Test
  void countOccurrences() {
      assertEquals(0, StringUtils.countMatches(null, 'a'));
      assertEquals(0, StringUtils.countMatches("bcd", 'a'));
      assertEquals(3, StringUtils.countMatches("abaca", 'a'));
  }

  @Test
  void indexOfAny() {
    assertEquals(-1, StringUtil.indexOfAny(null, "a"));
    assertEquals(-1, StringUtil.indexOfAny("abc", null));
    assertEquals(-1, StringUtil.indexOfAny("", "a"));
    assertEquals(-1, StringUtil.indexOfAny("abc", ""));
    assertEquals(-1, StringUtil.indexOfAny("abc", "xyz"));
    assertEquals(1, StringUtil.indexOfAny("abc", "cb"));
  }

  @Test
  void sanitizeFilenameKeepsNullAndEmpty() {
    assertNull(StringUtil.sanitizeFilename(null));
    assertEquals("", StringUtil.sanitizeFilename(""));
    assertEquals("a b c", StringUtil.sanitizeFilename("a\u0000b\nc"));
  }

  @Test
  void stripControlCharactersKeepsTabCrLfAndValidSurrogatePairs() {
    String allowed = "a\tb\rc\nd😀";
    assertSame(allowed, StringUtil.stripControlCharacters(allowed));
  }

  @Test
  void stripControlCharactersRemovesUnpairedHighSurrogate() {
    assertEquals("ab", StringUtil.stripControlCharacters("a\uD800b"));
    assertEquals("ab", StringUtil.stripControlCharacters("ab\uD800"));
    assertEquals("ab", StringUtil.stripControlCharacters("ab￾"));
  }

  @Test
  void removeSurrogatesReturnsSameInstanceWhenNothingToReplace() {
    String noSurrogates = "plain";
    assertSame(noSurrogates, StringUtil.removeSurrogates(noSurrogates));
    assertEquals("a?b", StringUtil.removeSurrogates("a😀b"));
  }

  @Test
  void containsSurrogates() {
    assertFalse(StringUtil.containsSurrogates(null));
    assertFalse(StringUtil.containsSurrogates(""));
    assertFalse(StringUtil.containsSurrogates("abc\uE000"));
    assertTrue(StringUtil.containsSurrogates("a😀"));
  }

  @Test
  void isAsciiString() {
    assertFalse(StringUtil.isAsciiString(null));
    assertTrue(StringUtil.isAsciiString(""));
    assertTrue(StringUtil.isAsciiString("Hello ~\r\n\t"));
    assertFalse(StringUtil.isAsciiString("café"));
    assertFalse(StringUtil.isAsciiString("a\u0001"));
    assertFalse(StringUtil.isAsciiString("a\u007F"));
  }

  @Test
  void trimTrailingSpaces() {
    assertNull(StringUtil.trimTrailingSpaces(null));
    String noTrailing = "  abc";
    assertSame(noTrailing, StringUtil.trimTrailingSpaces(noTrailing));
    assertEquals("  abc", StringUtil.trimTrailingSpaces("  abc \t\u0001\r\n"));
    assertEquals("", StringUtil.trimTrailingSpaces("   "));
  }

  @Test
  void addToMultiMapGrowsFromStringToArray() {
    Map<String, Object> map = new HashMap<>();
    StringUtil.addToMultiMap(map, "k", "v1");
    assertEquals("v1", map.get("k"));
    StringUtil.addToMultiMap(map, "k", "v2");
    assertArrayEquals(new String[] {"v1", "v2"}, (String[]) map.get("k"));
    StringUtil.addToMultiMap(map, "k", "v3");
    assertArrayEquals(new String[] {"v1", "v2", "v3"}, (String[]) map.get("k"));
  }

  @Test
  void addToMultiMapIgnoresUnsupportedExistingValue() {
    Map<String, Object> map = new HashMap<>();
    map.put("k", 42);
    StringUtil.addToMultiMap(map, "k", "v");
    assertEquals(42, map.get("k"));
  }

  @Test
  void keyValueArrayToMultiMap() {
    Map<String, Object> map =
        StringUtil.keyValueArrayToMultiMap(new String[] {"skip", "a1", "v1", "a2", "v2", "a2", "v3"}, 1);
    assertEquals(2, map.size());
    assertEquals("v1", map.get("a1"));
    assertArrayEquals(new String[] {"v2", "v3"}, (String[]) map.get("a2"));
  }

  @Test
  void keyValueArrayToMultiMapRejectsOddArguments() {
    assertThrows(
        IllegalArgumentException.class,
        () -> StringUtil.keyValueArrayToMultiMap(new String[] {"a1", "v1", "a2"}, 0));
  }

  @Test
  void toNewMultimap() {
    Map<String, Object> oldMap = new LinkedHashMap<>();
    oldMap.put("array", new String[] {"a", "b"});
    oldMap.put("string", "s");
    oldMap.put("number", 7);
    oldMap.put("null", null);

    Multimap<String, String> newMap = StringUtil.toNewMultimap(oldMap);

    assertEquals(List.of("a", "b"), newMap.get("array"));
    assertEquals(List.of("s"), newMap.get("string"));
    assertEquals(List.of("7"), newMap.get("number"));
    assertEquals(Arrays.asList((String) null), newMap.get("null"));
  }

  @Test
  void toOldMultimap() {
    Multimap<String, String> newMap = ArrayListMultimap.create();
    newMap.put("single", "s");
    newMap.put("multi", "m1");
    newMap.put("multi", "m2");

    Map<String, Object> oldMap = StringUtil.toOldMultimap(newMap);

    assertEquals("s", oldMap.get("single"));
    assertArrayEquals(new String[] {"m1", "m2"}, (String[]) oldMap.get("multi"));
  }

  @Test
  void toStringArray() {
    assertNull(StringUtil.toStringArray(null));
    assertArrayEquals(new String[] {"a"}, StringUtil.toStringArray("a"));
    String[] array = {"a", "b"};
    assertSame(array, StringUtil.toStringArray(array));
    assertThrows(UnsupportedOperationException.class, () -> StringUtil.toStringArray(1));
  }

  @Test
  void readSingleLineFromFile(@TempDir Path tempDir) throws IOException {
    Path file = tempDir.resolve("lines.txt");
    Files.writeString(file, "first\nsecond\n");
    assertEquals("first", StringUtil.readSingleLineFromFile(file.toString()));
  }

  @Test
  void readLineJoinsContinuationLines() throws IOException {
    BufferedReader reader = readerOf("a\\\nb\n\nc\nd\\");
    assertEquals("ab", StringUtil.readLine(reader));
    assertEquals("", StringUtil.readLine(reader));
    assertEquals("c", StringUtil.readLine(reader));
    assertEquals("d", StringUtil.readLine(reader));
    assertNull(StringUtil.readLine(reader));
  }

  @Test
  void readLineJoinsSeveralContinuationLines() throws IOException {
    assertEquals("abc", StringUtil.readLine(readerOf("a\\\nb\\\nc")));
  }

  @Test
  void readLineStopsContinuationAtEmptyLine() throws IOException {
    BufferedReader reader = readerOf("a\\\n\nb");
    assertEquals("a", StringUtil.readLine(reader));
    assertEquals("b", StringUtil.readLine(reader));
  }

  @Test
  void parseLineSplitsOnWhitespace() {
    assertArrayEquals(new String[] {"this", "is", "a", "test"}, StringUtil.parseLine("this is  a\ttest"));
    assertArrayEquals(new String[0], StringUtil.parseLine(""));
    assertArrayEquals(new String[0], StringUtil.parseLine("   "));
  }

  @Test
  void parseLineHonorsQuotes() {
    assertArrayEquals(
        new String[] {"first", "a nother", "double \"q\"", "end"},
        StringUtil.parseLine("first 'a nother' \"double \\\"q\\\"\" end"));
    assertArrayEquals(new String[] {"it's"}, StringUtil.parseLine("'it\\'s'"));
  }

  @Test
  void parseLineHandlesEscapes() {
    assertArrayEquals(new String[] {"this", "is test"}, StringUtil.parseLine("this is\\ test"));
    assertArrayEquals(new String[] {"multi", "a\nb"}, StringUtil.parseLine("multi 'a\\nb'"));
    assertArrayEquals(new String[] {"\t\r\"'"}, StringUtil.parseLine("\\t\\r\\\"\\'"));
    assertArrayEquals(new String[] {"backslash", "\\"}, StringUtil.parseLine("backslash \\\\"));
    assertArrayEquals(new String[] {"backslash", "f"}, StringUtil.parseLine("backslash \\f"));
    assertArrayEquals(new String[] {"a\\"}, StringUtil.parseLine("a\\"));
  }

  @Test
  void mainDumpsSampleLines() {
    assertDoesNotThrow(() -> StringUtil.main(new String[0]));
  }

  @Test
  void fillTemplateWithNullTemplate() {
    assertNull(StringUtil.fillTemplate(null, Map.of()));
  }

  @Test
  void fillTemplateReplacesMissingKeyWithEmptyString() {
    assertEquals("Hello !", StringUtil.fillTemplate("Hello ${NAME}!", Map.of()));
  }

  @Test
  void fillTemplateWithCustomPattern() {
    assertEquals(
        "Hello World", StringUtil.fillTemplate("Hello @NAME@", Map.of("NAME", "World"), StringUtil.atPattern));
  }

  @Test
  void joinShorts() {
    assertNull(StringUtil.join(",", (short[]) null));
    assertEquals("", StringUtil.join(",", new short[0]));
    assertEquals("1,2,3", StringUtil.join(",", new short[] {1, 2, 3}));
  }

  @Test
  void joinNullArrays() {
    assertNull(StringUtil.join(",", (Object[]) null));
    assertNull(StringUtil.join(",", null, 0, 1));
    assertNull(StringUtil.join(",", (Iterable<?>) null));
  }

  @Test
  void joinArrayRange() {
    assertEquals("b-c", StringUtil.join("-", new Object[] {"a", "b", "c", "d"}, 1, 2));
  }

  @Test
  void isNullOrEmpty() {
    assertTrue(StringUtil.isNullOrEmpty(null));
    assertTrue(StringUtil.isNullOrEmpty(""));
    assertFalse(StringUtil.isNullOrEmpty(" "));
  }

  @Test
  void jsEncode() {
    assertEquals("", StringUtil.jsEncode(null));
    String safe = "safe";
    assertSame(safe, StringUtil.jsEncode(safe));
    assertEquals(
        "\\u003Ca\\u003E\\\\\\\"\\u2028\\u2029\\n\\u0000x",
        StringUtil.jsEncode("<a>\\\"  \n\u0000x"));
    assertEquals("42", StringUtil.jsEncode(42));
  }

  @Test
  void escapeHtml() {
    assertEquals("", StringUtil.escapeHtml(null));
    assertEquals("", StringUtil.escapeHtml(""));
    assertEquals(
        "&lt;a href=&quot;x&quot;&gt;Tom &amp; Jerry&#039;s  x&lt;/a&gt;",
        StringUtil.escapeHtml("<a href=\"x\">Tom & Jerry's\r\nx</a>"));
  }

  @Test
  void isJavaReservedWord() {
    assertTrue(StringUtil.isJavaReservedWord("class"));
    assertFalse(StringUtil.isJavaReservedWord("clazz"));
  }

  @Test
  void enclose() {
    assertEquals("'abc'", StringUtil.enclose("abc", '\''));
  }

  @Test
  void getCachedPatternReturnsSameInstance() {
    Pattern pattern = StringUtil.getCachedPattern("cached-[0-9]+");
    assertSame(pattern, StringUtil.getCachedPattern("cached-[0-9]+"));
  }

  @Test
  void replaceAllWithNull() {
    assertNull(StringUtil.replaceAll(null, "a", "b"));
  }

  @Test
  void isUUID() {
    assertTrue(StringUtil.isUUID("123e4567-e89b-12d3-a456-426614174000"));
    assertFalse(StringUtil.isUUID("123e4567e89b12d3a456426614174000"));
    assertFalse(StringUtil.isUUID("123e4567xe89b-12d3-a456-426614174000"));
    assertFalse(StringUtil.isUUID("123e4567-e89bx12d3-a456-426614174000"));
    assertFalse(StringUtil.isUUID("123e4567-e89b-12d3xa456-426614174000"));
    assertFalse(StringUtil.isUUID("123e4567-e89b-12d3-a456x426614174000"));
  }

      @Test
  void truncateIfRequiredDoesNotSplitMultiByteCharacters() {
    String body = "héllo";
    assertEquals("h", StringUtil.truncateIfRequired(body, 2));
    assertEquals("hé", StringUtil.truncateIfRequired(body, 3));
    assertEquals("", StringUtil.truncateIfRequired("é", 1));
  }

  @Test
  void truncateIfRequiredLeavesShortOrUnlimitedBodies() {
    String body = "hello";
    assertSame(body, StringUtil.truncateIfRequired(body, 10));
    assertSame(body, StringUtil.truncateIfRequired(body, -1));
  }

  @Test
  void sanitizeString() {
    assertNull(StringUtil.sanitizeString(null));
    assertEquals(
        "abcd e f g h😀",
        StringUtil.sanitizeString(
            "a\u0000b​c͸d e f g\uD800 h😀"));
  }

  @Test
  void maskEmail() {
    assertNull(StringUtil.maskEmail(null));
    assertNull(StringUtil.maskEmail(""));
    assertEquals("*@example.com", StringUtil.maskEmail("a@example.com"));
    assertEquals("a*@example.com", StringUtil.maskEmail("ab@example.com"));
    assertEquals("a**@example.com", StringUtil.maskEmail("abc@example.com"));
    assertEquals("abc***@example.com", StringUtil.maskEmail("abcdef@example.com"));
  }

  private static BufferedReader readerOf(String text) {
    return new BufferedReader(new StringReader(text));
  }
}
