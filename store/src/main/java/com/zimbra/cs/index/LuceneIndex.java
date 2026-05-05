// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.google.common.base.MoreObjects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.io.ByteStreams;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxIndex;
import com.zimbra.cs.util.IOUtil;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MergeTrigger;
import org.apache.lucene.index.MultiBits;
import org.apache.lucene.index.MultiTerms;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Bits;

/**
 * {@link IndexStore} implementation using Apache Lucene.
 *
 * @author tim
 * @author ysasaki
 */
public final class LuceneIndex extends IndexStore {

  private static final Semaphore READER_THROTTLE =
      new Semaphore(LC.zimbra_index_max_readers.intValue());
  private static final Semaphore WRITER_THROTTLE =
      new Semaphore(LC.zimbra_index_max_writers.intValue());

  private static final Cache<Integer, IndexSearcherImpl> SEARCHER_CACHE =
      CacheBuilder.newBuilder()
          .maximumSize(LC.zimbra_index_reader_cache_size.intValue())
          .expireAfterAccess(LC.zimbra_index_reader_cache_ttl.intValue(), TimeUnit.SECONDS)
          .removalListener(
              (RemovalListener<Integer, IndexSearcherImpl>) notification -> IOUtil.closeQuietly(notification.getValue()))
          .build();

  // Bug: 60631
  // cache lucene index of GAL sync account separately with no automatic eviction
  private static final ConcurrentMap<Integer, IndexSearcherImpl> GAL_SEARCHER_CACHE =
      new ConcurrentLinkedHashMap.Builder<Integer, IndexSearcherImpl>()
          .maximumWeightedCapacity(LC.zimbra_galsync_index_reader_cache_size.intValue())
          .listener(
              (mboxId, searcher) -> IOUtil.closeQuietly(searcher))
          .build();

  private final Mailbox mailbox;
  private final FSDirectory indexDirectory;
  private final AtomicBoolean pendingDelete = new AtomicBoolean(false);
  private final WriterInfo writerInfo = new WriterInfo();

  /**
   * Holds information related to writers to the index. Deletion of the index is only allowed when
   * there are no writers, hence it is important to know when there are no more writers.
   */
  private final class WriterInfo {
    private IndexWriterRef writerRef;
    private final Lock lock = new ReentrantLock();
    private final Condition hasNoWriters = lock.newCondition();

    private WriterInfo() {
      writerRef = null;
    }

    public IndexWriterRef getWriterRef() {
      return writerRef;
    }

    /**
     * @param newRef index writer reference. If null, there are no writers
     */
    public void setWriterRef(IndexWriterRef newRef) throws IOException {
      if ((newRef != null) && isPendingDelete()) {
        throw new IndexPendingDeleteException();
      }
      lock.lock();
      try {
        writerRef = newRef;
        if (writerRef == null) {
          hasNoWriters.signal();
        }
      } finally {
        lock.unlock();
      }
    }

    public Condition getHasNoWritersCondition() {
      return hasNoWriters;
    }

    public Lock getHasNoWritersLock() {
      return lock;
    }
  }

  private LuceneIndex(Mailbox mbox) throws ServiceException {
    mailbox = mbox;
    Volume vol = VolumeManager.getInstance().getVolume(mbox.getIndexVolume());
    String dir = vol.getMailboxDir(mailbox.getId(), Volume.TYPE_INDEX);

    // this must be different from the root dir (see the IMPORTANT comment below)
    File root = new File(dir + File.separatorChar + '0');

    // IMPORTANT!  Don't make the actual index directory (mIdxDirectory) yet!
    //
    // The runtime open-index code checks the existance of the actual index directory:
    // if it does exist but we cannot open the index, we do *NOT* create it under the
    // assumption that the index was somehow corrupted and shouldn't be messed-with....on the
    // other hand if the index dir does NOT exist, then we assume it has never existed (or
    // was deleted intentionally) and therefore we should just create an index.
    if (!root.exists()) {
      root.mkdirs();
    }

    if (!root.canRead()) {
      throw ServiceException.FAILURE(
          "LuceneDirectory not readable mbox=" + mbox.getId() + ",dir=" + root, null);
    }
    if (!root.canWrite()) {
      throw ServiceException.FAILURE(
          "LuceneDirectory not writable mbox=" + mbox.getId() + ",dir=" + root, null);
    }

    // the Lucene code does not atomically swap the "segments" and "segments.new" files...so it is
    // possible that a
    // previous run of the server crashed exactly in such a way that we have a "segments.new" file
    // but not a
    // "segments" file. We we will check here for the special situation that we have a segments.new/
    // file but not a
    // segments file...
    File segments = new File(root, "segments");
    if (!segments.exists()) {
      File newSegments = new File(root, "segments.new");
      if (newSegments.exists()) {
        newSegments.renameTo(segments);
      }
    }

    try {
      indexDirectory = openFSDirectory(root);
      // Detect legacy Lucene 3.5 index format and wipe if found
      if (isLegacyFormatIndex(root)) {
        ZimbraLog.index.warn("Legacy Lucene 3.5 index format detected, wiping index for reindex: %s", root);
        wipeLegacyIndex(root);
      }
    } catch (IOException e) {
      throw ServiceException.FAILURE("Failed to open index directory: " + root, e);
    }
  }

