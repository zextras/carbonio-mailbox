// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.lock;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lock owner and hold count tracker for debugging read lock
 */
public class MailboxLockOwner {

    private AtomicInteger count = new AtomicInteger(0);
    private final Thread owner;

    MailboxLockOwner() {
        owner = Thread.currentThread();
    }

    void increment() {
        count.incrementAndGet();
    }

    int decrement() {
        return count.decrementAndGet();
    }

    int getCount() {
        return count.get();
    }

    Thread getOwnerThread() {
        return owner;
    }
}
