// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Ticker;
import io.vavr.control.Try;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class ProxyIpsCacheTest {

  private static final Duration TIME_TO_LIVE = Duration.ofMinutes(5);
  private static final Duration PAST_TIME_TO_LIVE = TIME_TO_LIVE.plusSeconds(1);
  private static final List<String> PROXY_IPS = List.of("10.0.0.1");
  private static final List<String> NEW_PROXY_IPS = List.of("10.0.0.2");

  private final FakeTicker ticker = new FakeTicker();

  @Test
  void shouldLoadOnceWhileFresh() {
    final ScriptedLoader loader = new ScriptedLoader(Try.success(PROXY_IPS));
    final ProxyIpsCache cache = cacheWith(loader, TIME_TO_LIVE);

    cache.get();
    ticker.advance(TIME_TO_LIVE.minusSeconds(1));
    cache.get();

    assertEquals(1, loader.loads());
  }

  @Test
  void shouldReloadWhenTimeToLiveHasPassed() {
    final ScriptedLoader loader =
        new ScriptedLoader(Try.success(PROXY_IPS), Try.success(NEW_PROXY_IPS));
    final ProxyIpsCache cache = cacheWith(loader, TIME_TO_LIVE);

    cache.get();
    ticker.advance(PAST_TIME_TO_LIVE);

    assertEquals(NEW_PROXY_IPS, cache.get());
    assertEquals(2, loader.loads());
  }

  @Test
  void shouldKeepPreviousIpsWhenReloadFails() {
    final ScriptedLoader loader = new ScriptedLoader(Try.success(PROXY_IPS), failure());
    final ProxyIpsCache cache = cacheWith(loader, TIME_TO_LIVE);

    cache.get();
    ticker.advance(PAST_TIME_TO_LIVE);

    assertEquals(PROXY_IPS, cache.get());
  }

  @Test
  void shouldNotRetryFailedReloadBeforeTimeToLiveHasPassed() {
    final ScriptedLoader loader = new ScriptedLoader(Try.success(PROXY_IPS), failure());
    final ProxyIpsCache cache = cacheWith(loader, TIME_TO_LIVE);

    cache.get();
    ticker.advance(PAST_TIME_TO_LIVE);
    cache.get();
    cache.get();

    assertEquals(2, loader.loads());
  }

  @Test
  void shouldReturnNoIpsWhenFirstLoadFails() {
    final ProxyIpsCache cache = cacheWith(new ScriptedLoader(failure()), TIME_TO_LIVE);

    assertTrue(cache.get().isEmpty());
  }

  @Test
  void shouldReloadAfterInvalidateEvenWhileFresh() {
    final ScriptedLoader loader =
        new ScriptedLoader(Try.success(PROXY_IPS), Try.success(NEW_PROXY_IPS));
    final ProxyIpsCache cache = cacheWith(loader, TIME_TO_LIVE);

    cache.get();
    cache.invalidate();

    assertEquals(NEW_PROXY_IPS, cache.get());
    assertEquals(2, loader.loads());
  }

  @Test
  void shouldLoadEveryTimeWhenTimeToLiveIsZero() {
    final ScriptedLoader loader = new ScriptedLoader(Try.success(PROXY_IPS));
    final ProxyIpsCache cache = cacheWith(loader, Duration.ZERO);

    cache.get();
    cache.get();

    assertEquals(2, loader.loads());
  }

  private ProxyIpsCache cacheWith(ScriptedLoader loader, Duration timeToLive) {
    return new ProxyIpsCache(loader, timeToLive, ticker);
  }

  private static Try<List<String>> failure() {
    return Try.failure(new IllegalStateException("LDAP unavailable"));
  }

  /** Returns the given results in order, then keeps returning the last one. */
  private static class ScriptedLoader implements Supplier<Try<List<String>>> {

    private final Deque<Try<List<String>>> results;
    private int loads;

    @SafeVarargs
    ScriptedLoader(Try<List<String>>... results) {
      this.results = new ArrayDeque<>(List.of(results));
    }

    @Override
    public Try<List<String>> get() {
      loads++;
      return results.size() > 1 ? results.poll() : results.peek();
    }

    int loads() {
      return loads;
    }
  }

  private static class FakeTicker extends Ticker {

    private long nanos;

    void advance(Duration duration) {
      nanos += duration.toNanos();
    }

    @Override
    public long read() {
      return nanos;
    }
  }
}
