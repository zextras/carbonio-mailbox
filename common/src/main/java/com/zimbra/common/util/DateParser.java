// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Wrapper around {@link SimpleDateFormat} that allows it to be used in a
 * static context.  <tt>DateParser</tt> automatically handles using
 * <tt>ThreadLocal</tt> to allocate one <tt>SimpleDateFormat</tt> per
 * thread.
 */
public class DateParser {

    private ThreadLocal<SimpleDateFormat> mFormatterHolder = new ThreadLocal<SimpleDateFormat>();
    private String mDatePattern;
    
    public DateParser(String datePattern) {
        mDatePattern = datePattern;
    }
    
    public Date parse(String s) {
        return getFormatter().parse(s, new ParsePosition(0));
    }
    
    public String format(Date date) {
        return getFormatter().format(date);
    }
    
    private SimpleDateFormat getFormatter() {
        SimpleDateFormat formatter = mFormatterHolder.get();
        if (formatter == null) {
            formatter = new SimpleDateFormat(mDatePattern);
            mFormatterHolder.set(formatter);
        }
        return formatter;
    }
}
