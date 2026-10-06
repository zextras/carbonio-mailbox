// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet.continuation;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import com.zimbra.common.util.ZimbraLog;

/**
 * {@link AsyncListener} for filters that delay a request: when the delay elapses the request is
 * dispatched so it proceeds to the servlet (Jetty 9 continuation behaviour). Without it Jetty
 * answers the timed out request with HTTP 500 "AsyncContext timeout".
 */
public class DispatchOnTimeoutListener implements AsyncListener {

    private final AtomicBoolean done = new AtomicBoolean(false);

    @Override
    public void onComplete(AsyncEvent event) {
    }

    @Override
    public void onTimeout(AsyncEvent event) {
        if (done.compareAndSet(false, true)) {
            try {
                event.getAsyncContext().dispatch();
            } catch (IllegalStateException ise) {
                ZimbraLog.misc.debug(
                        "ignoring IllegalStateException during dispatch; context may be completed", ise);
            }
        }
    }

    @Override
    public void onError(AsyncEvent event) {
        if (done.compareAndSet(false, true)) {
            try {
                event.getAsyncContext().complete();
            } catch (IllegalStateException ise) {
                ZimbraLog.misc.debug(
                        "ignoring IllegalStateException during complete; context may be completed", ise);
            }
        }
    }

    @Override
    public void onStartAsync(AsyncEvent event) {
    }

    /** Registers the listener and sets the delay on a freshly started async context. */
    public static void delay(AsyncContext asyncContext, long suspendMs) {
        asyncContext.addListener(new DispatchOnTimeoutListener());
        asyncContext.setTimeout(suspendMs);
    }
}
