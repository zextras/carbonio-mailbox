// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.index.ZimbraIndexReader.TermFieldEnumeration;
import com.zimbra.cs.mailbox.Contact;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mime.ParsedContact;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractIndexStoreTest {
    static String originalIndexStoreFactory;
    static Provisioning prov;
    static Account testAcct;

    protected abstract String getIndexStoreFactory();

    /**
     * Override this for any Index Store specific cleanup.  Note that for Mock Provisioning, deleting an account
     * does not currently cleanup the index.
     */
    protected void cleanupForIndexStore() {
    }

    /**
     * Override this for any Index Store which might not be available;
     */
    protected boolean indexStoreAvailable() {
        return true;
    }


    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        prov = Provisioning.getInstance();
        testAcct = prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
        originalIndexStoreFactory = IndexStore.getFactory().getClass().getName();
    }

    @AfterAll
    public static void destroy() {
        try {
            prov.deleteAccount(testAcct.getId());
        } catch (ServiceException e) {
            ZimbraLog.test.error("Problem cleaning up test@zimbra.com account", e);
        }
        IndexStore.getFactory().destroy();
        IndexStore.setFactory(originalIndexStoreFactory);
    }

    @AfterEach
    public void teardown() throws Exception {
        IndexStore.getFactory().destroy();
        cleanupForIndexStore();
        MailboxTestUtil.clearData();
    }

    @BeforeEach
    public void setup() throws Exception {
        IndexStore.setFactory(getIndexStoreFactory());
        assertTrue(indexStoreAvailable(), "Index Store NEEDS to be configured and available");
        MailboxTestUtil.clearData();
        cleanupForIndexStore();
    }

 @Test
 void termQuery() throws Exception {
  ZimbraLog.test.debug("--->TEST termQuery");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  Contact contact = createContact(mbox, "First", "Last", "test@zimbra.com");
  createContact(mbox, "a", "bc", "abc@zimbra.com");
  createContact(mbox, "j", "k", "j.k@zimbra.com");
  createContact(mbox, "Matilda", "Higgs-Bozon", "matilda.higgs.bozon@zimbra.com");
  mbox.index.indexDeferredItems();

  // Stick with just one IndexStore - the one cached in Mailbox:
  //    IndexStore index = IndexStore.getFactory().getIndexStore(mbox);
  IndexStore index = mbox.index.getIndexStore();
  ZimbraIndexSearcher searcher = index.openSearcher();
  ZimbraTopDocs result = searcher.search(
    new TermQuery(new Term(LuceneFields.L_CONTACT_DATA, "none@zimbra.com")), 100);
  assertNotNull(result, "searcher.search result object - searching for none@zimbra.com");
  ZimbraLog.test.debug("Result for search for 'none@zimbra.com'\n" + result.toString());
  assertEquals(0, result.getTotalHits(), "Number of hits searching for none@zimbra.com");

  result = searcher.search(new TermQuery(new Term(LuceneFields.L_CONTACT_DATA, "test@zimbra.com")), 100);
  assertNotNull(result, "searcher.search result object - searching for test@zimbra.com");
  ZimbraLog.test.debug("Result for search for 'test@zimbra.com'\n" + result.toString());
  assertEquals(1, result.getTotalHits(), "Number of hits searching for test@zimbra.com");
  assertEquals(String.valueOf(contact.getId()), getBlobIdForResultDoc(searcher, result, 0));
  assertEquals(4, searcher.getIndexReader().numDocs());
  searcher.close();
 }

 @Test
 void filteredTermQuery() throws Exception {
  ZimbraLog.test.debug("--->TEST filteredTermQuery");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  Folder folder = mbox.getFolderById(null, Mailbox.ID_FOLDER_CONTACTS);
  Contact contact1 = createContact(mbox, "a", "bc", "abc@zimbra.com");
  Contact contact2 = createContact(mbox, "a", "bcd", "abcd@zimbra.com");
  Contact contact3 = createContact(mbox, "x", "y", "xy@zimbra.com");
  Contact contact4 = createContact(mbox, "x", "yz", "xyz@zimbra.com");
  Contact contact5 = createContact(mbox, "x", "yz", "xyz@zimbra.com");
  mbox.index.indexDeferredItems(); // Make sure we don't index items after the deleteIndex() below

  IndexStore index = mbox.index.getIndexStore();
  index.deleteIndex();
  Indexer indexer = index.openIndexer();
  indexer.addDocument(folder, contact1, contact1.generateIndexData());
  indexer.addDocument(folder, contact2, contact2.generateIndexData());
  indexer.addDocument(folder, contact3, contact3.generateIndexData());
  indexer.addDocument(folder, contact4, contact4.generateIndexData());
  // Note: NOT indexed contact5
  indexer.close();

  List<Term> terms = Lists.newArrayList();
  terms.add(new Term(LuceneFields.L_MAILBOX_BLOB_ID, String.valueOf(contact2.getId())));
  terms.add(new Term(LuceneFields.L_MAILBOX_BLOB_ID, String.valueOf(contact4.getId())));
  terms.add(new Term(LuceneFields.L_MAILBOX_BLOB_ID, String.valueOf(contact5.getId())));
  ZimbraTermsFilter filter = new ZimbraTermsFilter(terms);
  ZimbraIndexSearcher searcher = index.openSearcher();
  ZimbraTopDocs result;
  result = searcher.search(new TermQuery(new Term(LuceneFields.L_CONTACT_DATA, "zimbra.com")), filter, 100);
  assertNotNull(result, "searcher.search result object - searching for zimbra.com");
  ZimbraLog.test.debug("Result for search for 'zimbra.com', filtering by IDs\n%s", result.toString());
  assertEquals(2, result.getTotalHits(), "Number of hits");
  List<String> expecteds = Lists.newArrayList();
  List<String> matches = Lists.newArrayList();
  matches.add(getBlobIdForResultDoc(searcher, result, 0));
  matches.add(getBlobIdForResultDoc(searcher, result, 1));
  expecteds.add(String.valueOf(contact2.getId()));
  expecteds.add(String.valueOf(contact4.getId()));
  Collections.sort(matches);
  Collections.sort(expecteds);
  assertEquals(expecteds.get(0), matches.get(0), "Match Blob ID");
  assertEquals(expecteds.get(1), matches.get(1), "Match Blob ID");
  searcher.close();
 }

 @Test
 void sortedFilteredTermQuery() throws Exception {
  ZimbraLog.test.debug("--->TEST sortedFilteredTermQuery");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  Folder folder = mbox.getFolderById(null, Mailbox.ID_FOLDER_CONTACTS);
  Contact con1 = createContact(mbox, "a", "bc", "abc@zimbra.com");
  Contact con2 = createContact(mbox, "abcd@zimbra.com");
  Contact con3 = createContact(mbox, "xy@zimbra.com");
  Thread.sleep(1001);  // To ensure different sort date
        Contact con4 = createContact(mbox, "xyz@zimbra.com");
  Contact con5 = createContact(mbox, "xyz@zimbra.com");
  mbox.index.indexDeferredItems(); // Make sure we don't index items after the deleteIndex() below

  IndexStore index = mbox.index.getIndexStore();
  index.deleteIndex();
  Indexer indexer = index.openIndexer();
  indexer.addDocument(folder, con1, con1.generateIndexData());
  indexer.addDocument(folder, con2, con2.generateIndexData());
  indexer.addDocument(folder, con3, con3.generateIndexData());
  indexer.addDocument(folder, con4, con4.generateIndexData());
  // Note: NOT indexed contact5
  indexer.close();

  List<Term> terms = Lists.newArrayList();
  terms.add(new Term(LuceneFields.L_MAILBOX_BLOB_ID, String.valueOf(con2.getId())));
  terms.add(new Term(LuceneFields.L_MAILBOX_BLOB_ID, String.valueOf(con4.getId())));
  terms.add(new Term(LuceneFields.L_MAILBOX_BLOB_ID, String.valueOf(con5.getId())));
  ZimbraTermsFilter filter = new ZimbraTermsFilter(terms);
  ZimbraIndexSearcher srchr = index.openSearcher();
  ZimbraTopDocs result;
  Sort sort = new Sort(new SortField(LuceneFields.L_SORT_DATE, SortField.STRING, false));
  result = srchr.search(new TermQuery(new Term(LuceneFields.L_CONTACT_DATA, "zimbra.com")), filter, 100, sort);
  assertNotNull(result, "searcher.search result object - searching for zimbra.com");
  ZimbraLog.test.debug("Result for search for 'zimbra.com', filtering by IDs 2,4 & 5\n%s", result.toString());
  assertEquals(2, result.getTotalHits(), "Number of hits");
  assertEquals(String.valueOf(con2.getId()), getBlobIdForResultDoc(srchr, result, 0), "Match Blob ID 1");
  assertEquals(String.valueOf(con4.getId()), getBlobIdForResultDoc(srchr, result, 1), "Match Blob ID 2");
  // Repeat but with a reverse sort this time
  sort = new Sort(new SortField(LuceneFields.L_SORT_DATE, SortField.STRING, true));
  result = srchr.search(new TermQuery(new Term(LuceneFields.L_CONTACT_DATA, "zimbra.com")), filter, 100, sort);
  assertNotNull(result, "searcher.search result object - searching for zimbra.com");
  ZimbraLog.test.debug("Result for search for 'zimbra.com' sorted reverse, filter by IDs\n%s", result.toString());
  assertEquals(2, result.getTotalHits(), "Number of hits");
  assertEquals(String.valueOf(con4.getId()), getBlobIdForResultDoc(srchr, result, 0), "Match Blob ID 1");
  assertEquals(String.valueOf(con2.getId()), getBlobIdForResultDoc(srchr, result, 1), "Match Blob ID 2");
  srchr.close();
 }

 @Test
 void leadingWildcardQuery() throws Exception {
  ZimbraLog.test.debug("--->TEST leadingWildcardQuery");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  Contact contact = createContact(mbox, "First", "Last", "f.last@zimbra.com", "Leading Wildcard");
  createContact(mbox, "Grand", "Piano", "grand@vmware.com");
  mbox.index.indexDeferredItems(); // Make sure all indexing has been done

  IndexStore index = mbox.index.getIndexStore();
  ZimbraIndexSearcher searcher = index.openSearcher();
  // This seems to be the supported way of enabling leading wildcard queries for Lucene
  QueryParser queryParser = new QueryParser(LuceneIndex.VERSION, LuceneFields.L_CONTACT_DATA,
    new StandardAnalyzer(LuceneIndex.VERSION));
  queryParser.setAllowLeadingWildcard(true);
  Query query = queryParser.parse("*irst");
  ZimbraTopDocs result = searcher.search(query, 100);
  assertNotNull(result, "searcher.search result object - searching for *irst");
  ZimbraLog.test.debug("Result for search for '*irst'\n" + result.toString());
  assertEquals(1, result.getTotalHits(), "Number of hits searching for *irst");
  String expected1Id = String.valueOf(contact.getId());
  String match1Id = searcher.doc(result.getScoreDoc(0).getDocumentID()).get(LuceneFields.L_MAILBOX_BLOB_ID);
  assertEquals(expected1Id, match1Id, "Mailbox Blob ID of match");
 }

 @Test
 void booleanQuery() throws Exception {
  ZimbraLog.test.debug("--->TEST booleanQuery");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  Contact contact = createContact(mbox, "First", "Last", "f.last@zimbra.com", "Software Development Engineer");
  createContact(mbox, "Given", "Surname", "GiV.SurN@zimbra.com");
  mbox.index.indexDeferredItems(); // Make sure all indexing has been done

  IndexStore index = mbox.index.getIndexStore();
  ZimbraIndexSearcher searcher = index.openSearcher();
  // This seems to be the supported way of enabling leading wildcard queries
  QueryParser queryParser = new QueryParser(LuceneIndex.VERSION, LuceneFields.L_CONTACT_DATA,
    new StandardAnalyzer(LuceneIndex.VERSION));
  queryParser.setAllowLeadingWildcard(true);
  Query wquery = queryParser.parse("*irst");
  Query tquery = new TermQuery(new Term(LuceneFields.L_CONTACT_DATA, "absent"));
  Query tquery2 = new TermQuery(new Term(LuceneFields.L_CONTACT_DATA, "Last"));
  BooleanQuery bquery = new BooleanQuery();
  bquery.add(wquery, Occur.MUST);
  bquery.add(tquery, Occur.MUST_NOT);
  bquery.add(tquery2, Occur.SHOULD);
  ZimbraTopDocs result = searcher.search(bquery, 100);
  assertNotNull(result, "searcher.search result object");
  ZimbraLog.test.debug("Result for search [hits=%d]:%s", result.getTotalHits(), result.toString());
  assertEquals(1, result.getTotalHits(), "Number of hits");
  String expected1Id = String.valueOf(contact.getId());
  String match1Id = searcher.doc(result.getScoreDoc(0).getDocumentID()).get(LuceneFields.L_MAILBOX_BLOB_ID);
  assertEquals(expected1Id, match1Id, "Mailbox Blob ID of match");
 }

 @Test
 void phraseQuery() throws Exception {
  ZimbraLog.test.debug("--->TEST phraseQuery");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  createContact(mbox, "Non", "Match", "nOn.MaTchiNg@zimbra.com");
  Contact contact2 = createContact(mbox, "First", "Last", "f.last@zimbra.com", "Software Development Engineer");
  createContact(mbox, "Given", "Surname", "GiV.SurN@zimbra.com");
  mbox.index.indexDeferredItems(); // Make sure all indexing has been done

  IndexStore index = mbox.index.getIndexStore();
  ZimbraIndexSearcher searcher = index.openSearcher();
  PhraseQuery pquery = new PhraseQuery();
  // Lower case required for each term for Lucene
  pquery.add(new Term(LuceneFields.L_CONTENT, "software"));
  pquery.add(new Term(LuceneFields.L_CONTENT, "development"));
  pquery.add(new Term(LuceneFields.L_CONTENT, "engineer"));
  ZimbraTopDocs result = searcher.search(pquery, 100);
  assertNotNull(result, "searcher.search result object");
  ZimbraLog.test.debug("Result for search [hits=%d]:%s", result.getTotalHits(), result.toString());
  assertEquals(1, result.getTotalHits(), "Number of hits");
  String expected1Id = String.valueOf(contact2.getId());
  String match1Id = getBlobIdForResultDoc(searcher, result, 0);
  assertEquals(expected1Id, match1Id, "Mailbox Blob ID of match");
  pquery = new PhraseQuery();
  // Try again with words out of order
  pquery.add(new Term(LuceneFields.L_CONTENT, "development"));
  pquery.add(new Term(LuceneFields.L_CONTENT, "software"));
  pquery.add(new Term(LuceneFields.L_CONTENT, "engineer"));
  result = searcher.search(pquery, 100);
  assertNotNull(result, "searcher.search result object");
  ZimbraLog.test.debug("Result for search [hits=%d]:%s", result.getTotalHits(), result.toString());
  assertEquals(0, result.getTotalHits(), "Number of hits");
 }

 @Test
 void phraseQueryWithStopWord() throws Exception {
  ZimbraLog.test.debug("--->TEST phraseQueryWithStopWord");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  createContact(mbox, "Non", "Match", "nOn.MaTchiNg@zimbra.com");
  Contact contact2 = createContact(mbox, "First", "Last", "f.last@zimbra.com",
    "1066 and all that with William the conqueror and others");
  createContact(mbox, "Given", "Surname", "GiV.SurN@zimbra.com");
  mbox.index.indexDeferredItems(); // Make sure all indexing has been done

  IndexStore index = mbox.index.getIndexStore();
  ZimbraIndexSearcher searcher = index.openSearcher();
  PhraseQuery pquery = new PhraseQuery();
  // Lower case required for each term for Lucene
  pquery.add(new Term(LuceneFields.L_CONTENT, "william"));
  // pquery.add(new Term(LuceneFields.L_CONTENT, "the")); - excluded because it is a stop word
  pquery.add(new Term(LuceneFields.L_CONTENT, "conqueror"));
  ZimbraTopDocs result = searcher.search(pquery, 100);
  assertNotNull(result, "searcher.search result object");
  ZimbraLog.test.debug("Result for search [hits=%d]:%s", result.getTotalHits(), result.toString());
  assertEquals(1, result.getTotalHits(), "Number of hits");
  String expected1Id = String.valueOf(contact2.getId());
  String match1Id = getBlobIdForResultDoc(searcher, result, 0);
  assertEquals(expected1Id, match1Id, "Mailbox Blob ID of match");
 }

 @Test
 void multiPhraseQuery() throws Exception {
  ZimbraLog.test.debug("--->TEST multiPhraseQuery");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  createContact(mbox, "Non", "Match", "nOn.MaTchiNg@zimbra.com");
  Contact contact1 = createContact(mbox, "Paul", "AA",  "aa@example.net", "Software Development Engineer");
  createContact(mbox, "Jane", "BB",  "bb@example.net", "Software Planning Engineer");
  Contact contact2 = createContact(mbox, "Peter", "CC", "cc@example.net", "Software Dev Engineer");
  createContact(mbox, "Avril", "DD", "dd@example.net", "Software Architectural Engineer");
  Contact contact3 = createContact(mbox, "Leo", "EE",   "ee@example.net", "Software Developer Engineer");
  Contact contact4 = createContact(mbox, "Wow", "DD", "dd@example.net", "Softly Development Engineer");
  createContact(mbox, "Given", "Surname", "GiV.SurN@zimbra.com");
  mbox.index.indexDeferredItems(); // Make sure all indexing has been done

  IndexStore index = mbox.index.getIndexStore();
  ZimbraIndexSearcher searcher = index.openSearcher();
  MultiPhraseQuery pquery = new MultiPhraseQuery();
  // Lower case required for each term for Lucene
  Term[] firstWords = {new Term(LuceneFields.L_CONTENT, "softly"),
    new Term(LuceneFields.L_CONTENT, "software")
  };
  pquery.add(firstWords);
  Term[] secondWords = {new Term(LuceneFields.L_CONTENT, "dev"),
    new Term(LuceneFields.L_CONTENT, "development"),
    new Term(LuceneFields.L_CONTENT, "developer")
  };
  pquery.add(secondWords);
  pquery.add(new Term(LuceneFields.L_CONTENT, "engineer"));
  ZimbraTopDocs result = searcher.search(pquery, 100);
  assertNotNull(result, "searcher.search result object");
  ZimbraLog.test.debug("Result for search [hits=%d]:%s", result.getTotalHits(), result.toString());
  assertEquals(4, result.getTotalHits(), "Number of hits");
  List<String> expecteds = Lists.newArrayList();
  List<String> matches = Lists.newArrayList();
  matches.add(getBlobIdForResultDoc(searcher, result, 0));
  matches.add(getBlobIdForResultDoc(searcher, result, 1));
  matches.add(getBlobIdForResultDoc(searcher, result, 2));
  matches.add(getBlobIdForResultDoc(searcher, result, 3));
  expecteds.add(String.valueOf(contact1.getId()));
  expecteds.add(String.valueOf(contact2.getId()));
  expecteds.add(String.valueOf(contact3.getId()));
  expecteds.add(String.valueOf(contact4.getId()));
  Collections.sort(matches);
  Collections.sort(expecteds);
  for (int ndx = 0; ndx < 4; ndx++) {
   assertEquals(expecteds.get(0), matches.get(0), "Match Blob ID");
  }
 }

 @Test
 void prefixQuery() throws Exception {
  ZimbraLog.test.debug("--->TEST prefixQuery");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  Contact contact1 = createContact(mbox, "a", "bc", "abc@zimbra.com");
  Contact contact2 = createContact(mbox, "a", "bcd", "abcd@zimbra.com");
  createContact(mbox, "x", "Y", "xy@zimbra.com");
  createContact(mbox, "x", "Yz", "x.Yz@zimbra.com");
  mbox.index.indexDeferredItems(); // Make sure all indexing has been done
  IndexStore index = mbox.index.getIndexStore();
  ZimbraIndexSearcher searcher = index.openSearcher();
  ZimbraTopDocs result = searcher.search(new PrefixQuery(new Term(LuceneFields.L_CONTACT_DATA, "ab")), 100);
  assertNotNull(result, "searcher.search result object - searching for 'ab' prefix");
  ZimbraLog.test.debug("Result for search for 'ab'\n" + result.toString());
  assertEquals(2, result.getTotalHits(), "Number of hits searching for 'ab' prefix");
  String contact1Id = String.valueOf(contact1.getId());
  String contact2Id = String.valueOf(contact2.getId());
  String match1Id = getBlobIdForResultDoc(searcher, result, 0);
  String match2Id = getBlobIdForResultDoc(searcher, result, 1);
  ZimbraLog.test.debug("Contact1ID=%s Contact2ID=%s match1id=%s match2id=%s",
    contact1Id, contact2Id, match1Id, match2Id);
  if (contact1Id.equals(match1Id)) {
   assertEquals(contact2Id, match2Id, "2nd match isn't contact2's ID");
  } else if (contact1Id.equals(match2Id)) {
   assertEquals(contact2Id, match1Id, "2nd match isn't contact1's ID");
  } else {
   fail(String.format("Contact 1 ID [%s] doesn't match either [%s] or [%s]",
     contact1Id, match1Id, match2Id));
  }
 }

 @Test
 void termRangeQuery() throws Exception {
  ZimbraLog.test.debug("--->TEST termRangeQuery");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  Contact contact1 = createContact(mbox, "James", "Peters", "abc@zimbra.com");
  Contact contact2 = createContact(mbox, "a", "bcd", "abcd@zimbra.com");
  createContact(mbox, "aa", "bcd", "aaaa@zimbra.com");
  createContact(mbox, "aa", "bcd", "zzz@zimbra.com");

  mbox.index.indexDeferredItems(); // Make sure all indexing has been done
  IndexStore index = mbox.index.getIndexStore();
  ZimbraIndexSearcher searcher = index.openSearcher();
  TermRangeQuery query = new TermRangeQuery(LuceneFields.L_FIELD,
    "email:aba@zimbra.com",
    "email:abz@zimbra.com",
    false, true);
  ZimbraTopDocs result = searcher.search(query, 100);
  assertNotNull(result, "searcher.search result object");
  ZimbraLog.test.debug("Result for search %s", result.toString());
  assertEquals(2, result.getTotalHits(), "Number of hits");
  List<String> expecteds = Lists.newArrayList();
  List<String> matches = Lists.newArrayList();
  matches.add(getBlobIdForResultDoc(searcher, result, 0));
  matches.add(getBlobIdForResultDoc(searcher, result, 1));
  expecteds.add(String.valueOf(contact1.getId()));
  expecteds.add(String.valueOf(contact2.getId()));
  Collections.sort(matches);
  Collections.sort(expecteds);
  assertEquals(expecteds.get(0), matches.get(0), "Match Blob ID");
  assertEquals(expecteds.get(1), matches.get(1), "Match Blob ID");
 }

 @Test
 void deleteDocument() throws Exception {
  ZimbraLog.test.debug("--->TEST deleteDocument");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  IndexStore index = mbox.index.getIndexStore();
  index.deleteIndex();
  Indexer indexer = index.openIndexer();
  assertEquals(0, indexer.maxDocs(), "maxDocs at start");
  Contact contact1 = createContact(mbox, "James", "Peters", "test1@zimbra.com");
  createContact(mbox, "Emma", "Peters", "test2@zimbra.com");

  mbox.index.indexDeferredItems(); // Make sure all indexing has been done
  ZimbraIndexSearcher searcher = index.openSearcher();
  assertEquals(2, searcher.getIndexReader().numDocs(), "numDocs after 2 adds");
  ZimbraTopDocs result = searcher.search(
    new TermQuery(new Term(LuceneFields.L_CONTACT_DATA, "@zimbra.com")), 100);
  assertNotNull(result, "searcher.search result object - searching for '@zimbra.com'");
  ZimbraLog.test.debug("Result for search for '@zimbra.com'\n" + result.toString());
  assertEquals(2, result.getTotalHits(), "Total hits after 2 adds");
  searcher.close();

  indexer = index.openIndexer();
  indexer.deleteDocument(Collections.singletonList(contact1.getId()));
  indexer.close();

  searcher = index.openSearcher();
  assertEquals(1, searcher.getIndexReader().numDocs(), "numDocs after 2 adds/1 del");
  result = searcher.search(new TermQuery(new Term(LuceneFields.L_CONTACT_DATA, "@zimbra.com")), 100);
  assertNotNull(result, "searcher.search result object after 2 adds/1 del");
  ZimbraLog.test.debug("Result for search for '@zimbra.com'\n" + result.toString());
  assertEquals(1, result.getTotalHits(), "Total hits after 2 adds/1 del");
  searcher.close();
 }

 @Test
 void getCount() throws Exception {
  ZimbraLog.test.debug("--->TEST getCount");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  IndexStore index = mbox.index.getIndexStore();
  index.deleteIndex();
  Indexer indexer = index.openIndexer();
  assertEquals(0, indexer.maxDocs(), "maxDocs at start");
  createContact(mbox, "Jane", "Peters", "test1@zimbra.com");
  createContact(mbox, "Emma", "Peters", "test2@zimbra.com");
  createContact(mbox, "Fiona", "Peters", "test3@zimbra.com");
  createContact(mbox, "Edward", "Peters", "test4@zimbra.com");
  mbox.index.indexDeferredItems(); // Make sure all indexing has been done

  assertEquals(4, indexer.maxDocs(), "maxDocs after adding 4 contacts");
  indexer.close();

  ZimbraIndexSearcher searcher = index.openSearcher();
  assertEquals(4, searcher.getIndexReader().numDocs(), "numDocs after adding 4 contacts");
  assertEquals(1,
    searcher.docFreq(new Term(LuceneFields.L_CONTACT_DATA, "test1")),
    "docs which match 'test1'");
  assertEquals(4,
    searcher.docFreq(new Term(LuceneFields.L_CONTACT_DATA, "@zimbra.com")),
    "docs which match '@zimbra.com'");
  searcher.close();
 }

    private void checkNextTerm(TermFieldEnumeration fields, Term term) {
        assertTrue(fields.hasMoreElements(), "fields.hasMoreElements() value when expecting:" + term.toString());
        BrowseTerm browseTerm = fields.nextElement();
        assertNotNull(browseTerm, "fields.nextElement() value when expecting:" + term.toString());
        ZimbraLog.test.debug("Expecting %s=%s value is %s docFreq=%d",
                term.field(), term.text(), browseTerm.getText(), browseTerm.getFreq());
        assertEquals(term.text(), browseTerm.getText(), "field value");
    }
    private void checkNextTermFieldType(TermFieldEnumeration fields, String field) {
        assertTrue(fields.hasMoreElements(), "fields.hasMoreElements() value when expecting:" + field);
        BrowseTerm browseTerm = fields.nextElement();
        assertNotNull(browseTerm, "fields.nextElement() value when expecting:" + field);
        ZimbraLog.test.debug("Expecting %s=?anyvalue? value is %s docFreq=%d",
                field, browseTerm.getText(), browseTerm.getFreq());
    }

    private void checkAtEnd(TermFieldEnumeration fields, String field) {
        assertFalse(fields.hasMoreElements(), "fields.hasMoreElements() at end of list for field:" + field);
        try {
            fields.nextElement();
            fail("fields.nextElement() at end of list for field:" + field + " contact data succeeded");
        } catch (NoSuchElementException ex) {
        }
    }

 /**
  * The result of getTermsForField can be good for seeing the effects of {@code ZimbraAnalyzer} on how fields get
  * tokenized. TODO:  Add tests for different types of tokenizers.
  * @throws Exception
     */
 @Test
 void termEnum() throws Exception {
  ZimbraLog.test.debug("--->TEST termEnum");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(testAcct.getId());
  createContact(mbox, "teSt1@ziMBRA.com");
  createContact(mbox, "test2@zimbra.com");

  mbox.index.indexDeferredItems(); // Make sure all indexing has been done
  IndexStore index = mbox.index.getIndexStore();
  ZimbraIndexSearcher searcher = index.openSearcher();
  // Note that TermFieldEnumeration order is defined to be sorted
  try (TermFieldEnumeration fields = searcher.getIndexReader()
    .getTermsForField(LuceneFields.L_CONTACT_DATA, "")) {
   checkNextTerm(fields, new Term(LuceneFields.L_CONTACT_DATA, "@zimbra"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTACT_DATA, "@zimbra.com"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTACT_DATA, "test1"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTACT_DATA, "test1@zimbra.com"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTACT_DATA, "test2"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTACT_DATA, "test2@zimbra.com"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTACT_DATA, "zimbra"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTACT_DATA, "zimbra.com"));
   checkAtEnd(fields, LuceneFields.L_CONTACT_DATA);
  }
  try (TermFieldEnumeration fields = searcher.getIndexReader()
    .getTermsForField(LuceneFields.L_CONTENT, "")) {
   // l.content values:
   // "test1@zimbra.com test1 @zimbra.com zimbra.com zimbra @zimbra  "
   // "test2@zimbra.com test2 @zimbra.com zimbra.com zimbra @zimbra  "
            
   checkNextTerm(fields, new Term(LuceneFields.L_CONTENT, "test1"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTENT, "test1@zimbra.com"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTENT, "test2"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTENT, "test2@zimbra.com"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTENT, "zimbra"));
   checkNextTerm(fields, new Term(LuceneFields.L_CONTENT, "zimbra.com"));
   checkAtEnd(fields, LuceneFields.L_CONTENT);
  }
  try (TermFieldEnumeration fields = searcher.getIndexReader()
    .getTermsForField(LuceneFields.L_FIELD, "")) {
   checkNextTerm(fields, new Term(LuceneFields.L_FIELD, "email:test1@zimbra.com"));
   checkNextTerm(fields, new Term(LuceneFields.L_FIELD, "email:test2@zimbra.com"));
   checkAtEnd(fields, LuceneFields.L_FIELD);
  }
  try (TermFieldEnumeration fields = searcher.getIndexReader()
    .getTermsForField(LuceneFields.L_PARTNAME, "")) {
   checkNextTerm(fields, new Term(LuceneFields.L_PARTNAME, "CONTACT"));
   checkAtEnd(fields, LuceneFields.L_PARTNAME);
  }
  try (TermFieldEnumeration fields = searcher.getIndexReader()
    .getTermsForField(LuceneFields.L_H_TO, "")) {
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "@zimbra"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "@zimbra.com"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "test1"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "test1@zimbra.com"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "test2"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "test2@zimbra.com"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "zimbra"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "zimbra.com"));
   checkAtEnd(fields, LuceneFields.L_H_TO);
  }
  try (TermFieldEnumeration fields = searcher.getIndexReader()
    .getTermsForField(LuceneFields.L_H_TO, "tess")) {
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "test1"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "test1@zimbra.com"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "test2"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "test2@zimbra.com"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "zimbra"));
   checkNextTerm(fields, new Term(LuceneFields.L_H_TO, "zimbra.com"));
   checkAtEnd(fields, LuceneFields.L_H_TO + "(sublist)");
  }
  try (TermFieldEnumeration fields = searcher.getIndexReader()
    .getTermsForField(LuceneFields.L_SORT_DATE, "")) {
   checkNextTermFieldType(fields, LuceneFields.L_SORT_DATE);
   // TODO:  ElasticSearch has more.  Not sure why and not sure it matters
   // checkAtEnd(fields, LuceneFields.L_SORT_DATE);
  }
  try (TermFieldEnumeration fields = searcher.getIndexReader()
    .getTermsForField(LuceneFields.L_MAILBOX_BLOB_ID, "")) {
   checkNextTermFieldType(fields, LuceneFields.L_MAILBOX_BLOB_ID);
   checkNextTermFieldType(fields, LuceneFields.L_MAILBOX_BLOB_ID);
   // TODO:  ElasticSearch has more.  Investigate?  Believe it relates to fact that is a number field
   // Numbers have an associated precision step (number of terms generated for each number value)
   // which defaults to 4.
   // checkAtEnd(fields, LuceneFields.L_MAILBOX_BLOB_ID);
  }
  searcher.close();
 }

    private Contact createContact(Mailbox mbox, String email)
            throws ServiceException {
        Folder folder = mbox.getFolderById(null, Mailbox.ID_FOLDER_CONTACTS);
        return mbox.createContact(null, new ParsedContact(
                Collections.singletonMap(ContactConstants.A_email, email)), folder.getId(), null);
    }

    private Contact createContact(Mailbox mbox, String firstName, String lastName, String email)
            throws ServiceException {
        Folder folder = mbox.getFolderById(null, Mailbox.ID_FOLDER_CONTACTS);
        Map<String, Object> fields;
        fields = ImmutableMap.<String, Object>of(
                ContactConstants.A_firstName, firstName,
                ContactConstants.A_lastName, lastName,
                ContactConstants.A_email, email);
        return mbox.createContact(null, new ParsedContact(fields), folder.getId(), null);
    }

    private Contact createContact(Mailbox mbox, String firstName, String lastName, String email, String jobTitle)
            throws ServiceException {
        Folder folder = mbox.getFolderById(null, Mailbox.ID_FOLDER_CONTACTS);
        Map<String, Object> fields;
        fields = ImmutableMap.<String, Object>of(
                ContactConstants.A_firstName, firstName,
                ContactConstants.A_lastName, lastName,
                ContactConstants.A_jobTitle, jobTitle,
                ContactConstants.A_email, email);
        return mbox.createContact(null, new ParsedContact(fields), folder.getId(), null);
    }

    private static String getBlobIdForResultDoc(ZimbraIndexSearcher searcher, ZimbraTopDocs result, int index)
            throws IOException {
        return searcher.doc(result.getScoreDoc(index).getDocumentID()).get(LuceneFields.L_MAILBOX_BLOB_ID);
    }

}
