// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.http;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.zimbra.common.util.Pair;

public class RangeTest
{
    @Test
    public void parseSingleRange() throws Exception
    {
        Range range = Range.parse("bytes=0-999");

        List<Pair<Long, Long>> ranges = range.getRanges();
        Assert.assertFalse(ranges.isEmpty());
        Iterator<Pair<Long, Long>> iter = ranges.iterator();
        Pair<Long, Long> byteRange = iter.next();
        Assert.assertFalse(iter.hasNext());

        Assert.assertEquals(byteRange.getFirst().longValue(), 0);
        Assert.assertEquals(byteRange.getSecond().longValue(), 999);
    }

    @Test
    public void parseOpenEndedRange() throws Exception
    {
        Range range = Range.parse("bytes=123-");

        List<Pair<Long, Long>> ranges = range.getRanges();
        Assert.assertFalse(ranges.isEmpty());
        Iterator<Pair<Long, Long>> iter = ranges.iterator();
        Pair<Long, Long> byteRange = iter.next();
        Assert.assertFalse(iter.hasNext());

        Assert.assertEquals(byteRange.getFirst().longValue(), 123);
        Assert.assertNull(byteRange.getSecond());
    }

    @Test
    public void parseSuffixRange() throws Exception
    {
        Range range = Range.parse("bytes=-999");

        List<Pair<Long, Long>> ranges = range.getRanges();
        Assert.assertFalse(ranges.isEmpty());
        Iterator<Pair<Long, Long>> iter = ranges.iterator();
        Pair<Long, Long> byteRange = iter.next();
        Assert.assertFalse(iter.hasNext());

        Assert.assertNull(byteRange.getFirst());
        Assert.assertEquals(byteRange.getSecond().longValue(), 999);
    }

    @Test
    public void parseMultipleRanges() throws Exception
    {
        Range range = Range.parse("bytes=0-999,2000-3000, 4444 - 7777, -12345");

        List<Pair<Long, Long>> ranges = range.getRanges();
        Assert.assertFalse(ranges.isEmpty());

        Iterator<Pair<Long, Long>> iter = ranges.iterator();

        {
            Pair<Long, Long> byteRange = iter.next();
            Assert.assertEquals(byteRange.getFirst().longValue(), 0);
            Assert.assertEquals(byteRange.getSecond().longValue(), 999);
        }

        {
            Pair<Long, Long> byteRange = iter.next();
            Assert.assertEquals(byteRange.getFirst().longValue(), 2000);
            Assert.assertEquals(byteRange.getSecond().longValue(), 3000);
        }

        {
            Pair<Long, Long> byteRange = iter.next();
            Assert.assertEquals(byteRange.getFirst().longValue(), 4444);
            Assert.assertEquals(byteRange.getSecond().longValue(), 7777);
        }

        {
            Pair<Long, Long> byteRange = iter.next();
            Assert.assertNull(byteRange.getFirst());
            Assert.assertEquals(byteRange.getSecond().longValue(), 12345);
        }

        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public void parseMultipleRangesWithExtraSpacesAndCommas() throws Exception
    {
        Range range = Range.parse("  bytes = ,, 0   - 999,,,, ,  ,2000  -3000  ");

        List<Pair<Long, Long>> ranges = range.getRanges();
        Assert.assertFalse(ranges.isEmpty());

        Iterator<Pair<Long, Long>> iter = ranges.iterator();

        {
            Pair<Long, Long> byteRange = iter.next();
            Assert.assertEquals(byteRange.getFirst().longValue(), 0);
            Assert.assertEquals(byteRange.getSecond().longValue(), 999);
        }

        {
            Pair<Long, Long> byteRange = iter.next();
            Assert.assertEquals(byteRange.getFirst().longValue(), 2000);
            Assert.assertEquals(byteRange.getSecond().longValue(), 3000);
        }
    }

    @Test
    public void parseNull() throws Exception
    {
        Range range = Range.parse((String)null);
        Assert.assertNull(range);
    }

    @Test(expected=RangeException.class)
    public void parseReversedRange() throws Exception
    {
        Range.parse("bytes=200-100");
    }

    @Test(expected=RangeException.class)
    public void parseNegativeEndValue() throws Exception
    {
        Range.parse("bytes=100--200");
    }

    @Test(expected=RangeException.class)
    public void parseNegativeStartValue() throws Exception
    {
        Range.parse("bytes=-100-200");
    }

    @Test(expected=RangeException.class)
    public void parseGarbage() throws Exception
    {
        Range.parse("garbage");
    }

    @Test(expected=RangeException.class)
    public void parseNoValues() throws Exception
    {
        Range.parse("-");
    }
}
