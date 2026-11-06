// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.TaskRetry.RetryParams;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TaskSchedulerTest {

    private final TaskScheduler<String> scheduler = new TaskScheduler<>("test", 1, 1);
    private final CountDownLatch release = new CountDownLatch(1);

    @AfterEach
    void tearDown() {
        release.countDown();
        scheduler.shutdown();
    }

    @Test
    void cancellationWhileRunningPreventsFailureFromSchedulingRetry() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch retried = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        scheduler.schedule("cancelled-retry", () -> {
            if (calls.incrementAndGet() == 1) {
                started.countDown();
                release.await(5, SECONDS);
                throw ServiceException.FAILURE("temporary failure", null);
            }
            retried.countDown();
            return "done";
        }, 0, retryParams());

        assertTrue(started.await(5, SECONDS));
        assertNotNull(scheduler.cancel("cancelled-retry", false));
        release.countDown();
        assertFalse(retried.await(500, TimeUnit.MILLISECONDS), "cancelled task must not be retried");
        assertEquals(1, calls.get());
        assertNull(scheduler.getTask("cancelled-retry"));
    }

    @Test
    void stillRetriesTransientFailureWhenNotCancelled() throws Exception {
        CountDownLatch retried = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        scheduler.schedule("normal-retry", () -> {
            if (calls.incrementAndGet() == 1) {
                throw ServiceException.FAILURE("temporary failure", null);
            }
            retried.countDown();
            return "done";
        }, 0, retryParams());

        assertTrue(retried.await(5, SECONDS));
        assertEquals(2, calls.get());
    }

    @Test
    void cancelledRecurringTaskDoesNotRunAgain() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch repeated = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        scheduler.schedule("recurring", () -> {
            if (calls.incrementAndGet() == 1) {
                started.countDown();
                release.await(5, SECONDS);
            } else {
                repeated.countDown();
            }
            return "done";
        }, true, 1, 0);

        assertTrue(started.await(5, SECONDS));
        scheduler.cancel("recurring", false);
        release.countDown();
        assertFalse(repeated.await(500, TimeUnit.MILLISECONDS));
    }

    private RetryParams retryParams() {
        RetryParams retry = new RetryParams();
        retry.setDelayPolicy(RetryParams.DelayPolicy.constant);
        retry.setInitialDelay(0);
        retry.setMaxRetries(1);
        return retry;
    }
}
