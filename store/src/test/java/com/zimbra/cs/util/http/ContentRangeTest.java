// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.http;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for ContentRange
 *
 * @author grzes
 */
public class ContentRangeTest
{
 @Test
 void parseRange() throws Exception
 {
  ContentRange range = ContentRange.parse("bytes 0-999/1000");

  assertTrue(range.hasStartEnd());
  assertEquals(range.getStart(), 0);
  assertEquals(range.getEnd(), 999);
  assertTrue(range.hasInstanceLength());
  assertEquals(range.getInstanceLength(), 1000);
 }

 @Test
 void parseRangeNoInstanceLength() throws Exception
 {
  ContentRange range = ContentRange.parse("bytes 100-200/*");

  assertTrue(range.hasStartEnd());
  assertEquals(range.getStart(), 100);
  assertEquals(range.getEnd(), 200);
  assertFalse(range.hasInstanceLength());
 }

 @Test
 void parseRangeExtraWhitespaces() throws Exception
 {
  ContentRange range = ContentRange.parse("   bytes   77777 -   99999   /   123456   ");

  assertTrue(range.hasStartEnd());
  assertEquals(range.getStart(), 77777);
  assertEquals(range.getEnd(), 99999);
  assertTrue(range.hasInstanceLength());
  assertEquals(range.getInstanceLength(), 123456);
 }

 @Test
 void parseNoRangeWithInstanceLength() throws Exception
 {
  ContentRange range = ContentRange.parse("bytes */7000");

  assertFalse(range.hasStartEnd());
  assertTrue(range.hasInstanceLength());
  assertEquals(range.getInstanceLength(), 7000);
 }

 @Test
 void parseNoRangeNoInstanceLength() throws Exception
 {
  ContentRange range = ContentRange.parse("bytes */*");

  assertFalse(range.hasStartEnd());
  assertFalse(range.hasInstanceLength());
 }

 @Test
 void parseNull() throws Exception
 {
  ContentRange range = ContentRange.parse((String) null);

  assertNull(range);
 }

 @Test
 void parseReversedRange() throws Exception
 {
  assertThrows(RangeException.class, () -> {
   ContentRange.parse("bytes 200-100/777");
  });
 }

 @Test
 void parseRangeTooBig() throws Exception
 {
  assertThrows(RangeException.class, () -> {
   ContentRange.parse("bytes 0-100/50");
  });
 }

 @Test
 void parseNegativeInstanceLength() throws Exception
 {
  assertThrows(RangeException.class, () -> {
   ContentRange.parse("bytes 0-100/-1000");
  });
 }

 @Test
 void parseNegativeEnd() throws Exception
 {
  assertThrows(RangeException.class, () -> {
   ContentRange.parse("bytes 0--100/1000");
  });
 }

 @Test
 void parseGarbage() throws Exception
 {
  assertThrows(RangeException.class, () -> {
   ContentRange.parse("garbage");
  });
 }

}
