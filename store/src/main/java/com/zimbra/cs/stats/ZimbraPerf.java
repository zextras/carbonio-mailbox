// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.stats;

import com.google.common.collect.Maps;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.stats.Counter;
import com.zimbra.common.stats.CsvStatsDumper;
import com.zimbra.common.stats.RealtimeStats;
import com.zimbra.common.stats.RealtimeStatsCallback;
import com.zimbra.common.stats.StatsScheduler;
import com.zimbra.common.stats.StopWatch;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.mailbox.MailboxManager;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** A collection of methods for keeping track of server performance statistics. */
public class ZimbraPerf {

  public enum ServerID {
    ZIMBRA
  }

  @Target({ElementType.FIELD})
  @Retention(RetentionPolicy.RUNTIME)
  private @interface Description {
    String value();
  }

  @Description("Number of database connections in use")
  public static final String RTS_DB_POOL_SIZE = "db_pool_size";

  @Description("InnoDB buffer pool hit rate")
  public static final String RTS_INNODB_BP_HIT_RATE = "innodb_bp_hit_rate";

  @Description("Number of LMTP connections")
  public static final String RTS_LMTP_CONN = "lmtp_conn";

  @Description("Number of LMTP handler threads")
  public static final String RTS_LMTP_THREADS = "lmtp_threads";

  @Description("Number of cleartext POP3 connections")
  public static final String RTS_POP_CONN = "pop_conn";

  @Description("Number of POP3 handler threads")
  public static final String RTS_POP_THREADS = "pop_threads";

  @Description("Number of SSL POP3 connections")
  public static final String RTS_POP_SSL_CONN = "pop_ssl_conn";

  @Description("Number of POP3 SSL handler threads")
  public static final String RTS_POP_SSL_THREADS = "pop_ssl_threads";

  @Description("Number of cleartext IMAP connections")
  public static final String RTS_IMAP_CONN = "imap_conn";

  @Description("Number of IMAP handler threads")
  public static final String RTS_IMAP_THREADS = "imap_threads";

  @Description("Number of SSL IMAP connections")
  public static final String RTS_IMAP_SSL_CONN = "imap_ssl_conn";

  @Description("Number of IMAP SSL handler threads")
  public static final String RTS_IMAP_SSL_THREADS = "imap_ssl_threads";

  @Description("Number of HTTP handler threads")
  public static final String RTS_HTTP_THREADS = "http_threads";

  @Description("Number of idle HTTP handler threads")
  public static final String RTS_HTTP_IDLE_THREADS = "http_idle_threads";

  @Description("Number of SOAP sessions")
  public static final String RTS_SOAP_SESSIONS = "soap_sessions";

  @Description("Number of mailboxes cached in memory")
  public static final String RTS_MBOX_CACHE_SIZE = "mbox_cache_size";

  @Description("Number of message structures cached in memory")
  public static final String RTS_MSG_CACHE_SIZE = "msg_cache_size";

  @Description("Number of open file descriptors that reference message content")
  public static final String RTS_FD_CACHE_SIZE = "fd_cache_size";

  @Description("LDAP account cache size")
  public static final String RTS_ACCOUNT_CACHE_SIZE = "account_cache_size";

  @Description("LDAP COS cache size")
  public static final String RTS_COS_CACHE_SIZE = "cos_cache_size";

  @Description("LDAP domain cache size")
  public static final String RTS_DOMAIN_CACHE_SIZE = "domain_cache_size";

  @Description("LDAP server cache size")
  public static final String RTS_SERVER_CACHE_SIZE = "server_cache_size";

  @Description("LDAP UC service cache size")
  public static final String RTS_UCSERVICE_CACHE_SIZE = "ucservice_cache_size";

  @Description("LDAP zimlet cache size")
  public static final String RTS_ZIMLET_CACHE_SIZE = "zimlet_cache_size";

  @Description("LDAP group cache size")
  public static final String RTS_GROUP_CACHE_SIZE = "group_cache_size";

  @Description("LDAP XMPP cache size")
  public static final String RTS_XMPP_CACHE_SIZE = "xmpp_cache_size";

  // Accumulators.  To add a new accumulator, create a static instance here and
  // add it to sAccumulators.
  @Description("Number of messages received over LMTP")
  public static final String DC_LMTP_RCVD_MSGS = "lmtp_rcvd_msgs";

  public static final Counter COUNTER_LMTP_RCVD_MSGS = new Counter(DC_LMTP_RCVD_MSGS);

