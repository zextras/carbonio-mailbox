// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Extension of ReentrantReadWriteLock which provides printStackTrace capability via protected methods
 *
 */
public class ZLock extends ReentrantReadWriteLock {

    private static final long serialVersionUID = 6961797355322055852L;

    public ZLock() {
        super();
    }

    public void printStackTrace(StringBuilder out) {
        Thread owner = getOwner();
        if (owner != null) {
            out.append("Write Lock Owner - ");
            printStackTrace(owner, out);
        }
        int readCount = getReadLockCount();
        if (readCount > 0) {
            out.append("Reader Count - " + readCount + "\n");
        }
        for (Thread waiter : getQueuedThreads()) {
            out.append("Lock Waiter - ");
            printStackTrace(waiter, out);
        }
    }

    protected void printStackTrace(Thread thread, StringBuilder out) {
        out.append(thread.getName());
        if (thread.isDaemon()) {
            out.append(" daemon");
        }
        out.append(" prio=").append(thread.getPriority());
        out.append(" id=").append(thread.getId());
        out.append(" state=").append(thread.getState());
        out.append('\n');
        for (StackTraceElement el : thread.getStackTrace()) {
            out.append("\tat ").append(el.toString()).append('\n');
        }
    }
}
