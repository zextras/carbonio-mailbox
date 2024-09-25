// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.jetty;

import org.eclipse.jetty.util.thread.ThreadPool;

/**
 * Holds on to a reference to the Jetty thread pool.  Called
 * by the Jetty startup code (see store module, com.zextras.mailbox.Mailbox class).
 */
public class JettyMonitor {

    private static ThreadPool threadPool = null;
    
    public static synchronized void setThreadPool(ThreadPool pool) {
        System.out.println(JettyMonitor.class.getSimpleName() + " monitoring thread pool " + pool);
        threadPool = pool;
    }
    
    public static synchronized ThreadPool getThreadPool() {
        return threadPool;
    }
}
