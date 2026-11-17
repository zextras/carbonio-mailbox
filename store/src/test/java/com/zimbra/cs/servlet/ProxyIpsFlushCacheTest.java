// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.Ticker;
import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.servlet.ProxyIpsCache;
import com.zimbra.cs.service.admin.FlushCache;
import com.zimbra.soap.admin.type.CacheEntryType;
import com.zimbra.soap.admin.type.CacheSelector;
import io.vavr.control.Try;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProxyIpsFlushCacheTest extends MailboxTestSuite {

  private final AtomicInteger loads = new AtomicInteger();
  private ProxyIpsCache original;

  @BeforeEach
  void installCountingCache() {
    final ProxyIpsCache counting =
        new ProxyIpsCache(
            () -> {
              loads.incrementAndGet();
              return Try.success(List.of("10.0.0.1"));
            },
            Duration.ofMinutes(5),
            Ticker.systemTicker());
    original = ZimbraServlet.replaceProxyIpsCache(counting);
  }

  @AfterEach
  void restoreCache() {
    ZimbraServlet.replaceProxyIpsCache(original);
  }

  @Test
  void shouldReloadProxyIpsAfterFlushingServerCache() throws Exception {
    ZimbraServlet.getTrustedIPs();
    ZimbraServlet.getTrustedIPs();

    flush(CacheEntryType.server);
    ZimbraServlet.getTrustedIPs();

    assertEquals(2, loads.get());
  }

  @Test
  void shouldKeepProxyIpsWhenFlushingAnotherCache() throws Exception {
    ZimbraServlet.getTrustedIPs();

    flush(CacheEntryType.account);
    ZimbraServlet.getTrustedIPs();

    assertEquals(1, loads.get());
  }

  private static void flush(CacheEntryType type) throws Exception {
    FlushCache.doFlush(new HashMap<>(), type, new CacheSelector(false, type.name()));
  }
}
