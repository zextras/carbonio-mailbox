// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.TaskRetry.RetryParams;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
    void cancellationDuringRetryPreventsFurtherAttempts() throws Exception {
        CountDownLatch retryStarted = new CountDownLatch(1);
        CountDownLatch retriedAgain = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        RetryParams retry = retryParams();
        retry.setMaxRetries(2);
        scheduler.schedule("cancelled-running-retry", () -> {
            int attempt = calls.incrementAndGet();
            if (attempt == 1) {
                throw ServiceException.FAILURE("temporary failure", null);
            }
            if (attempt == 2) {
                retryStarted.countDown();
                release.await(5, SECONDS);
                throw ServiceException.FAILURE("temporary failure", null);
            }
            retriedAgain.countDown();
            return "done";
        }, 0, retry);

        assertTrue(retryStarted.await(5, SECONDS));
        assertNotNull(scheduler.cancel("cancelled-running-retry", false));
        release.countDown();
        assertFalse(retriedAgain.await(500, TimeUnit.MILLISECONDS),
            "cancelling a running retry must prevent further attempts");
        assertEquals(2, calls.get());
        assertNull(scheduler.getTask("cancelled-running-retry"));
    }

    @Test
    void stillRetriesTransientFailureWhenNotCancelled() throws Exception {
        CountDownLatch completed = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<String> callbackResult = new AtomicReference<>();
        scheduler.addCallback((task, result) -> {
            callbackResult.set(result);
            completed.countDown();
        });
        scheduler.schedule("normal-retry", () -> {
            if (calls.incrementAndGet() == 1) {
                throw ServiceException.FAILURE("temporary failure", null);
            }
            return "done";
        }, 0, retryParams());

        assertTrue(completed.await(5, SECONDS));
        assertEquals(2, calls.get());
        assertEquals("done", callbackResult.get());
        assertEquals("done", scheduler.getLastResult("normal-retry"));
    }

    @Test
    void stopsRetryingWhenTheRetryLimitIsReached() throws Exception {
        CountDownLatch lastAttempt = new CountDownLatch(1);
        CountDownLatch extraAttempt = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        scheduler.schedule("retry-limit", () -> {
            int attempt = calls.incrementAndGet();
            if (attempt == 2) {
                lastAttempt.countDown();
            } else if (attempt > 2) {
                extraAttempt.countDown();
            }
            throw ServiceException.FAILURE("temporary failure", null);
        }, 0, retryParams());

        assertTrue(lastAttempt.await(5, SECONDS));
        assertFalse(extraAttempt.await(500, TimeUnit.MILLISECONDS),
            "one allowed retry must not schedule a third attempt");
        assertEquals(2, calls.get());
        assertNull(scheduler.getLastResult("retry-limit"));
    }

    @Test
    void recurringTaskRunsAgainUntilCancelled() throws Exception {
        CountDownLatch secondRun = new CountDownLatch(1);
        CountDownLatch thirdRun = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        scheduler.schedule("normal-recurring", () -> {
            int attempt = calls.incrementAndGet();
            if (attempt == 2) {
                secondRun.countDown();
                release.await(5, SECONDS);
            } else if (attempt > 2) {
                thirdRun.countDown();
            }
            return "done";
        }, true, 1, 0);

        assertTrue(secondRun.await(5, SECONDS));
        assertNotNull(scheduler.cancel("normal-recurring", false));
        release.countDown();
        assertFalse(thirdRun.await(500, TimeUnit.MILLISECONDS),
            "cancellation must stop further recurring executions");
        assertEquals(2, calls.get());
        assertNull(scheduler.getTask("normal-recurring"));
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

    @Test
    void recurringTaskDoesNotStartAnotherRunWhileItsRetryIsRunning() throws Exception {
        TaskScheduler<String> recurringScheduler = new TaskScheduler<>("recurring-retry", 2, 2);
        CountDownLatch retryStarted = new CountDownLatch(1);
        CountDownLatch overlappingRun = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        try {
            recurringScheduler.schedule("recurring-retry", () -> {
                int attempt = calls.incrementAndGet();
                if (attempt == 1) {
                    throw ServiceException.FAILURE("temporary failure", null);
                }
                if (attempt == 2) {
                    retryStarted.countDown();
                    release.await(5, SECONDS);
                } else {
                    overlappingRun.countDown();
                }
                return "done";
            }, true, 10, 0, retryParams());

            assertTrue(retryStarted.await(5, SECONDS));
            assertFalse(overlappingRun.await(500, TimeUnit.MILLISECONDS),
                "recurrence must not overlap a retry that has not completed");
            assertEquals(2, calls.get());
        } finally {
            recurringScheduler.cancel("recurring-retry", false);
            release.countDown();
            recurringScheduler.shutdown();
        }
    }

    private RetryParams retryParams() {
        RetryParams retry = new RetryParams();
        retry.setDelayPolicy(RetryParams.DelayPolicy.constant);
        retry.setInitialDelay(0);
        retry.setMaxRetries(1);
        return retry;
    }
}