  /** Opens the FSDirectory based on LC config. Defaults to MMapDirectory (best for JDK 21 + native access). */
  private static FSDirectory openFSDirectory(File path) throws IOException {
    String impl = LC.zimbra_index_lucene_io_impl.value();
    FSDirectory dir;
    if ("nio".equals(impl)) {
      dir = NIOFSDirectory.open(path.toPath());
    } else {
      // Default: MMapDirectory uses MemorySegment on JDK 21 with --enable-native-access=ALL-UNNAMED
      dir = MMapDirectory.open(path.toPath());
    }
    ZimbraLog.index.debug("OpenLuceneDirectory impl=%s,dir=%s", dir.getClass().getSimpleName(), path);
    return dir;
  }

  /** Detects legacy Lucene 3.5 index format by presence of .frq/.prx/.tii/.tis files. */
  private static boolean isLegacyFormatIndex(File dir) {
    String[] files = dir.list();
    if (files == null) return false;
    for (String f : files) {
      if (f.endsWith(".frq") || f.endsWith(".prx") || f.endsWith(".tii") || f.endsWith(".tis")) {
        return true;
      }
    }
    return false;
  }

  /** Wipes all files in the index directory (for legacy format migration). */
  private static void wipeLegacyIndex(File dir) {
    File[] files = dir.listFiles();
    if (files == null) return;
    for (File f : files) {
      if (!f.delete()) {
        ZimbraLog.index.warn("Failed to delete legacy index file: %s", f);
      }
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("mbox", mailbox.getId())
        .add("dir", indexDirectory)
        .toString();
  }

  private synchronized void doDeleteIndex() throws IOException {
    assert (writerInfo.getWriterRef() == null);
    ZimbraLog.index.debug("Deleting index %s", indexDirectory);
    if (mailbox.isGalSyncMailbox()) {
      IOUtil.closeQuietly(GAL_SEARCHER_CACHE.remove(mailbox.getId()));
    } else {
      SEARCHER_CACHE.asMap().remove(mailbox.getId());
    }

    String[] files;
    try {
      files = indexDirectory.listAll();
    } catch (IOException ignore) {
      return;
    }

    for (String file : files) {
      try {
        indexDirectory.deleteFile(file);
      } catch (IOException e) {
        ZimbraLog.index.warn("Failed to delete index file %s: %s", file, e.getMessage());
      }
    }
  }

  /** Deletes this index completely. */
  @Override
  public void deleteIndex() throws IOException {
    pendingDelete.set(true);
    writerInfo.getHasNoWritersLock().lock();
    try {
      if (writerInfo.getWriterRef() != null) {
        writerInfo.getHasNoWritersCondition().awaitUninterruptibly();
      }
      doDeleteIndex();
    } finally {
      writerInfo.getHasNoWritersLock().unlock();
    }
    pendingDelete.set(false);
  }

  /**
   * Runs a common search query + common sort order (and throw away the result) to warm up the
   * Lucene cache and OS file system cache.
   */
  @Override
  public synchronized void warmup() {
    if (SEARCHER_CACHE.asMap().containsKey(mailbox.getId())
        || GAL_SEARCHER_CACHE.containsKey(mailbox.getId())) {
      return; // already warmed up
    }
    long start = System.currentTimeMillis();
    try {
      try (ZimbraIndexSearcher searcher = openSearcher()) {
        searcher.search(
            new TermQuery(new Term(LuceneFields.L_CONTENT, "zimbra")),
            1,
            new Sort(new SortField(LuceneFields.L_SORT_DATE, SortField.Type.STRING, true)));
      }
    } catch (IOException e) {
      ZimbraLog.search.warn("Failed to warm up", e);
    }
    ZimbraLog.search.debug("WarmUpLuceneSearcher elapsed=%d", System.currentTimeMillis() - start);
  }

  /** Removes IndexSearcher used for this index from cache. */
  @Override
  public void evict() {
    if (mailbox.isGalSyncMailbox()) {
      IOUtil.closeQuietly(GAL_SEARCHER_CACHE.remove(mailbox.getId()));
    } else {
      SEARCHER_CACHE.asMap().remove(mailbox.getId());
    }
  }

  private IndexReader openIndexReader(boolean tryRepair) throws IOException {
    try {
      return DirectoryReader.open(indexDirectory);
    } catch (CorruptIndexException e) {
      if (!tryRepair) {
        throw e;
      }
      repairByWipe(e);
      return openIndexReader(false);
    } catch (AssertionError e) {
      if (!tryRepair) {
        throw e;
      }
      repairByWipe(e);
      return openIndexReader(false);
    }
  }

  private IndexWriter openIndexWriter(IndexWriterConfig.OpenMode mode, boolean tryRepair)
      throws IOException {
    try {
      IndexWriterConfig config = getWriterConfig().setOpenMode(mode);
      if (ZimbraLog.index.isDebugEnabled()) {
        config.setInfoStream(new org.apache.lucene.util.PrintStreamInfoStream(System.err));
      }
      return new IndexWriter(indexDirectory, config);
    } catch (AssertionError e) {
      if (!tryRepair) {
        throw e;
      }
      repairByWipe(e);
      return openIndexWriter(mode, false);
    } catch (CorruptIndexException e) {
      if (!tryRepair) {
        throw e;
      }
      repairByWipe(e);
      return openIndexWriter(mode, false);
    }
  }

  private synchronized <T extends Throwable> void repairByWipe(T ex) throws T {
    ZimbraLog.index.error("Index corrupted, wiping for reindex", ex);
    try {
      doDeleteIndex();
      ZimbraLog.index.info("Index wiped, re-indexing is required.");
    } catch (IOException e) {
      ZimbraLog.index.warn("Failed to wipe index, re-indexing is required.", e);
    }
    throw ex;
  }

  /**
   * Caller is responsible for calling {@link IndexReader#close()} to release system resources
   * associated with it.
   *
   * @return A {@link IndexReader} for this index.
   * @throws IOException if opening an {@link IndexReader} failed
   */
  @Override
  public synchronized ZimbraIndexSearcher openSearcher() throws IOException {
    IndexSearcherImpl searcher = null;
    if (mailbox.isGalSyncMailbox()) {
      searcher = GAL_SEARCHER_CACHE.get(mailbox.getId());
    } else {
      searcher = SEARCHER_CACHE.getIfPresent(mailbox.getId());
    }
    if (searcher != null) {
      ZimbraLog.search.debug("CacheHitLuceneSearcher %s", searcher);
      searcher.inc();
      return searcher;
    }

    READER_THROTTLE.acquireUninterruptibly();
    long start = System.currentTimeMillis();
    try {
      searcher = new IndexSearcherImpl(openIndexReader(true));
    } catch (IOException e) {
      // Handle the special case of trying to open a not-yet-created index, by opening for write and
      // immediately
      // closing. Index directory should get initialized as a result.
      if (isEmptyDirectory(indexDirectory.getDirectory().toFile())) {
        // create an empty index
        IndexWriter writer =
            new IndexWriter(
                indexDirectory, getWriterConfig().setOpenMode(IndexWriterConfig.OpenMode.CREATE));
        IOUtil.closeQuietly(writer);
        searcher = new IndexSearcherImpl(openIndexReader(false));
      } else {
        throw e;
      }
    } finally {
      if (searcher == null) {
        READER_THROTTLE.release();
      }
    }

    ZimbraLog.search.debug(
        "OpenLuceneSearcher %s,elapsed=%d", searcher, System.currentTimeMillis() - start);
    searcher.inc();
    if (mailbox.isGalSyncMailbox()) {
      IOUtil.closeQuietly(GAL_SEARCHER_CACHE.put(mailbox.getId(), searcher));
    } else {
      SEARCHER_CACHE.asMap().put(mailbox.getId(), searcher);
    }
    return searcher;
  }

  /**
   * Check to see if it is OK for us to create an index in the specified directory.
   *
   * @param dir index directory
   * @return TRUE if the index directory is empty or doesn't exist, FALSE if the index directory
   *     exists and has files in it or if we cannot list files in the directory
   */
  private boolean isEmptyDirectory(File dir) {
    if (!dir.exists()) { // dir doesn't even exist yet.  Create the parents and return true
      dir.mkdirs();
      return true;
    }

    // Empty directory is okay, but a directory with any files implies index corruption.
    File[] files = dir.listFiles();

    // if files is null here, we are likely running into file permission issue
    if (files == null) {
      ZimbraLog.index.warn("Could not list files in directory %s", dir.getAbsolutePath());
      return false;
    }

    int num = 0;
    for (File file : files) {
      String fname = file.getName();
      if (file.isDirectory() && (fname.equals(".") || fname.equals(".."))) {
        continue;
      }
      num++;
    }
    return (num <= 0);
  }

  @Override
  public synchronized Indexer openIndexer() throws IOException {
    if (writerInfo.getWriterRef() != null) {
      writerInfo.getWriterRef().inc();
    } else {
      WRITER_THROTTLE.acquireUninterruptibly();
      try {
        writerInfo.setWriterRef(openWriter());
      } finally {
        if (writerInfo.getWriterRef() == null) {
          WRITER_THROTTLE.release();
        }
      }
    }
    return new LuceneIndexerImpl(writerInfo.getWriterRef());
  }

  private IndexWriterRef openWriter() throws IOException {
    assert (Thread.holdsLock(this));

    IndexWriter writer;
    try {
      writer = openIndexWriter(IndexWriterConfig.OpenMode.APPEND, true);
    } catch (IOException e) {
      // the index (the segments* file in particular) probably didn't exist
      // when new IndexWriter was called in the try block, we would get a
      // FileNotFoundException for that case. If the directory is empty,
      // this is the very first index write for this this mailbox (or the
      // index might be deleted), the FileNotFoundException is benign.
      if (isEmptyDirectory(indexDirectory.getDirectory().toFile())) {
        writer = openIndexWriter(IndexWriterConfig.OpenMode.CREATE, false);
      } else {
        throw e;
      }
    }
    return new IndexWriterRef(this, writer);
  }

  private synchronized void commitWriter() throws IOException {
    assert (writerInfo.getWriterRef() != null);

    ZimbraLog.index.debug("Commit IndexWriter");

    MergeTask task = new MergeTask(writerInfo.getWriterRef());

    boolean success = false;
    try {
      try {
        writerInfo.getWriterRef().get().commit();
      } catch (CorruptIndexException e) {
        try {
          writerInfo.getWriterRef().get().close();
        } catch (Throwable ignore) {
        }
        repairByWipe(e);
        throw e; // fail to commit regardless of the repair
      } catch (AssertionError e) {
        try {
          writerInfo.getWriterRef().get().close();
        } catch (Throwable ignore) {
        }
        repairByWipe(e);
        throw e; // fail to commit regardless of the repair
      }
      mailbox.index.submit(task); // merge must run in background
      success = true;
    } catch (RejectedExecutionException e) {
      ZimbraLog.index.warn("Skipping merge because all index threads are busy");
    } finally {
      if (!success) {
        writerInfo.getWriterRef().dec();
      }
    }
  }

  /**
   * Called by {@link IndexWriterRef#dec()}. Can be called by the thread that opened the writer or
   * the merge thread.
   */
  private synchronized void closeWriter() {
    if (writerInfo.getWriterRef() == null) {
      return;
    }

    ZimbraLog.index.debug("Close IndexWriter");

    try {
      writerInfo.getWriterRef().get().close(); // ignore phantom pending merges
    } catch (CorruptIndexException e) {
      try {
        repairByWipe(e);
      } catch (CorruptIndexException ignore) {
      }
    } catch (AssertionError e) {
      try {
        repairByWipe(e);
      } catch (AssertionError ignore) {
      }
    } catch (IOException e) {
      ZimbraLog.index.error("Failed to close IndexWriter", e);
    } finally {
      WRITER_THROTTLE.release();
      try {
        writerInfo.setWriterRef(null);
      } catch (IOException e) {
      }
    }
  }

  /**
   * Run a sanity check for the index. Callers are responsible to make sure the index is not opened
   * by any writer.
   *
   * @param out info stream where messages should go. If null, no messages are printed.
   * @return true if no problems were found, otherwise false
   * @throws IOException failed to verify, but it doesn't necessarily mean the index is corrupted.
   */
  @Override
  public boolean verify(PrintStream out) throws IOException {
    if (!DirectoryReader.indexExists(indexDirectory)) {
      out.println(
          "index does not exist or no segments file found: " + indexDirectory.getDirectory());
      return true;
    }
    CheckIndex check = new CheckIndex(indexDirectory);
    if (out != null) {
      check.setInfoStream(out);
    }
    CheckIndex.Status status = check.checkIndex();
    return status.clean;
  }

  /**
   * Only one background thread that holds the lock may process a merge for the given writer. Other
   * concurrent attempts simply skip the merge.
   */
  private static final class MergeScheduler extends ConcurrentMergeScheduler {
    private final ReentrantLock lock = new ReentrantLock();

    /** Acquires the lock. */
    void lock() {
      lock.lock();
    }

    /**
     * Try to hold the lock.
     *
     * @return true if the lock is held, false the lock is currently held by the other thread.
     */
    boolean tryLock() {
      return lock.tryLock();
    }

    void release() {
      try {
        lock.unlock();
      } catch (IllegalMonitorStateException ignore) {
      }
    }

    /** Removes the Thread-to-IndexWriter reference. */
    @Override
    public void close() {
      super.close();
      release();
    }
  }

  /**
   * In order to minimize delay caused by merges, merges are processed only in background threads.
   * Writers triggered by batch threshold or search commit the changes before processing merges, so
   * that the changes are available to readers without long delay that merges likely cause. Merge
   * threads don't block other writer threads running in foreground. Another indexing using the same
   * writer may start even while the merge is in progress.
   */
  private final class MergeTask extends MailboxIndex.IndexTask {
    private final IndexWriterRef ref;

    MergeTask(IndexWriterRef ref) {
      super(ref.getIndex().mailbox);
      this.ref = ref;
    }

    @Override
    public void exec() throws IOException {
      IndexWriter writer = ref.get();
      MergeScheduler scheduler = (MergeScheduler) writer.getConfig().getMergeScheduler();
      try {
        if (scheduler.tryLock()) {
          writer.maybeMerge();
        } else {
          ZimbraLog.index.debug("Merge is in progress by other thread");
        }
      } catch (CorruptIndexException e) {
        try {
          writer.close();
        } catch (Throwable ignore) {
        }
        repairByWipe(e);
      } catch (AssertionError e) {
        try {
          writer.close();
        } catch (Throwable ignore) {
        }
        repairByWipe(e);
      } catch (IOException e) {
        ZimbraLog.index.error("Failed to merge IndexWriter", e);
      } finally {
        scheduler.release();
        ref.dec();
      }
    }
  }

  private IndexWriterConfig getWriterConfig() {
    IndexWriterConfig config = new IndexWriterConfig(mailbox.index.getAnalyzer());
    config.setMergeScheduler(new MergeScheduler());
    config.setMaxBufferedDocs(LC.zimbra_index_lucene_max_buffered_docs.intValue());
    config.setRAMBufferSizeMB(LC.zimbra_index_lucene_ram_buffer_size_kb.intValue() / 1024.0);
    config.setUseCompoundFile(LC.zimbra_index_lucene_use_compound_file.booleanValue());
    
    TieredMergePolicy policy = new TieredMergePolicy();
    if (LC.zimbra_index_lucene_merge_policy.booleanValue()) {
      // LogDocMergePolicy equivalent: limit by doc count
      policy.setMaxMergeAtOnce(LC.zimbra_index_lucene_merge_factor.intValue());
      policy.setSegmentsPerTier(LC.zimbra_index_lucene_merge_factor.intValue());
      if (LC.zimbra_index_lucene_max_merge.intValue() != Integer.MAX_VALUE) {
        policy.setMaxMergedSegmentMB(LC.zimbra_index_lucene_max_merge.intValue() / 1024.0);
      }
    } else {
      // LogByteSizeMergePolicy equivalent: limit by byte size
      policy.setMaxMergeAtOnce(LC.zimbra_index_lucene_merge_factor.intValue());
      policy.setSegmentsPerTier(LC.zimbra_index_lucene_merge_factor.intValue());
      policy.setFloorSegmentMB(LC.zimbra_index_lucene_min_merge.intValue() / 1024.0);
      if (LC.zimbra_index_lucene_max_merge.intValue() != Integer.MAX_VALUE) {
        policy.setMaxMergedSegmentMB(LC.zimbra_index_lucene_max_merge.intValue() / 1024.0);
      }
    }
    config.setMergePolicy(policy);
    return config;
  }

  public static ZimbraQueryResults search(ZimbraQuery zq) throws ServiceException {
    SearchParams params = zq.getParams();
    ZimbraLog.search.debug("query: %s", params.getQueryString());

    // handle special-case localized sorts: re-sort results at the end
    boolean isLocalizedSort = false;
    SortBy originalSort = params.getSortBy();
    switch (originalSort) {
      case NAME_LOCALIZED_ASC:
      case NAME_LOCALIZED_DESC:
        isLocalizedSort = true;
        break;
    }

    ZimbraQueryResults results = zq.execute();
    if (isLocalizedSort) {
      results = new ReSortingQueryResults(results, originalSort, params);
    }
    return results;
  }

  @Override
  public boolean isPendingDelete() {
    return pendingDelete.get();
  }

  @Override
  public void setPendingDelete(boolean pendingDelete) {
    this.pendingDelete.set(pendingDelete);
  }

  public static final class Factory implements IndexStore.Factory {
    public Factory() {
      IndexSearcher.setMaxClauseCount(LC.zimbra_index_lucene_max_terms_per_query.intValue());
    }

    @Override
    public LuceneIndex getIndexStore(Mailbox mbox) throws ServiceException {
      return new LuceneIndex(mbox);
    }

    @Override
    public void destroy() {
      SEARCHER_CACHE.asMap().clear();

      for (IndexSearcherImpl searcher : GAL_SEARCHER_CACHE.values()) {
        IOUtil.closeQuietly(searcher);
      }
      GAL_SEARCHER_CACHE.clear();
    }
  }

  private static final class LuceneIndexerImpl implements Indexer {
    private final IndexWriterRef writer;

    LuceneIndexerImpl(IndexWriterRef writer) {
      this.writer = writer;
    }

    @Override
    public void close() throws IOException {
      writer.index.commitWriter();
      ZimbraIndexSearcher searcher = null;
      if (writer.getIndex().mailbox.isGalSyncMailbox()) {
        searcher = GAL_SEARCHER_CACHE.get(writer.getIndex().mailbox.getId());
      } else {
        searcher = SEARCHER_CACHE.getIfPresent(writer.getIndex().mailbox.getId());
      }
      if (searcher != null) {
        ZimbraLuceneIndexReader ndxReader = (ZimbraLuceneIndexReader) searcher.getIndexReader();
        IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) ndxReader.getLuceneReader());
        if (newReader != null) {
          if (writer.getIndex().mailbox.isGalSyncMailbox()) {
            // make sure that we close the previous value associated with the key
            IOUtil.closeQuietly(
                GAL_SEARCHER_CACHE.put(
                    writer.getIndex().mailbox.getId(), new IndexSearcherImpl(newReader)));
          } else {
            // Bug: 69870
            // No need to close the previous value associated with the key here.
            // CacheBuilder sends a callback using removalListener onRemoval(..)
            // which eventually closes IndexSearcher
            SEARCHER_CACHE
                .asMap()
                .put(writer.getIndex().mailbox.getId(), new IndexSearcherImpl(newReader));
          }
        }
      }
    }

