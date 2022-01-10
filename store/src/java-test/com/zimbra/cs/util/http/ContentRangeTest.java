// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.http;

import org.junit.Assert;
import org.junit.Test;



/**
 * Unit tests for ContentRange
 *
 * @author grzes
 */
public class ContentRangeTest
{
    @Test
    public void parseRange() throws Exception
    {
        ContentRange range = ContentRange.parse("bytes 0-999/1000");

        Assert.assertTrue(range.hasStartEnd());
        Assert.assertEquals(range.getStart(), 0);
        Assert.assertEquals(range.getEnd(), 999);
        Assert.assertTrue(range.hasInstanceLength());
        Assert.assertEquals(range.getInstanceLength(), 1000);
    }

    @Test
    public void parseRangeNoInstanceLength() throws Exception
    {
        ContentRange range = ContentRange.parse("bytes 100-200/*");

        Assert.assertTrue(range.hasStartEnd());
        Assert.assertEquals(range.getStart(), 100);
        Assert.assertEquals(range.getEnd(), 200);
        Assert.assertFalse(range.hasInstanceLength());
    }

    @Test
    public void parseRangeExtraWhitespaces() throws Exception
    {
        ContentRange range = ContentRange.parse("   bytes   77777 -   99999   /   123456   ");

        Assert.assertTrue(range.hasStartEnd());
        Assert.assertEquals(range.getStart(), 77777);
        Assert.assertEquals(range.getEnd(), 99999);
        Assert.assertTrue(range.hasInstanceLength());
        Assert.assertEquals(range.getInstanceLength(), 123456);
    }

    @Test
    public void parseNoRangeWithInstanceLength() throws Exception
    {
        ContentRange range = ContentRange.parse("bytes */7000");

        Assert.assertFalse(range.hasStartEnd());
        Assert.assertTrue(range.hasInstanceLength());
        Assert.assertEquals(range.getInstanceLength(), 7000);
    }

    @Test
    public void parseNoRangeNoInstanceLength() throws Exception
    {
        ContentRange range = ContentRange.parse("bytes */*");

        Assert.assertFalse(range.hasStartEnd());
        Assert.assertFalse(range.hasInstanceLength());
    }

    @Test
    public void parseNull() throws Exception
    {
        ContentRange range = ContentRange.parse((String)null);

        Assert.assertNull(range);
    }

    @Test(expected=RangeException.class)
    public void parseReversedRange() throws Exception
    {
        ContentRange.parse("bytes 200-100/777");
    }

    @Test(expected=RangeException.class)
    public void parseRangeTooBig() throws Exception
    {
        ContentRange.parse("bytes 0-100/50");
    }

    @Test(expected=RangeException.class)
    public void parseNegativeInstanceLength() throws Exception
    {
        ContentRange.parse("bytes 0-100/-1000");
    }

    @Test(expected=RangeException.class)
    public void parseNegativeEnd() throws Exception
    {
        ContentRange.parse("bytes 0--100/1000");
    }

    @Test(expected=RangeException.class)
    public void parseGarbage() throws Exception
    {
        ContentRange.parse("garbage");
    }

}