  @Description("Number of bytes received over LMTP")
  public static final String DC_LMTP_RCVD_BYTES = "lmtp_rcvd_bytes";

  public static final Counter COUNTER_LMTP_RCVD_BYTES = new Counter(DC_LMTP_RCVD_BYTES);

  @Description("Number of LMTP recipients")
  public static final String DC_LMTP_RCVD_RCPT = "lmtp_rcvd_rcpt";

  public static final Counter COUNTER_LMTP_RCVD_RCPT = new Counter(DC_LMTP_RCVD_RCPT);

  @Description("Number of messages delivered to mailboxes as a result of LMTP delivery")
  public static final String DC_LMTP_DLVD_MSGS = "lmtp_dlvd_msgs";

  public static final Counter COUNTER_LMTP_DLVD_MSGS = new Counter(DC_LMTP_DLVD_MSGS);

  @Description("Number of bytes of data delivered to mailboxes as a result of LMTP delivery")
  public static final String DC_LMTP_DLVD_BYTES = "lmtp_dlvd_bytes";

  public static final Counter COUNTER_LMTP_DLVD_BYTES = new Counter(DC_LMTP_DLVD_BYTES);

  // Stop watch

  @Description("Number of times that the server got a database connection from the pool")
  public static final String DC_DB_CONN_COUNT = "db_conn_count";

  public static final StopWatch STOPWATCH_DB_CONN = new StopWatch(DC_DB_CONN_COUNT);

  @Description("Number of times that the server got an LDAP directory context")
  public static final String DC_LDAP_DC_COUNT = "ldap_dc_count";

  public static final StopWatch STOPWATCH_LDAP_DC = new StopWatch(DC_LDAP_DC_COUNT);

  @Description("Number of messages that were added to a mailbox")
  public static final String DC_MBOX_ADD_MSG_COUNT = "mbox_add_msg_count";

  public static final StopWatch STOPWATCH_MBOX_ADD_MSG = new StopWatch(DC_MBOX_ADD_MSG_COUNT);

  @Description("Number of times that the server got a mailbox from the cache")
  public static final String DC_MBOX_GET_COUNT = "mbox_get_count";

  public static final StopWatch STOPWATCH_MBOX_GET =
      new StopWatch(DC_MBOX_GET_COUNT); // Mailbox accessor response time

  // Mailbox cache
  @Description("Mailbox cache hit rate")
  public static final String DC_MBOX_CACHE = "mbox_cache";

  public static final Counter COUNTER_MBOX_CACHE =
      new Counter(DC_MBOX_CACHE); // Mailbox cache hit rate

  @Description("Message cache hit rate")
  public static final String DC_MBOX_MSG_CACHE = "mbox_msg_cache";

  public static final Counter COUNTER_MBOX_MSG_CACHE = new Counter(DC_MBOX_MSG_CACHE);

  @Description("Item cache hit rate")
  public static final String DC_MBOX_ITEM_CACHE = "mbox_item_cache";

  public static final Counter COUNTER_MBOX_ITEM_CACHE = new Counter(DC_MBOX_ITEM_CACHE);

  // Stop watch
  @Description("Number of SOAP requests received")
  public static final String DC_SOAP_COUNT = "soap_count";

  public static final StopWatch STOPWATCH_SOAP = new StopWatch(DC_SOAP_COUNT);

  @Description("Number of IMAP requests received")
  public static final String DC_IMAP_COUNT = "imap_count";

  public static final StopWatch STOPWATCH_IMAP = new StopWatch(DC_IMAP_COUNT);

  @Description("Number of POP3 requests received")
  public static final String DC_POP_COUNT = "pop_count";

  public static final StopWatch STOPWATCH_POP = new StopWatch(DC_POP_COUNT);

  // Calendar
  @Description("Hit rate of calendar summary cache, counting cache hit from both memory and file")
  public static final String DC_CALCACHE_HIT = "calcache_hit";

  public static final Counter COUNTER_CALENDAR_CACHE_HIT = new Counter(DC_CALCACHE_HIT);

  @Description("Hit rate of calendar summary cache, counting cache hit from memory only")
  public static final String DC_CALCACHE_MEM_HIT = "calcache_mem_hit";

  public static final Counter COUNTER_CALENDAR_CACHE_MEM_HIT = new Counter(DC_CALCACHE_MEM_HIT);