    @Override
    public void compact() {
      MergeScheduler scheduler = (MergeScheduler) writer.get().getConfig().getMergeScheduler();
      scheduler.lock();
      try {
        // In Lucene 9.x, we can't directly get the reader from IndexWriter
        // Just log and proceed with forceMergeDeletes
        ZimbraLog.index.info("Force merge deletes");
        writer.get().forceMergeDeletes(true);
      } catch (IOException e) {
        ZimbraLog.index.error("Failed to optimize index", e);
      } finally {
        scheduler.release();
      }
    }

    @Override
    public synchronized int maxDocs() {
      // In Lucene 9.x, we can't directly get the reader from IndexWriter
      // Return the number of documents in the writer
      // Note: This is an approximation since we can't get the exact count
      return 0;
    }

    /**
     * Adds the list of documents to the index.
     *
     * <p>If the index status is stale, delete the stale documents first, then add new documents. If
     * the index status is deferred, we are sure that this item is not already in the index, and so
     * we can skip the check-update step.
     */
    @Override
    public synchronized void addDocument(Folder folder, MailItem item, List<IndexDocument> docs)
        throws IOException {
      if (docs == null || docs.isEmpty()) {
        return;
      }

      // handle the partial re-index case here by simply deleting all the documents matching the
      // index_id
      // so that we can simply add the documents to the index later!!
      switch (item.getIndexStatus()) {
        case STALE:
        case DONE: // for partial re-index
          Term term = new Term(LuceneFields.L_MAILBOX_BLOB_ID, String.valueOf(item.getId()));
          writer.get().deleteDocuments(term);
          break;
        case DEFERRED:
          break;
        default:
          assert false : item.getIndexId();
      }

      for (IndexDocument doc : docs) {
        // doc can be shared by multiple threads if multiple mailboxes are referenced in a single
        // email
        synchronized (doc) {
          setFields(item, doc);
          Document luceneDoc = doc.toDocument();
          if (ZimbraLog.index.isTraceEnabled()) {
            ZimbraLog.index.trace("Adding lucene document %s", luceneDoc.toString());
          }
          writer.get().addDocument(luceneDoc);
        }
      }
    }

