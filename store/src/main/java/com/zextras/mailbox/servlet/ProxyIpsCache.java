// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import com.zimbra.common.util.ZimbraLog;
import io.vavr.control.Try;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Keeps the proxy server IPs for a configurable time, so resolving the client IP on every request
 * does not search LDAP for the proxy servers and resolve their hostnames each time.
 *
 * <p>Only one thread reloads an expired entry; the others keep using the previous one meanwhile.
 * When a reload fails the previous IPs are kept until the next attempt. {@code zmprov fc server}
 * (and {@code all}) invalidates it.
 */
public class ProxyIpsCache {

  private final Supplier<Try<List<String>>> loader;
  private final Supplier<Duration> timeToLive;
  private final Clock clock;
  private final ReentrantLock reloading = new ReentrantLock();
  private final AtomicReference<Snapshot> snapshot = new AtomicReference<>();

  public ProxyIpsCache(
      Supplier<Try<List<String>>> loader, Supplier<Duration> timeToLive, Clock clock) {
    this.loader = loader;
    this.timeToLive = timeToLive;
    this.clock = clock;
  }

  public List<String> get() {
    final Snapshot current = snapshot.get();
    if (current != null && current.isFreshAt(clock.instant())) {
      return current.proxyIps();
    }
    if (current != null && !reloading.tryLock()) {
      return current.proxyIps();
    }
    if (current == null) {
      reloading.lock();
    }
    try {
      return reload();
    } finally {
      reloading.unlock();
    }
  }

  /** Drops the cached IPs, so the next {@link #get()} loads them again. */
  public void invalidate() {
    reloading.lock();
    try {
      snapshot.set(null);
    } finally {
      reloading.unlock();
    }
  }

  private List<String> reload() {
    final Snapshot current = snapshot.get();
    final Instant now = clock.instant();
    if (current != null && current.isFreshAt(now)) {
      return current.proxyIps();
    }
    final Instant expiry = now.plus(timeToLive.get());
    return loader
        .get()
        .map(List::copyOf)
        .peek(proxyIps -> snapshot.set(new Snapshot(proxyIps, expiry)))
        .getOrElseGet(failure -> keepPrevious(current, expiry, failure));
  }

  private List<String> keepPrevious(Snapshot previous, Instant expiry, Throwable failure) {
    if (previous == null) {
      ZimbraLog.misc.warn("failed to get proxy server IPs, none will be trusted", failure);
      return List.of();
    }
    ZimbraLog.misc.warn("failed to reload proxy server IPs, keeping the previous ones", failure);
    snapshot.set(new Snapshot(previous.proxyIps(), expiry));
    return previous.proxyIps();
  }

  private record Snapshot(List<String> proxyIps, Instant expiry) {

    boolean isFreshAt(Instant now) {
      return now.isBefore(expiry);
    }
  }
}
