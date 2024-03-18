// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.util.EhcacheManager;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.ehcache.Cache;
import org.ehcache.spi.loaderwriter.CacheLoadingException;
import org.ehcache.spi.loaderwriter.CacheWritingException;

/**
 * IMAP cache using Ehcache's DiskStore.
 *
 * @author ysasaki
 */
final class EhcacheImapCache implements ImapSessionManager.Cache<String, ImapFolder> {
  private final Cache ehcache;
  private final boolean active;
  private Map<String, Long> activeCacheUpdateTimes;
  private static final int ACTIVE_CACHE_THRESHOLD = 1000;

  @SuppressWarnings("serial")
  EhcacheImapCache(String name, boolean active) {
    // If running inside mailboxd, share mailboxd cache, else use separate imap cache.  This avoids
    // issues when running
    // decoupled IMAP service on same host as mailbox.
    EhcacheManager.Service service = EhcacheManager.Service.MAILBOX;
    ehcache = EhcacheManager.getInstance(service).getEhcache(name);
    this.active = active;
    if (active) {
      activeCacheUpdateTimes =
          new LinkedHashMap<String, Long>(ACTIVE_CACHE_THRESHOLD, 0.75f, true) {
            private boolean isExpired(long timestamp) {
              return (timestamp
                  < (System.currentTimeMillis()
                      - (LC.imap_authenticated_max_idle_time.intValue()
                              * Constants.MILLIS_PER_SECOND
                          + 5 * Constants.MILLIS_PER_MINUTE)));
            }

            private boolean removeIfExpired(Cache.Entry<String, Long> entry) {
              if (isExpired(entry.getValue())) {
                doRemove(entry.getKey());
                return true;
              } else {
                return false;
              }
            }

            private void doRemove(String key) {
              ZimbraLog.imap.debug("removing expired active cache element %s", key);
              ehcache.remove(key);
              remove(key);
            }

            protected boolean removeEldestEntry(Cache.Entry<String, Long> eldest) {
              if (removeIfExpired(eldest)) {
                Set<String> keysToRemove = new HashSet<>();
                if (size() > ACTIVE_CACHE_THRESHOLD) {
                  // keep size under threshold when possible - can grow if there are many active
                  // sessions but will shrink as they 'expire'
                  for (Entry<String, Long> entry : this.entrySet()) {
                    if (isExpired(entry.getValue())) {
                      keysToRemove.add(entry.getKey());
                    } else {
                      break;
                    }
                  }
                  for (String key : keysToRemove) {
                    doRemove(key);
                  }
                }
              }
              return false; // always return false per LinkedHashMap:
              // It is permitted for this method to modify the map directly, but if it does so, it
              // must return false
            }
          };
    }
  }

  @Override
  public void put(String key, ImapFolder folder) {
    try {
      if (active) {
        synchronized (activeCacheUpdateTimes) {
          if (!ehcache.containsKey(key)) {
            ehcache.put(key, folder);
            ZimbraLog.imap.debug("put key %s", key);
          }
          long currentTime = System.currentTimeMillis();
          activeCacheUpdateTimes.put(key, currentTime);
        }
      } else {
        if (!ehcache.containsKey(key)) {
          ehcache.put(key, folder);
        }
      }
    } catch (CacheWritingException we) {
      ZimbraLog.imap.error("IMAP cache exception - failed to insert key: %s", key);
    }
  }

  @Override
  public ImapFolder get(String key) {
    try {
      ImapFolder el = null;
      if (active) {
        synchronized (activeCacheUpdateTimes) {
          el = (ImapFolder) ehcache.get(key);
          if (el != null) {
            activeCacheUpdateTimes.put(key, System.currentTimeMillis());
            ZimbraLog.imap.debug("got Element for key %s", key);
          } else {
            ZimbraLog.imap.debug("null get for key %s", key);
          }
        }
      } else {
        el = (ImapFolder) ehcache.get(key);
      }
      return el;
    } catch (CacheLoadingException ce) {
      ZimbraLog.imap.error("IMAP cache exception - removing offending key", ce);
      remove(key, true);
      return null;
    }
  }

  private void remove(String key, boolean quiet) {
    try {
      if (active) {
        synchronized (activeCacheUpdateTimes) {
          ZimbraLog.imap.debug("removing key %s", key);
          activeCacheUpdateTimes.remove(key);
        }
      }
      ehcache.remove(key);
    } catch (CacheWritingException ce) {
      if (!quiet) {
        ZimbraLog.imap.error("IMAP cache exception", ce);
      }
    }
  }

  @Override
  public void remove(String key) {
    remove(key, false);
  }

  @Override
  public void updateAccessTime(String key) {
    if (active) {
      synchronized (activeCacheUpdateTimes) {
        if (activeCacheUpdateTimes.containsKey(key)) {
          activeCacheUpdateTimes.put(key, System.currentTimeMillis());
        } else {
          ZimbraLog.imap.warn("active cache needed update but not found: %s", key);
        }
      }
    } else {
      // inactive expiration not time-based
    }
  }
}
