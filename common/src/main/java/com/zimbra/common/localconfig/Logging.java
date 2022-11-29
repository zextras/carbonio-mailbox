// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.localconfig;

import com.zimbra.common.util.ZimbraLog;

/*
 * Log4j is expensive for command line invocation
 */
public class Logging {

    private static boolean sQuietMode = false;
    
    public static void setQuietMode(boolean value) {
        sQuietMode = value;
    }

    private static boolean sUseZimbraLog = true;

    public static void setUseZimbraLog(boolean value) {
        sUseZimbraLog = value;
    }

    public static void warn(String message) {
        warn(message, null);
    }

    public static void warn(String message, Exception e) {
        if (sQuietMode) {
            return;
        }
        if (sUseZimbraLog) {
            ZimbraLog.misc.warn(message, e);
        } else {
            System.err.println("Warning: " + message);
            if (e != null) {
                System.err.println(e);
                e.printStackTrace(System.err);
            }
        }
       
    }

    public static void error(String message) {
        error(message, null);
    }

    public static void error(String message, Exception e) {
        if (sQuietMode) {
            return;
        }
        if (sUseZimbraLog) {
            ZimbraLog.misc.warn(message, e);
        } else {
            System.err.println("Error: " + message);
            if (e != null) {
                System.err.println(e);
                e.printStackTrace(System.err);
            }
        }
    }
}
