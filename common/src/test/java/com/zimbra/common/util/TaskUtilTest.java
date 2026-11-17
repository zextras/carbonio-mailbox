// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

class TaskUtilTest {

  @Test
  void callReturnsResultWithinTimeout() throws Exception {
    assertEquals("done", TaskUtil.call(() -> "done", 10_000));
    assertFalse(Thread.currentThread().isInterrupted());
  }

  @Test
  void callThrowsTimeoutWhenTaskIsTooSlow() {
    assertThrows(
        TimeoutException.class,
        () -> TaskUtil.call(() -> {
          Thread.sleep(10_000);
          return "late";
        }, 50));
    assertFalse(Thread.currentThread().isInterrupted());
  }

  @Test
  void callPropagatesTaskException() {
    assertThrows(
        IllegalStateException.class,
        () -> TaskUtil.call(() -> {
          throw new IllegalStateException("boom");
        }, 10_000));
  }

  @Test
  void daemonThreadFactoryNamesThreadsWithPrefix() {
    ThreadFactory factory = TaskUtil.newDaemonThreadFactory("worker");

    assertEquals("worker-1", factory.newThread(() -> {}).getName());
    assertEquals("worker-2", factory.newThread(() -> {}).getName());
  }

  @Test
  void daemonThreadFactoryDefaultsPrefixToPool() {
    assertEquals("pool-1", TaskUtil.newDaemonThreadFactory(null).newThread(() -> {}).getName());
    assertEquals("pool-1", TaskUtil.newDaemonThreadFactory("").newThread(() -> {}).getName());
  }

  @Test
  void daemonThreadFactoryMakesThreadsOfUserThreadsDaemon() throws Exception {
    Thread thread = createFrom(TaskUtil.newDaemonThreadFactory("worker"), false, Thread.NORM_PRIORITY);

    assertTrue(thread.isDaemon());
    assertEquals(Thread.NORM_PRIORITY, thread.getPriority());
  }

  @Test
  void daemonThreadFactoryResetsPriorityInheritedFromCreator() throws Exception {
    Thread thread = createFrom(TaskUtil.newDaemonThreadFactory("worker"), true, Thread.MIN_PRIORITY);

    assertTrue(thread.isDaemon());
    assertEquals(Thread.NORM_PRIORITY, thread.getPriority());
  }

  private static Thread createFrom(ThreadFactory factory, boolean daemonCreator, int creatorPriority)
      throws Exception {
    CompletableFuture<Thread> created = new CompletableFuture<>();
    Thread creator = new Thread(() -> created.complete(factory.newThread(() -> {})));
    creator.setDaemon(daemonCreator);
    creator.setPriority(creatorPriority);
    creator.start();
    return created.get();
  }
}