  @Description("Number of calendars (folders) in the calendar summary cache LRU in Java heap")
  public static final String DC_CALCACHE_LRU_SIZE = "calcache_lru_size";

  public static final Counter COUNTER_CALENDAR_CACHE_LRU_SIZE = new Counter(DC_CALCACHE_LRU_SIZE);

  // Lucene
  // TODO: this is unused, check it:
  // public static final Counter COUNTER_IDX_WRT = new Counter(name);

  @Description("Accumulated number of index writers opened")
  public static final String DC_IDX_WRT_OPENED = "idx_wrt_opened";

  public static final Counter COUNTER_IDX_WRT_OPENED = new Counter(DC_IDX_WRT_OPENED);

  @Description("Accumulated bytes written by Lucene")
  public static final String DC_IDX_BYTES_WRITTEN = "idx_bytes_written";

  public static final Counter COUNTER_IDX_BYTES_WRITTEN = new Counter(DC_IDX_BYTES_WRITTEN);

  @Description("Accumulated bytes read by Lucene")
  public static final String DC_IDX_BYTES_READ = "idx_bytes_read";

  public static final Counter COUNTER_IDX_BYTES_READ = new Counter(DC_IDX_BYTES_READ);

  // File operations
  @Description("Number of times that the file descriptor cache read message data from disk")
  public static final String DC_BIS_READ = "bis_read";

  public static final Counter COUNTER_BLOB_INPUT_STREAM_READ = new Counter(DC_BIS_READ);

  @Description("Percentage of file descriptor cache disk reads that required a seek")
  public static final String DC_BIS_SEEK_RATE = "bis_seek_rate";

  public static final Counter COUNTER_BLOB_INPUT_STREAM_SEEK_RATE = new Counter(DC_BIS_SEEK_RATE);

  // Trackers for duration
  public static final ActivityTracker SOAP_TRACKER = new ActivityTracker("soap");
  public static final ActivityTracker IMAP_TRACKER = new ActivityTracker("imap");
  public static final ActivityTracker POP_TRACKER = new ActivityTracker("pop3");
  public static final ActivityTracker LDAP_TRACKER = new ActivityTracker("ldap");
  public static final ActivityTracker SQL_TRACKER = new ActivityTracker("sql");

  public static final ActivityTracker SOAP_TRACKER_PROMETHEUS = new ActivityTracker("soap");
  public static final ActivityTracker IMAP_TRACKER_PROMETHEUS = new ActivityTracker("imap");
  public static final ActivityTracker POP_TRACKER_PROMETHEUS = new ActivityTracker("pop3");
  public static final ActivityTracker LDAP_TRACKER_PROMETHEUS = new ActivityTracker("ldap");
  public static final ActivityTracker SQL_TRACKER_PROMETHEUS = new ActivityTracker("sql");

  private static final Log log = LogFactory.getLog(ZimbraPerf.class);
  private static final String[] mboxRealtimeStatsNames =
      new String[] {
        RTS_DB_POOL_SIZE,
        RTS_INNODB_BP_HIT_RATE,
        RTS_LMTP_CONN,
        RTS_LMTP_THREADS,
        RTS_POP_CONN,
        RTS_POP_THREADS,
        RTS_POP_SSL_CONN,
        RTS_POP_SSL_THREADS,
        RTS_IMAP_CONN,
        RTS_IMAP_THREADS,
        RTS_IMAP_SSL_CONN,
        RTS_IMAP_SSL_THREADS,
        RTS_HTTP_IDLE_THREADS,
        RTS_HTTP_THREADS,
        RTS_SOAP_SESSIONS,
        RTS_MBOX_CACHE_SIZE,
        RTS_MSG_CACHE_SIZE,
        RTS_FD_CACHE_SIZE,
        RTS_ACCOUNT_CACHE_SIZE,
        RTS_COS_CACHE_SIZE,
        RTS_DOMAIN_CACHE_SIZE,
        RTS_SERVER_CACHE_SIZE,
        RTS_UCSERVICE_CACHE_SIZE,
        RTS_ZIMLET_CACHE_SIZE,
        RTS_GROUP_CACHE_SIZE,
        RTS_XMPP_CACHE_SIZE,
      };

  private static final Lock LOCK = new ReentrantLock();

