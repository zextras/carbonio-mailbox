// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.zimbra.common.util.ZimbraLog;
import io.vavr.control.Try;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * Keeps the proxy server IPs for a configurable time, so resolving the client IP on every request
 * does not search LDAP for the proxy servers and resolve their hostnames each time.
 *
 * <p>Once the time to live has passed, the next request reloads the IPs while concurrent requests
 * keep using the previous ones. A failed reload keeps the previous IPs until the time to live
 * passes again. A time to live of zero disables caching. {@code zmprov fc server} (and {@code all})
 * invalidates it.
 */
public class ProxyIpsCache {

  private static final String KEY = "proxyIps";

  private final LoadingCache<String, List<String>> cache;

  public ProxyIpsCache(Supplier<Try<List<String>>> loader, Duration timeToLive, Ticker ticker) {
    final CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().ticker(ticker);
    if (timeToLive.isZero() || timeToLive.isNegative()) {
      builder.maximumSize(0);
    } else {
      builder.refreshAfterWrite(timeToLive);
    }
    this.cache = builder.build(new ProxyIpsLoader(loader));
  }

  public List<String> get() {
    try {
      return cache.getUnchecked(KEY);
    } catch (UncheckedExecutionException e) {
      ZimbraLog.misc.warn("failed to load proxy server IPs, returning none", e.getCause());
      return List.of();
    }
  }

  /** Drops the cached IPs, so the next {@link #get()} loads them again. */
  public void invalidate() {
    cache.invalidateAll();
  }

  private static class ProxyIpsLoader extends CacheLoader<String, List<String>> {

    private final Supplier<Try<List<String>>> loader;

    ProxyIpsLoader(Supplier<Try<List<String>>> loader) {
      this.loader = loader;
    }

    @Override
    public List<String> load(String key) {
      return loader.get().map(List::copyOf).get();
    }

    /** Keeps the previous IPs on failure, so LDAP is not retried on every request. */
    @Override
    public ListenableFuture<List<String>> reload(String key, List<String> previous) {
      return Futures.immediateFuture(
          loader
              .get()
              .<List<String>>map(List::copyOf)
              .onFailure(
                  failure ->
                      ZimbraLog.misc.warn(
                          "failed to reload proxy server IPs, keeping the previous ones", failure))
              .getOrElse(previous));
    }
  }
}
