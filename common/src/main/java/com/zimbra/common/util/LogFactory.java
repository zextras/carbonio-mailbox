// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.zimbra.common.localconfig.LC;

/**
 * Factory that creates and manages instances of {@link Log}.
 *
 * @author bburtin
 */
public final class LogFactory {

    private static final ConcurrentMap<String, Log> NAME2LOG = new ConcurrentHashMap<String, Log>();

    private LogFactory() {
    }

    public synchronized static void init() {
        PropertyConfigurator.configure(LC.zimbra_log4j_properties.value());
    }

    public synchronized static void reset() {
        ZimbraLog.misc.info("Resetting all loggers");
        // LogManager.resetConfiguration() will set all logger's level to null. We don't want to leave account loggers
        // with that state.
        for (Log log : getAllLoggers()) {
            log.removeAccountLoggers();
        }
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(LC.zimbra_log4j_properties.value());
    }

    public static Log getLog(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return getLog(clazz.getName());
    }

    public static Log getLog(String name) {
        Log log = NAME2LOG.get(name);
        if (log == null) {
            log = new Log(Logger.getLogger(name));
            Log prev = NAME2LOG.putIfAbsent(name, log);
            if (prev != null) {
                log = prev;
            }
        }
        return log;
    }

    /**
     * Returns <tt>true</tt> if a logger with the given name exists.
     */
    public static boolean logExists(String name) {
        return (LogManager.exists(name) != null);
    }

    /**
     * Returns all account loggers that have been created since the last server start, or
     * an empty <tt>List</tt>.
     */
    public static List<AccountLogger> getAllAccountLoggers() {
        List<AccountLogger> accountLoggers = new ArrayList<AccountLogger>();
        List<Log> allLogs = new ArrayList<Log>(NAME2LOG.values());
        for (Log log : allLogs) {
            List<AccountLogger> alForCategory = log.getAccountLoggers();
            if (alForCategory != null) {
                accountLoggers.addAll(alForCategory);
            }
        }
        return accountLoggers;
    }

    /**
     * Returns all the loggers that have been created with {@link #getLog}.  The
     * <tt>Collection</tt> does not include account loggers.
     */
    public static Collection<Log> getAllLoggers() {
        return new ArrayList<Log>(NAME2LOG.values());
    }
}