  private static final StatsScheduler statsScheduler = StatsScheduler.getDefault();
  private static final long DUMP_FREQUENCY = Constants.MILLIS_PER_MINUTE;
  /**
   * The number of statements that were prepared, as reported by {@link
   * DbPool.DbConnection#prepareStatement}.
   */
  private static final AtomicInteger sPrepareCount = new AtomicInteger(0);

  private static int mailboxCacheSize;
  private static long mailboxCacheSizeTimestamp = 0;
  private static Map<String, String> descriptions = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);
  private static final RealtimeStats realtimeStats = new RealtimeStats(mboxRealtimeStatsNames);
  private static boolean sIsInitialized = false;

  public static String getDescription(String statName) {
    return descriptions.get(statName);
  }

  private static void initDescriptions() {
    descriptions = Collections.synchronizedMap(descriptions);

    for (Field f : ZimbraPerf.class.getDeclaredFields()) {
      if (f.isAnnotationPresent(Description.class)) {
        try {
          Object o = f.get(null);
          if (o instanceof String) {
            String description = f.getAnnotation(Description.class).value();
            descriptions.put((String) o, description);
          }
        } catch (IllegalAccessException e) {
          ZimbraLog.perf.warn("Unexpected @Description annotation on field %s.", f.getName(), e);
        }
      }
    }
  }

  /** Returns all the latest stats as a key-value <tt>Map</tt>. */
  public static Map<String, Integer> getRealTimeStats() {
    return realtimeStats.getData();
  }

  /**
   * This may only be called BEFORE ZimbraPerf.initialize is called, otherwise the column names will
   * not be output correctly into the logs
   */
  public static void addRealtimeStatName(String name, String description) {
    if (sIsInitialized) {
      throw new IllegalStateException("Cannot add stat name after ZimbraPerf has been initialized");
    }
    ZimbraLog.perf.debug("Adding realtime stat '%s': %s", name, description);
    realtimeStats.addName(name);
    descriptions.put(name, description);
  }

  public static int getPrepareCount() {
    return sPrepareCount.get();
  }

  public static void incrementPrepareCount() {
    sPrepareCount.getAndIncrement();
  }

  /**
   * Adds the given callback to the list of callbacks that are called during realtime stats
   * collection.
   */
  public static void addStatsCallback(RealtimeStatsCallback callback) {
    realtimeStats.addCallback(callback);
  }

  public static void initialize(ServerID serverID) {
    LOCK.lock();
    try {
      if (sIsInitialized) {
        log.warn("Detected a second call to ZimbraPerf.initialize()", new Exception());
        return;
      }
      initDescriptions();
      switch (serverID) {
        case ZIMBRA:
          initializeForMainZimbraJVM();
          break;
        default:
      }
      sIsInitialized = true;
    } finally {
      LOCK.unlock();
    }
  }

  private static void initializeForMainZimbraJVM() {
    LOCK.lock();
    try {
      addStatsCallback(new ServerStatsCallback());
      addStatsCallback(new JettyStats());
      // Initialize JMX
      final ThreadStats threadsTracker = new ThreadStats("threads");

      // CSV
      statsScheduler.schedule(new CsvStatsDumper(SOAP_TRACKER), DUMP_FREQUENCY);
      statsScheduler.schedule(new CsvStatsDumper(IMAP_TRACKER), DUMP_FREQUENCY);
      statsScheduler.schedule(new CsvStatsDumper(POP_TRACKER), DUMP_FREQUENCY);
      statsScheduler.schedule(new CsvStatsDumper(LDAP_TRACKER), DUMP_FREQUENCY);
      statsScheduler.schedule(new CsvStatsDumper(SQL_TRACKER), DUMP_FREQUENCY);
      statsScheduler.schedule(new CsvStatsDumper(threadsTracker), DUMP_FREQUENCY);
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * Returns the mailbox cache size. The real value is reread once a minute so that cache
   * performance is not affected.
   */
  protected static int getMailboxCacheSize() {
    long now = System.currentTimeMillis();
    if (now - mailboxCacheSizeTimestamp > Constants.MILLIS_PER_MINUTE) {
      try {
        mailboxCacheSize = MailboxManager.getInstance().getCacheSize();
      } catch (ServiceException e) {
        ZimbraLog.perf.warn("Unable to determine mailbox cache size.", e);
      }
      mailboxCacheSizeTimestamp = now;
    }
    return mailboxCacheSize;
  }

  public static void main(String[] args) {
    initDescriptions();
    for (String field : descriptions.keySet()) {
      System.out.println(field + ": " + descriptions.get(field));
    }
  }
}
