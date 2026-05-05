// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.util.EhcacheManager;
import org.ehcache.Cache;
import org.ehcache.spi.loaderwriter.CacheLoadingException;
import org.ehcache.spi.loaderwriter.CacheWritingException;

/**
 * IMAP cache using Caffeine for active sessions and Ehcache's DiskStore for inactive sessions.
 *
 * @author ysasaki
 */
final class EhcacheImapCache implements ImapSessionManager.Cache<String, ImapFolder> {
  private final com.github.benmanes.caffeine.cache.Cache<String, ImapFolder> caffeineCache;
  private final Cache<String, ImapFolder> ehcache;
  private final boolean active;

  EhcacheImapCache(String name, boolean active) {
    // If running inside mailboxd, share mailboxd cache, else use separate imap cache.  This avoids
    // issues when running decoupled IMAP service on same host as mailbox.
    EhcacheManager.Service service = EhcacheManager.Service.MAILBOX;
    this.active = active;
    if (active) {
      caffeineCache = EhcacheManager.getInstance(service).getActiveSessionCaffeineCache();
      ehcache = null;
    } else {
      caffeineCache = null;
      ehcache = EhcacheManager.getInstance(service).getEhcache(name);
    }
  }

  @Override
  public void put(String key, ImapFolder folder) {
    if (active) {
      caffeineCache.put(key, folder);
      ZimbraLog.imap.debug("put key %s", key);
    } else {
      try {
        if (!ehcache.containsKey(key)) {
          ehcache.put(key, folder);
        }
      } catch (CacheWritingException we) {
        ZimbraLog.imap.error("IMAP cache exception - failed to insert key: %s", key);
      }
    }
  }

  @Override
  public ImapFolder get(String key) {
    if (active) {
      ImapFolder el = caffeineCache.getIfPresent(key);
      if (el != null) {
        ZimbraLog.imap.debug("got Element for key %s", key);
      } else {
        ZimbraLog.imap.debug("null get for key %s", key);
      }
      return el;
    } else {
      try {
        return ehcache.get(key);
      } catch (CacheLoadingException ce) {
        ZimbraLog.imap.error("IMAP cache exception - removing offending key", ce);
        remove(key);
        return null;
      }
    }
  }

  @Override
  public void remove(String key) {
    if (active) {
      caffeineCache.invalidate(key);
      ZimbraLog.imap.debug("removing key %s", key);
    } else {
      try {
        ehcache.remove(key);
      } catch (CacheWritingException ce) {
        ZimbraLog.imap.error("IMAP cache exception", ce);
      }
    }
  }

  @Override
  public void updateAccessTime(String key) {
    if (active) {
      // Caffeine expireAfterAccess updates on get(); explicit touch via getIfPresent
      caffeineCache.getIfPresent(key);
    }
    // inactive: no-op
  }
}
