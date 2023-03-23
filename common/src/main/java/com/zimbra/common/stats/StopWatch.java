// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.stats;

/**
 * A <code>Counter</code> that supports <code>start()</code>
 * and <code>stop()</code> methods for conveniently timing events.
 */
public class StopWatch
extends Counter {

    /**
     * @return current time in millis
     */
    public long start() {
        return System.currentTimeMillis();
    }

    /**
     * Computes elapsed time in millis between start time and now
     *
     * @param startTime start time
     *
     * @return elapsed time in in millis
     */
    public long stop(long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        increment(elapsed);
        return elapsed;
    }
}