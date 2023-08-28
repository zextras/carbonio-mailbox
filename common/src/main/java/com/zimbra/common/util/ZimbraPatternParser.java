// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Formats the <tt>%z</tt> pattern as all the keys and values passed
 * to {@link ZimbraLog#addToContext}.
 *  
 * @author bburtin
 */
public class ZimbraPatternParser
extends PatternParser {

    ZimbraPatternLayout mLayout;
    
    ZimbraPatternParser(String pattern, ZimbraPatternLayout layout) {
        super(pattern);
        mLayout = layout;
    }
      
    public void finalizeConverter(char c) {
        if (c == 'z') {
            addConverter(new ZimbraPatternConverter(formattingInfo));
            currentLiteral.setLength(0);
        } else {
            super.finalizeConverter(c);
        }
    }

    private class ZimbraPatternConverter extends PatternConverter {
        ZimbraPatternConverter(FormattingInfo formattingInfo) {
            super(formattingInfo);
        }

        public String convert(LoggingEvent event) {
            return ZimbraLog.getContextString();
        }
    }  
}
