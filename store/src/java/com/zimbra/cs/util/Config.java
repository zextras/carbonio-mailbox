// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import java.io.File;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.DbConfig;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;

/**
 * @since Apr 19, 2004
 * @author schemers
 */
public final class Config {

    public static final String KEY_PURGE_LAST_MAILBOX_ID = "purge.lastMailboxId";
    public static final String CONTACT_BACKUP_LAST_MAILBOX_ID = "contactBackup.lastMailboxId";

    public static final int D_LMTP_THREADS = 10;

    public static final int D_LMTP_BIND_PORT = 7025;
    public static final int D_IMAP_BIND_PORT = 143;
    public static final int D_IMAP_SSL_BIND_PORT = 993;
    public static final int D_POP3_BIND_PORT = 110;
    public static final int D_POP3_SSL_BIND_PORT = 995;
    public static final int D_MILTER_BIND_PORT = 7026;

    public static final int D_SMTP_TIMEOUT = 60;
    public static final int D_SMTP_PORT = 25;

    private static Map<String, DbConfig> mConfigMap;
    private static Timestamp mYoungest;

    private static void init(Timestamp ts) throws ServiceException {
        DbConnection conn = null;
        try {
            conn = DbPool.getConnection();
            mConfigMap = DbConfig.getAll(conn, ts);
            for (Iterator<DbConfig> it = mConfigMap.values().iterator(); it.hasNext();) {
                DbConfig c = it.next();
                if (mYoungest == null) {
                    mYoungest = c.getModified();
                } else if (c.getModified().after(mYoungest)) {
                    mYoungest = c.getModified();
                }
            }
        } finally {
            if (conn != null)
                DbPool.quietClose(conn);
        }
    }

    private static synchronized void initConfig() {
        if (mConfigMap == null) {
            try {
                init(null);
            } catch (Exception e) {
                Zimbra.halt("Config initialization failed", e);
            }
        }
    }

    /**
     * @param name
     * @return specified config item as a String, null if it doesn't exist.
     */
    public static synchronized String getString(String name, String defaultValue) {
        initConfig();
        DbConfig c = mConfigMap.get(name);
        return c != null ? c.getValue() : defaultValue;
    }

    public static synchronized void setString(String name, String value)
    throws ServiceException {
        initConfig();
        DbConnection conn = null;
        try {
            conn = DbPool.getConnection();
            DbConfig c = DbConfig.set(conn, name, value);
            mConfigMap.put(name, c);
            conn.commit();
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static synchronized int getInt(String name, int defaultValue) {
        initConfig();
        String value = getString(name, null);
        if (value == null) {
            return defaultValue;
        }
        int intValue = defaultValue;
        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            ZimbraLog.misc.warn("Invalid integer value '%s' for config key '%s'.  Returning default value %d.",
                value, name, intValue);
        }
        return intValue;
    }

    public static synchronized void setInt(String name, int value)
    throws ServiceException {
        initConfig();
        setString(name, Integer.toString(value));
    }

    public static synchronized void setLong(String name, long value)
    throws ServiceException {
        initConfig();
        setString(name, Long.toString(value));
    }

    public static synchronized long getLong(String name, long defaultValue) {
        initConfig();
        String value = getString(name, null);
        if (value == null) {
            return defaultValue;
        }
        long longValue = defaultValue;
        try {
            longValue = Long.parseLong(value);
        } catch (NumberFormatException e) {
            ZimbraLog.misc.warn("Invalid long value '%s' for config key '%s'.  Returning default value %d.",
                value, name, longValue);
        }
        return longValue;
    }

    /**
     * Returns <tt>true</tt> if value in config equals (ignoring case): "yes", "true", or "1".
     * @param name
     * @return specified config item as a boolean, defaultValue if it doesn't exist.
     */
    public static synchronized boolean getBoolean(String name, boolean defaultValue) {
        initConfig();
        String value = getString(name, null);
        return value == null ? defaultValue :
            (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") ||
            value.equals("1"));
    }

    /**
     * Returns a File object representing the path relative to the
     * Zimbra home directory.
     * @param path
     * @return
     */
    public static File getPathRelativeToZimbraHome(String path) {
        char first = path.charAt(0);
        if (first == File.separatorChar || first == '/')
            return new File(path);

        String home = LC.zimbra_home.value();
        return new File(home, path);
    }

}
