// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.http;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.zimbra.common.util.Pair;

import static org.junit.jupiter.api.Assertions.*;

public class RangeTest
{
 @Test
 void parseSingleRange() throws Exception
 {
  Range range = Range.parse("bytes=0-999");

  List<Pair<Long, Long>> ranges = range.getRanges();
  assertFalse(ranges.isEmpty());
  Iterator<Pair<Long, Long>> iter = ranges.iterator();
  Pair<Long, Long> byteRange = iter.next();
  assertFalse(iter.hasNext());

  assertEquals(byteRange.getFirst().longValue(), 0);
  assertEquals(byteRange.getSecond().longValue(), 999);
 }

 @Test
 void parseOpenEndedRange() throws Exception
 {
  Range range = Range.parse("bytes=123-");

  List<Pair<Long, Long>> ranges = range.getRanges();
  assertFalse(ranges.isEmpty());
  Iterator<Pair<Long, Long>> iter = ranges.iterator();
  Pair<Long, Long> byteRange = iter.next();
  assertFalse(iter.hasNext());

  assertEquals(byteRange.getFirst().longValue(), 123);
  assertNull(byteRange.getSecond());
 }

 @Test
 void parseSuffixRange() throws Exception
 {
  Range range = Range.parse("bytes=-999");

  List<Pair<Long, Long>> ranges = range.getRanges();
  assertFalse(ranges.isEmpty());
  Iterator<Pair<Long, Long>> iter = ranges.iterator();
  Pair<Long, Long> byteRange = iter.next();
  assertFalse(iter.hasNext());

  assertNull(byteRange.getFirst());
  assertEquals(byteRange.getSecond().longValue(), 999);
 }

 @Test
 void parseMultipleRanges() throws Exception
 {
  Range range = Range.parse("bytes=0-999,2000-3000, 4444 - 7777, -12345");

  List<Pair<Long, Long>> ranges = range.getRanges();
  assertFalse(ranges.isEmpty());

  Iterator<Pair<Long, Long>> iter = ranges.iterator();

  {
   Pair<Long, Long> byteRange = iter.next();
   assertEquals(byteRange.getFirst().longValue(), 0);
   assertEquals(byteRange.getSecond().longValue(), 999);
  }

  {
   Pair<Long, Long> byteRange = iter.next();
   assertEquals(byteRange.getFirst().longValue(), 2000);
   assertEquals(byteRange.getSecond().longValue(), 3000);
  }

  {
   Pair<Long, Long> byteRange = iter.next();
   assertEquals(byteRange.getFirst().longValue(), 4444);
   assertEquals(byteRange.getSecond().longValue(), 7777);
  }

  {
   Pair<Long, Long> byteRange = iter.next();
   assertNull(byteRange.getFirst());
   assertEquals(byteRange.getSecond().longValue(), 12345);
  }

  assertFalse(iter.hasNext());
 }

 @Test
 void parseMultipleRangesWithExtraSpacesAndCommas() throws Exception
 {
  Range range = Range.parse("  bytes = ,, 0   - 999,,,, ,  ,2000  -3000  ");

  List<Pair<Long, Long>> ranges = range.getRanges();
  assertFalse(ranges.isEmpty());

  Iterator<Pair<Long, Long>> iter = ranges.iterator();

  {
   Pair<Long, Long> byteRange = iter.next();
   assertEquals(byteRange.getFirst().longValue(), 0);
   assertEquals(byteRange.getSecond().longValue(), 999);
  }

  {
   Pair<Long, Long> byteRange = iter.next();
   assertEquals(byteRange.getFirst().longValue(), 2000);
   assertEquals(byteRange.getSecond().longValue(), 3000);
  }
 }

 @Test
 void parseNull() throws Exception
 {
  Range range = Range.parse((String) null);
  assertNull(range);
 }

 @Test
 void parseReversedRange() throws Exception
 {
  assertThrows(RangeException.class, () -> {
   Range.parse("bytes=200-100");
  });
 }

 @Test
 void parseNegativeEndValue() throws Exception
 {
  assertThrows(RangeException.class, () -> {
   Range.parse("bytes=100--200");
  });
 }

 @Test
 void parseNegativeStartValue() throws Exception
 {
  assertThrows(RangeException.class, () -> {
   Range.parse("bytes=-100-200");
  });
 }

 @Test
 void parseGarbage() throws Exception
 {
  assertThrows(RangeException.class, () -> {
   Range.parse("garbage");
  });
 }

 @Test
 void parseNoValues() throws Exception
 {
  assertThrows(RangeException.class, () -> {
   Range.parse("-");
  });
 }
}
