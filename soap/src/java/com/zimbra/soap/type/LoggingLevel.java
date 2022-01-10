// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Arrays;
import java.util.Locale;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import com.zimbra.common.service.ServiceException;

/**
 * relates to Logging levels.
 */
@XmlEnum
public enum LoggingLevel {
    // keep in sync with com.zimbra.common.util.Log.Level
    @XmlEnumValue("error") error(com.zimbra.common.util.Log.Level.error),
    @XmlEnumValue("warn") warn(com.zimbra.common.util.Log.Level.warn),
    @XmlEnumValue("info") info(com.zimbra.common.util.Log.Level.info),
    @XmlEnumValue("debug") debug(com.zimbra.common.util.Log.Level.debug),
    @XmlEnumValue("trace") trace(com.zimbra.common.util.Log.Level.trace);

    private final com.zimbra.common.util.Log.Level commonLevel;

    private LoggingLevel(com.zimbra.common.util.Log.Level commonLvl) {
        this.commonLevel = commonLvl;
    }

    public com.zimbra.common.util.Log.Level fromJaxb() {
        return commonLevel;
    }

    public static LoggingLevel toJaxb(com.zimbra.common.util.Log.Level commonLvl) {
        for (LoggingLevel ll : LoggingLevel.values()) {
            if (ll.fromJaxb() == commonLvl) {
                return ll;
            }
        }
        throw new IllegalArgumentException("Unrecognised Level:" + commonLvl);
    }

    public static LoggingLevel fromString(String s) throws ServiceException {
        try {
            return LoggingLevel.valueOf(s.toLowerCase(Locale.ENGLISH));
        } catch (IllegalArgumentException iae) {
            throw ServiceException.INVALID_REQUEST("unknown Logging Level: " + s + ", valid values: " +
                            Arrays.asList(LoggingLevel.values()), null);
        }
    }
}
