// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.index.ZimbraIndexReader.TermFieldEnumeration;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.util.IOUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/** {@link QueryOperation} which queries Lucene. */
public final class LuceneQueryOperation extends QueryOperation {
  private static final float DB_FIRST_TERM_FREQ_PERC;

  static {
    float f = 0.8f;
    try {
      f = Float.parseFloat(LC.search_dbfirst_term_percentage_cutoff.value());
    } catch (Exception e) {
    }
    if (f < 0.0 || f > 1.0) {
      f = 0.8f;
    }
    DB_FIRST_TERM_FREQ_PERC = f;
  }

  private int curHitNo = 0; // our offset into the hits
  private boolean haveRunSearch = false;
  private String queryString = "";
  private Query luceneQuery;

  /**
   * Used for doing DB-joins: the list of terms for the filter one of the terms in the list MUST
   * occur in the document for it to match.
   */
  private List<Term> filterTerms;

  /**
   * Because we don't store the real mail-item-id of documents, we ALWAYS need a DBOp in order to
   * properly get our results.
   */
  private DBQueryOperation dbOp;

  private final List<QueryInfo> queryInfo = Lists.newArrayList();
  private boolean hasSpamTrashSetting = false;

  private ZimbraTopDocs hits;
  private int topDocsLen = 0; // number of hits fetched
  private int topDocsChunkSize = 2000; // how many hits to fetch per step in Lucene
  private ZimbraIndexSearcher searcher;
  private Sort sort;

  /**
   * Adds the specified text clause at the top level.
   *
   * <p>e.g. going in "a b c" if we addClause("d") we get "a b c d".
   *
   * @param queryStr Appended to the end of the text-representation of this query
   * @param query Lucene query
   * @param bool allows for negated query terms
   */
  public void addClause(String queryStr, Query query, boolean bool) {
    assert (!haveRunSearch);

    // ignore empty BooleanQuery
    if (query instanceof BooleanQuery && ((BooleanQuery) query).clauses().isEmpty()) {
      return;
    }

    if (queryString.isEmpty()) {
      queryString = (bool ? "" : "-") + queryStr;
    } else {
      queryString = queryString + " " + (bool ? "" : "-") + queryStr;
    }

    if (bool) {
      if (luceneQuery == null) {
        luceneQuery = query;
      } else if (luceneQuery instanceof BooleanQuery) {
        BooleanQuery bq = (BooleanQuery) luceneQuery;
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (BooleanClause clause : bq.clauses()) {
          builder.add(clause);
        }
        builder.add(query, BooleanClause.Occur.MUST);
        luceneQuery = builder.build();
      } else if (query instanceof BooleanQuery) {
        BooleanQuery bq = (BooleanQuery) query;
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(luceneQuery, BooleanClause.Occur.MUST);
        for (BooleanClause clause : bq.clauses()) {
          builder.add(clause);
        }
        luceneQuery = builder.build();
      } else {
        BooleanQuery combined = new BooleanQuery.Builder()
            .add(luceneQuery, BooleanClause.Occur.MUST)
            .add(query, BooleanClause.Occur.MUST)
            .build();
        luceneQuery = combined;
      }
    } else {
      if (luceneQuery == null) {
        BooleanQuery negate = new BooleanQuery.Builder()
            .add(query, BooleanClause.Occur.MUST_NOT)
            .build();
        luceneQuery = negate;
      } else if (luceneQuery instanceof BooleanQuery) {
        BooleanQuery bq = (BooleanQuery) luceneQuery;
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (BooleanClause clause : bq.clauses()) {
          builder.add(clause);
        }
        builder.add(query, BooleanClause.Occur.MUST_NOT);
        luceneQuery = builder.build();
      } else {
        BooleanQuery combined = new BooleanQuery.Builder()
            .add(luceneQuery, BooleanClause.Occur.MUST)
            .add(query, BooleanClause.Occur.MUST_NOT)
            .build();
        luceneQuery = combined;
      }
    }
  }