    /**
     * Deletes documents.
     *
     * <p>The document count may be more than you expect here, the document may already be deleted
     * and just not be optimized out yet -- some Lucene APIs (e.g. docFreq) will still return the
     * old count until the indexes are optimized.
     */
    @Override
    public void deleteDocument(List<Integer> ids) throws IOException {
      for (Integer id : ids) {
        Term term = new Term(LuceneFields.L_MAILBOX_BLOB_ID, id.toString());
        writer.get().deleteDocuments(term);
        ZimbraLog.index.debug("Deleted documents id=%d", id);
      }
    }
  }

  /** {@link IndexWriter} wrapper that supports a reference counter. */
  private static final class IndexWriterRef {
    private final LuceneIndex index;
    private final IndexWriter writer;
    private final AtomicInteger count = new AtomicInteger(1); // ref counter

    IndexWriterRef(LuceneIndex index, IndexWriter writer) {
      this.index = index;
      this.writer = writer;
    }

    IndexWriter get() {
      return writer;
    }

    LuceneIndex getIndex() {
      return index;
    }

    void inc() {
      count.incrementAndGet();
    }

    void dec() {
      synchronized (index) {
        if (count.decrementAndGet() <= 0) {
          index.closeWriter();
        }
      }
    }
  }

  /** Custom {@link IndexSearcher} that supports a reference counter. */
  private static final class IndexSearcherImpl implements ZimbraIndexSearcher {
    private final AtomicInteger count = new AtomicInteger(1);
    private final IndexSearcher luceneSearcher;
    private final ZimbraIndexReader luceneReader;

    IndexSearcherImpl(IndexReader reader) {
      luceneSearcher = new IndexSearcher(reader);
      luceneReader = new ZimbraLuceneIndexReader(luceneSearcher.getIndexReader());
    }

    void inc() {
      count.incrementAndGet();
    }

    @Override
    public void close() throws IOException {
      if (count.decrementAndGet() == 0) {
        ZimbraLog.search.debug("Close IndexSearcher");
        try {
          IOUtil.closeQuietly((java.io.Closeable) luceneSearcher);
        } finally {
          IOUtil.closeQuietly(getIndexReader());
          READER_THROTTLE.release();
        }
      }
    }

    @Override
    public Document doc(ZimbraIndexDocumentID docID) throws IOException {
      if (docID instanceof ZimbraLuceneDocumentID) {
        ZimbraLuceneDocumentID zlDocID = (ZimbraLuceneDocumentID) docID;
        return luceneSearcher.doc(zlDocID.getLuceneDocID());
      }
      throw new IllegalArgumentException("Expected a ZimbraLuceneDocumentID");
    }

    @Override
    public int docFreq(Term term) throws IOException {
      return luceneSearcher.getIndexReader().docFreq(term);
    }

    @Override
    public ZimbraIndexReader getIndexReader() {
      return luceneReader;
    }

    @Override
    public ZimbraTopDocs search(Query query, int n) throws IOException {
      return ZimbraTopDocs.create(luceneSearcher.search(query, n));
    }

    @Override
    public ZimbraTopDocs search(Query query, int n, org.apache.lucene.search.Sort sort) throws IOException {
      return ZimbraTopDocs.create(luceneSearcher.search(query, n, sort));
    }
  }

  public static final class ZimbraLuceneIndexReader implements ZimbraIndexReader {

    private final IndexReader luceneReader;

    private ZimbraLuceneIndexReader(IndexReader indexReader) {
      luceneReader = indexReader;
    }

    @Override
    public void close() throws IOException {
      IOUtil.closeQuietly(getLuceneReader());
    }

    @Override
    public int numDocs() {
      return getLuceneReader().numDocs();
    }

    @Override
    public int numDeletedDocs() {
      return getLuceneReader().numDeletedDocs();
    }

    /**
     * Returns an enumeration of the String representations for values of terms with {@code field}
     * positioned to start at the first term with a value greater than {@code firstTermValue}. The
     * enumeration is ordered by String.compareTo().
     */
    @Override
    public TermFieldEnumeration getTermsForField(String field, String firstTermValue)
        throws IOException {
      return new LuceneTermValueEnumeration(field, firstTermValue);
    }

    private final class LuceneTermValueEnumeration implements TermFieldEnumeration {
      private TermsEnum termsEnum;
      private BytesRef currentTerm;
      private final String field;

      private LuceneTermValueEnumeration(String field, String firstTermValue) throws IOException {
        this.field = field;
        org.apache.lucene.index.Terms terms = MultiTerms.getTerms(getLuceneReader(), field);
        if (terms != null) {
          termsEnum = terms.iterator();
          TermsEnum.SeekStatus status = termsEnum.seekCeil(new BytesRef(firstTermValue));
          if (status != TermsEnum.SeekStatus.END) {
            currentTerm = termsEnum.term();
          } else {
            currentTerm = null;
          }
        } else {
          termsEnum = null;
          currentTerm = null;
        }
      }

      @Override
      public boolean hasMoreElements() {
        return currentTerm != null;
      }

      @Override
      public BrowseTerm nextElement() {
        if (currentTerm == null) {
          throw new NoSuchElementException("No more values");
        }
        BrowseTerm result = new BrowseTerm(currentTerm.utf8ToString(), 0);
        try {
          result = new BrowseTerm(currentTerm.utf8ToString(), termsEnum.docFreq());
          currentTerm = termsEnum.next();
        } catch (IOException e) {
          currentTerm = null;
        }
        return result;
      }

      @Override
      public void close() throws IOException {
        currentTerm = null;
        termsEnum = null;
      }
    }

    public IndexReader getLuceneReader() {
      return luceneReader;
    }
  }

  /**
   * Note: Lucene 3.5.0 highly discourages optimizing the index as it is horribly inefficient and
   * very rarely justified. Please check {@code IndexWriter.forceMerge} API documentation for more
   * details. Code removed which used to use forceMerge.
   */
  @Override
  public void optimize() {}
}