  /**
   * Adds the specified text clause ANDED with the existing query.
   *
   * <p>e.g. going in w/ "a b c" if we addAndedClause("d") we get "(a b c) AND d".
   *
   * <p>This API may only be called AFTER query optimizing and AFTER remote queries have been split.
   *
   * <p>Note that this API does *not* update the text-representation of this query.
   */
  void addAndedClause(Query query, boolean bool) {
    assert (luceneQuery != null);
    haveRunSearch = false; // will need to re-run the search with this new clause
    curHitNo = 0;

    if (luceneQuery instanceof BooleanQuery) {
      BooleanQuery bquery = ((BooleanQuery) luceneQuery);
      boolean orOnly = true;
      for (BooleanClause clause : bquery.clauses()) {
        if (clause.getOccur() != BooleanClause.Occur.SHOULD) {
          orOnly = false;
          break;
        }
      }
      if (!orOnly) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (BooleanClause clause : bquery.clauses()) {
          builder.add(clause);
        }
        builder.add(
            new BooleanClause(
                query, bool ? BooleanClause.Occur.MUST : BooleanClause.Occur.MUST_NOT));
        luceneQuery = builder.build();
        return;
      }
    }

    BooleanQuery bquery = new BooleanQuery.Builder()
        .add(new BooleanClause(luceneQuery, BooleanClause.Occur.MUST))
        .add(
            new BooleanClause(query, bool ? BooleanClause.Occur.MUST : BooleanClause.Occur.MUST_NOT))
        .build();
    luceneQuery = bquery;
  }

  /**
   * Adds the specified text clause as a filter over the existing query.
   *
   * <p>e.g. going in w/ "a b c" if we addAndedClause("d") we get "(a b c) AND d".
   *
   * <p>This API is used by the query executor so that it can temporarily add a bunch of indexIds to
   * the existing query -- this is necessary when we are doing a DB-first query plan execution.
   *
   * <p>Note that this API does *not* update the text-representation of this query.
   */
  void addFilterClause(Term t) {
    haveRunSearch = false; // will need to re-run the search with this new clause
    curHitNo = 0;

    if (filterTerms == null) {
      filterTerms = new ArrayList<>();
    }
    filterTerms.add(t);
  }

  /** Clears the filter clause */
  void clearFilterClause() {
    filterTerms = null;
  }

  /**
   * Sets the text query *representation* manually -- the thing that is output if we have to proxy
   * this search somewhere else -- used when dealing with wildcard searches.
   */
  public void setQueryString(String value) {
    assert (queryString.isEmpty());
    queryString = value;
  }

  @Override
  public String toQueryString() {
    return '(' + queryString + ')';
  }

  /** Returns {@code true} if we think this query is best evaluated DB-FIRST. */
  boolean shouldExecuteDbFirst() {
    if (searcher == null || luceneQuery == null) {
      return true;
    }

    if (luceneQuery instanceof TermQuery) {
      TermQuery query = (TermQuery) luceneQuery;
      Term term = query.getTerm();
      long start = System.currentTimeMillis();
      try {
        int freq = searcher.docFreq(term);
        int docsCutoff = (int) (searcher.getIndexReader().numDocs() * DB_FIRST_TERM_FREQ_PERC);
        ZimbraLog.search.debug(
            "LuceneDocFreq freq=%d,cutoff=%d(%d%%),elapsed=%d",
            freq,
            docsCutoff,
            (int) (100 * DB_FIRST_TERM_FREQ_PERC),
            System.currentTimeMillis() - start);
        if (freq > docsCutoff) {
          return true;
        }
      } catch (IOException e) {
        return false;
      }
    }

    try {
      // TODO count results using TotalHitCountCollector
      fetchFirstResults(1000); // some arbitrarily large initial size to fetch
      if (getTotalHitCount()
          > 1000) { // also arbitrary, just to make very small searches run w/o extra DB check
        // Bug: 68630
        // Let's try to avoid the additional D/B lookup;
        // We can calculate the number of items contained by the folders to search by getting the
        // total
        // item counts from the cache. If total items < total lucene hits - its cheaper to do the
        // D/B query first.
        Set<Folder> targetFolders = dbOp.getTargetFolders();
        if (targetFolders != null && targetFolders.size() > 0) {
          long itemCount = getTotalItemCount(targetFolders);
          ZimbraLog.search.debug(
              "lucene hits=%d, folders item count=%d", getTotalHitCount(), itemCount);
          if (itemCount < getTotalHitCount()) return true; // run DB-FIRST
        }

        int dbHitCount = dbOp.getDbHitCount();
        ZimbraLog.search.debug("EstimatedHits lucene=%d,db=%d", getTotalHitCount(), dbHitCount);
        if (dbHitCount < getTotalHitCount()) {
          return true; // run DB-FIRST
        }
      }
      return false;
    } catch (ServiceException e) {
      return false;
    }
  }

  private long getTotalItemCount(Set<Folder> folders) {
    long total = 0;
    for (Folder f : folders) total += f.getItemCount();

    return total;
  }

  @Override
  public void close() {
    IOUtil.closeQuietly(searcher);
    searcher = null;
  }

  private void fetchFirstResults(int initialChunkSize) {
    if (!haveRunSearch) {
      assert (curHitNo == 0);
      topDocsLen = 3 * initialChunkSize;
      runSearch();
    }
  }

  /**
   * Fetch the next chunk of results.
   *
   * <p>Called by a {@link DBQueryOperation} that is wrapping us in a DB-First query plan: gets a
   * chunk of results that it feeds into a SQL query.
   */
  LuceneResultsChunk getNextResultsChunk(int max) {
    if (!haveRunSearch) {
      fetchFirstResults(max);
    }

    long start = System.currentTimeMillis();
    LuceneResultsChunk result = new LuceneResultsChunk();
    int luceneLen = hits != null ? hits.getTotalHits() : 0;
    while ((result.size() < max) && (curHitNo < luceneLen)) {
      if (topDocsLen <= curHitNo) {
        topDocsLen += topDocsChunkSize;
        topDocsChunkSize *= 4;
        if (topDocsChunkSize > 1000000) {
          topDocsChunkSize = 1000000;
        }
        if (topDocsLen > luceneLen) {
          topDocsLen = luceneLen;
        }
        runSearch();
      }

      Document doc;
      try {
        doc = searcher.doc(hits.getScoreDoc(curHitNo).getDocumentID());
      } catch (Exception e) {
        ZimbraLog.search.error(
            "Failed to retrieve Lucene document: %s",
            hits.getScoreDoc(curHitNo).getDocumentID().toString(), e);
        return result;
      }
      curHitNo++;
      String mbid = doc.get(LuceneFields.L_MAILBOX_BLOB_ID);
      if (mbid != null) {
        try {
          result.addHit(Integer.parseInt(mbid), doc);
        } catch (NumberFormatException e) {
          ZimbraLog.search.error("Invalid MAILBOX_BLOB_ID: " + mbid, e);
        }
      }
    }
    ZimbraLog.search.debug(
        "LuceneFetchDocs n=%d,elapsed=%d", luceneLen, System.currentTimeMillis() - start);
    return result;
  }

  /**
   * It is not possible to search for queries that only consist of a MUST_NOT clause. Combining with
   * MatchAllDocsQuery works in general, but we generate more than one documents per item for
   * multipart messages. If we match including non top level parts, negative queries will end up
   * matching everything. Therefore we only match the top level part for negative queries.
   */
  private BooleanQuery fixMustNotOnly(BooleanQuery query) {
    for (BooleanClause clause : query.clauses()) {
      if (clause.getQuery() instanceof BooleanQuery) {
        BooleanQuery fixed = fixMustNotOnly((BooleanQuery) clause.getQuery());
        if (fixed != clause.getQuery()) {
          // Need to rebuild the query with the fixed inner query
          BooleanQuery.Builder builder = new BooleanQuery.Builder();
          for (BooleanClause c : query.clauses()) {
            if (c.getQuery() == clause.getQuery()) {
              builder.add(fixed, c.getOccur());
            } else {
              builder.add(c);
            }
          }
          query = builder.build();
        }
      }
      if (clause.getOccur() != BooleanClause.Occur.MUST_NOT) {
        return query;
      }
    }

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (BooleanClause clause : query.clauses()) {
      builder.add(clause);
    }
    builder.add(
        new TermQuery(new Term(LuceneFields.L_PARTNAME, LuceneFields.L_PARTNAME_TOP)),
        BooleanClause.Occur.SHOULD);
    Set<MailItem.Type> types = context.getParams().getTypes();
    if (types.contains(MailItem.Type.CONTACT)) {
      builder.add(
          new TermQuery(new Term(LuceneFields.L_PARTNAME, LuceneFields.L_PARTNAME_CONTACT)),
          BooleanClause.Occur.SHOULD);
    }
    return builder.build();
  }

  /** Execute the actual search via Lucene */
  private void runSearch() {
    haveRunSearch = true;

    if (searcher
        == null) { // this can happen if the Searcher couldn't be opened, e.g. index does not exist
      hits = null;
      return;
    }

    try {
      if (luceneQuery instanceof BooleanQuery) {
        luceneQuery = fixMustNotOnly((BooleanQuery) luceneQuery);
      }
      luceneQuery = expandLazyMultiPhraseQuery(luceneQuery);
      if (luceneQuery == null) { // optimized away
        hits = null;
        return;
      }
      
      // Fold filter into query using TermInSetQuery
      Query effectiveQuery;
      if (filterTerms != null && !filterTerms.isEmpty()) {
        // All terms use the same field (L_MAILBOX_BLOB_ID) — confirmed
        String field = filterTerms.get(0).field();
        List<BytesRef> refs = new ArrayList<>();
        for (Term t : filterTerms) {
          refs.add(new BytesRef(t.text()));
        }
        Query filterQuery = new TermInSetQuery(field, refs);
        effectiveQuery = new BooleanQuery.Builder()
            .add(luceneQuery, BooleanClause.Occur.MUST)
            .add(filterQuery, BooleanClause.Occur.FILTER)
            .build();
      } else {
        effectiveQuery = luceneQuery;
      }
      
      long start = System.currentTimeMillis();
      if (sort == null) {
        hits = searcher.search(effectiveQuery, topDocsLen);
      } else {
        hits = searcher.search(effectiveQuery, topDocsLen, sort);
      }
      ZimbraLog.search.debug(
          "LuceneSearch query=%s,n=%d,total=%d,elapsed=%d",
          effectiveQuery, topDocsLen, hits.getTotalHits(), System.currentTimeMillis() - start);
    } catch (IOException e) {
      ZimbraLog.search.error("Failed to search query=%s", luceneQuery, e);
      IOUtil.closeQuietly(searcher);
      searcher = null;
      hits = null;
    }
  }

  private Query expandLazyMultiPhraseQuery(Query query) throws IOException {
    if (query instanceof LazyMultiPhraseQuery) {
      LazyMultiPhraseQuery lazy = (LazyMultiPhraseQuery) query;
      int max = LC.zimbra_index_wildcard_max_terms_expanded.intValue();
      MultiPhraseQuery.Builder builder = new MultiPhraseQuery.Builder();
      for (Term[] terms : lazy.getTermArrays()) {
        if (terms.length != 1) {
          builder.add(terms);
          continue;
        }
        Term base = terms[0];
        if (!lazy.expand.contains(base)) {
          builder.add(terms);
          continue;
        }
        List<Term> expanded = Lists.newArrayList();
        try (TermFieldEnumeration itr =
            searcher.getIndexReader().getTermsForField(base.field(), base.text())) {
          while (itr.hasMoreElements()) {
            BrowseTerm term = itr.nextElement();
            if (term != null && term.getText().startsWith(base.text())) {
              if (expanded.size() >= max) { // too many terms
                // expanded
                break;
              }
              expanded.add(new Term(base.field(), term.getText()));
            } else {
              break;
            }
          }
        }
        if (expanded.isEmpty()) {
          return null;
        } else {
          builder.add(expanded.toArray(new Term[0]));
        }
      }
      return builder.build();
    } else if (query instanceof BooleanQuery) {
      BooleanQuery.Builder boolBuilder = new BooleanQuery.Builder();
      boolean modified = false;
      for (BooleanClause clause : ((BooleanQuery) query).clauses()) {
        Query result = expandLazyMultiPhraseQuery(clause.getQuery());
        if (result == null) {
          if (clause.isRequired()) {
            return null;
          } else {
            modified = true;
          }
        } else if (result != clause.getQuery()) {
          boolBuilder.add(result, clause.getOccur());
          modified = true;
        } else {
          boolBuilder.add(clause);
        }
      }
      return modified ? boolBuilder.build() : query;
    } else {
      return query;
    }
  }

  @Override
  public String toString() {
    return "LUCENE(" + luceneQuery + (hasSpamTrashSetting() ? " <ANYWHERE>" : "") + ")";
  }

  /** Just clone *this* object, don't clone the embedded DBOp */
  private LuceneQueryOperation cloneInternal() {
    assert (!haveRunSearch);
    LuceneQueryOperation clone = (LuceneQueryOperation) super.clone();
    // Query objects are immutable in Lucene 9.x, no need to clone
    clone.luceneQuery = luceneQuery;
    return clone;
  }

  @Override
  public Object clone() {
    assert (searcher == null);
    LuceneQueryOperation toRet = cloneInternal();
    if (dbOp != null) {
      toRet.dbOp = (DBQueryOperation) dbOp.clone(this);
    }
    return toRet;
  }

  /**
   * Called from {@link DBQueryOperation#clone()}
   *
   * @param caller - our DBQueryOperation which has ALREADY BEEN CLONED
   */
  Object clone(DBQueryOperation caller) {
    assert (searcher == null);
    LuceneQueryOperation toRet = cloneInternal();
    toRet.setDBOperation(caller);
    return toRet;
  }

  /**
   * Must be called AFTER the first results chunk is fetched.
   *
   * @return number of hits in this search
   */
  private long getTotalHitCount() {
    return hits != null ? hits.getTotalHits() : 0;
  }

  @Override
  public long getCursorOffset() {
    return -1;
  }

  /** Reset our hit iterator back to the beginning of the result set. */
  void resetDocNum() {
    curHitNo = 0;
  }

  @Override
  protected QueryOperation combineOps(QueryOperation other, boolean union) {
    assert (!haveRunSearch);

    if (union) {
      if (other.hasNoResults()) {
        queryInfo.addAll(other.getResultInfo());
        // a query for (other OR nothing) == other
        return this;
      }
    } else {
      if (other.hasAllResults()) {
        if (other.hasSpamTrashSetting()) {
          forceHasSpamTrashSetting();
        }
        queryInfo.addAll(other.getResultInfo());
        // we match all results.  (other AND anything) == other
        return this;
      }
    }

    if (other instanceof LuceneQueryOperation) {
      LuceneQueryOperation otherLucene = (LuceneQueryOperation) other;
      if (union) {
        queryString = '(' + queryString + ") OR (" + otherLucene.queryString + ')';
      } else {
        queryString = '(' + queryString + ") AND (" + otherLucene.queryString + ')';
      }

      BooleanQuery.Builder builder = new BooleanQuery.Builder();
      if (union) {
        if (luceneQuery instanceof BooleanQuery) {
          orCopy((BooleanQuery) luceneQuery, builder);
        } else {
          builder.add(new BooleanClause(luceneQuery, Occur.SHOULD));
        }
        if (otherLucene.luceneQuery instanceof BooleanQuery) {
          orCopy((BooleanQuery) otherLucene.luceneQuery, builder);
        } else {
          builder.add(new BooleanClause(otherLucene.luceneQuery, Occur.SHOULD));
        }
      } else {
        if (luceneQuery instanceof BooleanQuery) {
          andCopy((BooleanQuery) luceneQuery, builder);
        } else {
          builder.add(new BooleanClause(luceneQuery, Occur.MUST));
        }
        if (otherLucene.luceneQuery instanceof BooleanQuery) {
          andCopy((BooleanQuery) otherLucene.luceneQuery, builder);
        } else {
          builder.add(new BooleanClause(otherLucene.luceneQuery, Occur.MUST));
        }
      }
      luceneQuery = builder.build();
      queryInfo.addAll(other.getResultInfo());
      if (other.hasSpamTrashSetting()) {
        forceHasSpamTrashSetting();
      }
      return this;
    }
    return null;
  }

  private void andCopy(BooleanQuery from, BooleanQuery.Builder to) {
    boolean allAnd = true;
    for (BooleanClause clause : from.clauses()) {
      if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
        allAnd = false;
        break;
      }
    }
    if (allAnd) {
      for (BooleanClause clause : from.clauses()) {
        to.add(clause);
      }
    } else {
      to.add(new BooleanClause(from, Occur.MUST));
    }
  }

  private void orCopy(BooleanQuery from, BooleanQuery.Builder to) {
    boolean allOr = true;
    for (BooleanClause clause : from.clauses()) {
      if (clause.getOccur() != BooleanClause.Occur.SHOULD) {
        allOr = false;
        break;
      }
    }
    if (allOr) {
      for (BooleanClause clause : from.clauses()) {
        to.add(clause);
      }
    } else {
      to.add(new BooleanClause(from, Occur.SHOULD));
    }
  }

  @Override
  void forceHasSpamTrashSetting() {
    hasSpamTrashSetting = true;
  }

  List<QueryInfo> getQueryInfo() {
    return queryInfo;
  }

  public String getQueryString() {
    return queryString;
  }

  public Query getQuery() {
    return luceneQuery;
  }

  @Override
  boolean hasSpamTrashSetting() {
    return hasSpamTrashSetting;
  }

  @Override
  boolean hasNoResults() {
    return false;
  }

  @Override
  boolean hasAllResults() {
    return false;
  }

  @Override
  QueryOperation expandLocalRemotePart(Mailbox mbox) throws ServiceException {
    return this;
  }

  @Override
  QueryOperation ensureSpamTrashSetting(Mailbox mbox, boolean includeTrash, boolean includeSpam)
      throws ServiceException {
    // wrap ourselves in a DBQueryOperation, since we're eventually going to need to go to the DB
    DBQueryOperation dbOp = new DBQueryOperation();
    dbOp.setLuceneQueryOperation(this);
    return dbOp.ensureSpamTrashSetting(mbox, includeTrash, includeSpam);
  }

  @Override
  Set<QueryTarget> getQueryTargets() {
    return ImmutableSet.of(QueryTarget.UNSPECIFIED);
  }

  void setDBOperation(DBQueryOperation op) {
    dbOp = op;
  }

  @Override
  public void resetIterator() throws ServiceException {
    if (dbOp != null) {
      dbOp.resetIterator();
    }
  }

  @Override
  public ZimbraHit getNext() throws ServiceException {
    if (dbOp != null) {
      return dbOp.getNext();
    }
    return null;
  }

  @Override
  public ZimbraHit peekNext() throws ServiceException {
    if (dbOp != null) {
      return dbOp.peekNext();
    }
    return null;
  }

  @Override
  public List<QueryInfo> getResultInfo() {
    List<QueryInfo> toRet = new ArrayList<>(queryInfo);
    if (dbOp != null) {
      toRet.addAll(dbOp.getQueryInfo());
    }
    return toRet;
  }

  @Override
  QueryOperation optimize(Mailbox mbox) {
    return this;
  }

  /** Helper for implementing QueryOperation.depthFirstRecurse(RecurseCallback) */
  void depthFirstRecurseInternal(RecurseCallback cb) {
    cb.recurseCallback(this);
  }

  @Override
  protected void depthFirstRecurse(RecurseCallback cb) {
    if (dbOp != null) {
      dbOp.depthFirstRecurse(cb);
    } else {
      depthFirstRecurseInternal(cb);
    }
  }

  /**
   * Allows the parser (specifically the Query subclasses) to store some query result information so
   * that it can be returned to the caller after the query has run. This is used for things like
   * spelling suggestion correction, or wildcard expansion info: things that are not results per-se
   * but still need to have some way to be sent back to the caller.
   */
  public void addQueryInfo(QueryInfo inf) {
    queryInfo.add(inf);
  }

  /** Can be called more than once recursively from {@link DBQueryOperation}. */
  @Override
  protected final void begin(QueryContext ctx) throws ServiceException {
    assert (!haveRunSearch);
    context = ctx;
    if (dbOp == null) { // 1st time called
      // wrap ourselves in a DBQueryOperation, since we're eventually
      // going to need to go to the DB
      dbOp = new DBQueryOperation();
      dbOp.setLuceneQueryOperation(this);
      dbOp.begin(ctx); // will call back into this method again!
    } else { // 2nd time called
      try {
        searcher = ctx.getMailbox().index.getIndexStore().openSearcher();
      } catch (IOException e) {
        throw ServiceException.FAILURE("Failed to open searcher", e);
      }
      sort = toLuceneSort(ctx.getResults().getSortBy());
    }
  }

  private Sort toLuceneSort(SortBy sortBy) {
    if (sortBy == null) {
      return null;
    }

    switch (sortBy.getKey()) {
      case NONE:
        return null;
      case NAME:
      case NAME_NATURAL_ORDER:
      case SENDER:
        return new Sort(
            new SortField(
                LuceneFields.L_SORT_NAME,
                SortField.Type.STRING,
                sortBy.getDirection() == SortBy.Direction.DESC));
      case SUBJECT:
        return new Sort(
            new SortField(
                LuceneFields.L_SORT_SUBJECT,
                SortField.Type.STRING,
                sortBy.getDirection() == SortBy.Direction.DESC));
      case SIZE:
        return new Sort(
            new SortField(
                LuceneFields.L_SORT_SIZE,
                SortField.Type.LONG,
                sortBy.getDirection() == SortBy.Direction.DESC));
      case ATTACHMENT:
        return new Sort(
            new SortField(
                LuceneFields.L_SORT_ATTACH,
                SortField.Type.STRING,
                sortBy.getDirection() == SortBy.Direction.DESC));
      case FLAG:
        return new Sort(
            new SortField(
                LuceneFields.L_SORT_FLAG,
                SortField.Type.STRING,
                sortBy.getDirection() == SortBy.Direction.DESC));
      case PRIORITY:
        return new Sort(
            new SortField(
                LuceneFields.L_SORT_PRIORITY,
                SortField.Type.STRING,
                sortBy.getDirection() == SortBy.Direction.DESC));
      case RCPT:
        assert false : sortBy; // should already be checked in the compile phase
      case DATE:
      default: // default to DATE_DESCENDING
        return new Sort(
            new SortField(
                LuceneFields.L_SORT_DATE,
                SortField.Type.STRING,
                sortBy.getDirection() == SortBy.Direction.DESC));
    }
  }

  /**
   * We use this data structure to track a "chunk" of Lucene hits which the {@link DBQueryOperation}
   * will use to check against the DB.
   */
  static final class LuceneResultsChunk {
    private final Multimap<Integer, Document> hits = LinkedHashMultimap.create();

    Set<Integer> getIndexIds() {
      return hits.keySet();
    }

    int size() {
      return hits.size();
    }

    void addHit(int indexId, Document doc) {
      hits.put(indexId, doc);
    }

    Collection<Document> getHit(int indexId) {
      return hits.get(indexId);
    }
  }

  /**
   * Extended query that defers wildcard expansion until actual Lucene search
   * execution, rather than doing so when creating a {@link MultiPhraseQuery}.
   *
   * @see LuceneQueryOperation#expandLazyMultiPhraseQuery(Query)
   */
  public static final class LazyMultiPhraseQuery extends Query {
    private static final long serialVersionUID = -6754267749628771968L;

    private final List<Term[]> termArrays = Lists.newArrayList();
    final Set<Term> expand = Sets.newIdentityHashSet();

    public void add(Term term) {
      add(new Term[] { term });
    }

    public void add(Term[] terms) {
      termArrays.add(terms);
    }

    public List<Term[]> getTermArrays() {
      return termArrays;
    }

    public void expand(Term term) {
      add(term);
      expand.add(term);
    }

    @Override
    public String toString(String field) {
      return "LazyMultiPhraseQuery(" + termArrays + ")";
    }

    @Override
    public int hashCode() {
      return termArrays.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || !(obj instanceof LazyMultiPhraseQuery)) return false;
      LazyMultiPhraseQuery other = (LazyMultiPhraseQuery) obj;
      return termArrays.equals(other.termArrays);
    }

    @Override
    public void visit(org.apache.lucene.search.QueryVisitor visitor) {
      // No-op for lazy query
    }
  }
}
