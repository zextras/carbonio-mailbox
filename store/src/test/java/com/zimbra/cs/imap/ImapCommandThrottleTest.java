// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.common.localconfig.LC;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.imap.AppendMessage.Part;
import com.zimbra.cs.imap.ImapHandler.StoreAction;
import com.zimbra.cs.imap.ImapSearch.AllSearch;
import com.zimbra.cs.imap.ImapSearch.AndOperation;
import com.zimbra.cs.imap.ImapSearch.FlagSearch;
import com.zimbra.cs.imap.ImapSearch.OrOperation;
import com.zimbra.cs.imap.ImapSearch.SequenceSearch;
import com.zimbra.cs.mailbox.MailboxTestUtil;

public class ImapCommandThrottleTest {
    private Account acct = null;
    ImapCredentials creds = null;
    private static final String LOCAL_USER = "localimaptest@zimbra.com";

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
        Provisioning prov = Provisioning.getInstance();
        HashMap<String,Object> attrs = new HashMap<String,Object>();
        attrs.put(Provisioning.A_zimbraId, "12aa345b-2b47-44e6-8cb8-7fdfa18c1a9f");
        acct = prov.createAccount(LOCAL_USER, "secret", attrs);
        acct.setFeatureAntispamEnabled(true);
        creds = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
    }

    @AfterEach
    public void tearDown() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void repeatCommand() {
  int limit = 25;
  ImapCommandThrottle throttle = new ImapCommandThrottle(limit);

  for (int i = 0; i < limit; i++) {
   MockImapCommand command = new MockImapCommand("p1", "p3", 123);
   assertFalse(throttle.isCommandThrottled(command));
  }
  MockImapCommand command = new MockImapCommand("p1", "p3", 123);
  assertTrue(throttle.isCommandThrottled(command));
 }

 @Test
 void repeatUnderLimit() {
  int limit = 55;
  ImapCommandThrottle throttle = new ImapCommandThrottle(limit);

  for (int i = 0; i < limit; i++) {
   MockImapCommand command = new MockImapCommand("p1", "p3", 123);
   assertFalse(throttle.isCommandThrottled(command));
  }
  MockImapCommand command = new MockImapCommand("p2", "p3", 1234);
  assertFalse(throttle.isCommandThrottled(command));
  command = new MockImapCommand("p1", "p3", 123);
  assertFalse(throttle.isCommandThrottled(command));
 }

    private QResyncInfo makeQri() {
        QResyncInfo qri = new QResyncInfo();
        qri.setKnownUIDs("knownUIDs");
        qri.setModseq(1);
        qri.setSeqMilestones("seqMilestones");
        qri.setUidMilestones("uidMilestones");
        qri.setUvv(123456);
        return qri;
    }

 @Test
 void select() {
  String pathName = "testfolder";

  SelectCommand select = new SelectCommand(new ImapPath(pathName, creds), (byte) 123, makeQri());

  assertTrue(select.isDuplicate(select), "same obj");

  SelectCommand select2 = new SelectCommand(new ImapPath(pathName, creds), (byte) 123, makeQri());
  assertTrue(select.isDuplicate(select2), "diff obj same fields");

  SelectCommand select3 = new SelectCommand(new ImapPath(pathName + "foo", creds), (byte) 123, makeQri());
  assertFalse(select.isDuplicate(select3), "different path");

  SelectCommand select4 = new SelectCommand(new ImapPath(pathName, creds), (byte) 101, makeQri());
  assertFalse(select.isDuplicate(select4), "different params");

  QResyncInfo qri = makeQri();
  qri.setKnownUIDs("foo");
  SelectCommand select5 = new SelectCommand(new ImapPath(pathName, creds), (byte) 123, qri);
  assertFalse(select.isDuplicate(select5), "different qri");
 }

 @Test
 void examine() {
  String pathName = "testfolder";

  ExamineCommand examine = new ExamineCommand(new ImapPath(pathName, creds), (byte) 123, makeQri());

  assertTrue(examine.isDuplicate(examine), "same obj");

  SelectCommand select = new SelectCommand(new ImapPath(pathName, creds), (byte) 123, makeQri());
  assertFalse(examine.isDuplicate(select), "select vs examine");

  ExamineCommand examine2 = new ExamineCommand(new ImapPath(pathName, creds), (byte) 123, makeQri());
  assertTrue(examine.isDuplicate(examine2), "diff obj same fields");

  ExamineCommand examine3 = new ExamineCommand(new ImapPath(pathName + "foo", creds), (byte) 123, makeQri());
  assertFalse(examine.isDuplicate(examine3), "different path");

  ExamineCommand examine4 = new ExamineCommand(new ImapPath(pathName, creds), (byte) 101, makeQri());
  assertFalse(examine.isDuplicate(examine4), "different params");

  QResyncInfo qri = makeQri();
  qri.setKnownUIDs("foo");
  ExamineCommand examine5 = new ExamineCommand(new ImapPath(pathName, creds), (byte) 123, qri);
  assertFalse(examine.isDuplicate(examine5), "different qri");
 }

    private List<ImapPartSpecifier> makeParts() {
        List<ImapPartSpecifier> parts = new ArrayList<ImapPartSpecifier>();
        parts.add(new ImapPartSpecifier("cmd1", "part1", "modifier1"));
        ImapPartSpecifier headerSpec = new ImapPartSpecifier("cmd2", null, null);
        List<String> headers = new ArrayList<String>();
        headers.add("h1");
        headers.add("h2");
        headerSpec.setHeaders(headers);
        parts.add(headerSpec);
        return parts;
    }

 @Test
 void fetch() {
  String sequence = "1:*";
  int attributes = 123;
  List<ImapPartSpecifier> parts = makeParts();
  FetchCommand fetch = new FetchCommand(sequence, attributes, parts);

  assertTrue(fetch.isDuplicate(fetch), "same obj");

  FetchCommand fetch2 = new FetchCommand(sequence, attributes, parts);
  assertTrue(fetch.isDuplicate(fetch2), "same args, different obj");

  FetchCommand fetch3 = new FetchCommand(sequence + "foo", attributes, parts);
  assertFalse(fetch.isDuplicate(fetch3), "different sequence");

  FetchCommand fetch4 = new FetchCommand(sequence, attributes + 1, parts);
  assertFalse(fetch.isDuplicate(fetch4), "different attributes");

  FetchCommand fetch5 = new FetchCommand(sequence, attributes, null);
  assertFalse(fetch.isDuplicate(fetch5), "null parts");

  List<ImapPartSpecifier> p2 = makeParts();
  p2.add(new ImapPartSpecifier("cmd3", "part1", "modifier1"));
  FetchCommand fetch6 = new FetchCommand(sequence, attributes, p2);
  assertFalse(fetch.isDuplicate(fetch6), "different length parts");

  List<ImapPartSpecifier> p3 = makeParts();
  p3.add(p3.remove(0));
  FetchCommand fetch7 = new FetchCommand(sequence, attributes, p3);
  assertTrue(fetch.isDuplicate(fetch7), "same parts; different order - should be a duplicate");

  List<ImapPartSpecifier> p4 = makeParts();
  ImapPartSpecifier headerPart = p4.get(1);
  List<String> headers = new ArrayList<String>();
  headers.add("h1");
  headers.add("h3");
  headerPart.setHeaders(headers);
  FetchCommand fetch8 = new FetchCommand(sequence, attributes, p4);
  assertFalse(fetch.isDuplicate(fetch8), "same lengths, different headers");

  List<ImapPartSpecifier> p5 = makeParts();
  p5.remove(0);
  ImapPartSpecifier newPart = new ImapPartSpecifier("cmd2", "part1", "modifier1");
  p5.add(newPart);
  FetchCommand fetch9 = new FetchCommand(sequence, attributes, p5);
  assertFalse(fetch.isDuplicate(fetch9), "different part.command, same length");

  List<ImapPartSpecifier> p6 = makeParts();
  p6.remove(0);
  newPart = new ImapPartSpecifier("cmd1", "part2", "modifier1");
  p6.add(newPart);
  FetchCommand fetch10 = new FetchCommand(sequence, attributes, p6);
  assertFalse(fetch.isDuplicate(fetch10), "different part.part, same length");

  List<ImapPartSpecifier> p7 = makeParts();
  p7.remove(0);
  newPart = new ImapPartSpecifier("cmd1", "part1", "modifier2");
  p7.add(newPart);
  FetchCommand fetch11 = new FetchCommand(sequence, attributes, p7);
  assertFalse(fetch.isDuplicate(fetch11), "different part.modifier, same length");
 }

 @Test
 void fetchBug68556() {
  ImapPartSpecifier part = new ImapPartSpecifier("BODY", "", "HEADER.FIELDS");
  List<String> headers = new ArrayList<String>();
  headers.add("CONTENT-CLASS");
  part.setHeaders(headers);
  assertTrue(part.isIgnoredExchangeHeader(), "Exchange header detected");

  List<ImapPartSpecifier> parts = new ArrayList<ImapPartSpecifier>();
  parts.add(part);

  ImapCommand command = new FetchCommand("1:123", ImapHandler.FETCH_FROM_CACHE, parts);

  assertFalse(command.throttle(null), "Fetch not throttled, just truncated parts");

  assertTrue(parts.isEmpty(), "CONTENT-CLASS removed");
 }

 @Test
 void copy() {
  String destFolder = "destFolder";
  String sequenceSet = "10:20";

  CopyCommand copy = new CopyCommand(sequenceSet, new ImapPath(destFolder, creds));

  assertTrue(copy.isDuplicate(copy), "same obj");

  CopyCommand copy2 = new CopyCommand(sequenceSet, new ImapPath(destFolder, creds));
  assertTrue(copy.isDuplicate(copy2), "diff obj same fields");

  CopyCommand copy3 = new CopyCommand(sequenceSet, new ImapPath(destFolder + "foo", creds));
  assertFalse(copy.isDuplicate(copy3), "diff dest path");

  CopyCommand copy4 = new CopyCommand("20:30", new ImapPath(destFolder + "foo", creds));
  assertFalse(copy.isDuplicate(copy4), "diff dest path");
 }

    private Part makeAppendPart(AppendMessage append, int size, byte fillByte) throws IOException {
        Literal literal = Literal.newInstance(size);
        byte[] bytes = new byte[size];
        Arrays.fill(bytes, fillByte);
        literal.put(bytes, 0, bytes.length);
        return append.new Part(literal);
    }

    private List<AppendMessage> makeAppends() throws IOException {
        List<AppendMessage> list = new ArrayList<AppendMessage>();
        List<String> flagNames = new ArrayList<String>();
        flagNames.add("F1");
        flagNames.add("F2");
        Date date = new Date(1234567890);
        List<Part> parts = new ArrayList<Part>();

        AppendMessage append = new AppendMessage(flagNames, date, parts);
        parts.add(makeAppendPart(append, 123, (byte) 99));

        List<String> flagNames2 = new ArrayList<String>();
        flagNames.add("F3");
        flagNames.add("F4");
        Date date2 = new Date(222222222);
        List<Part> parts2 = new ArrayList<Part>();
        AppendMessage append2 = new AppendMessage(flagNames2, date2, parts2);
        parts2.add(makeAppendPart(append2, 555, (byte) 55));
        parts2.add(makeAppendPart(append2, 444, (byte) 44));

        list.add(append);
        list.add(append2);
        return list;
    }

 @Test
 void append() throws Exception {
  String path = "testPath";
  AppendCommand append = new AppendCommand(new ImapPath(path, creds), makeAppends());

  assertTrue(append.isDuplicate(append), "same obj");

  AppendCommand append2 = new AppendCommand(new ImapPath(path, creds), makeAppends());
  assertTrue(append.isDuplicate(append2), "diff obj same params");

  AppendCommand append3 = new AppendCommand(new ImapPath(path + "foo", creds), makeAppends());
  assertFalse(append.isDuplicate(append3), "different path");

  List<AppendMessage> appends = makeAppends();
  appends.remove(0);
  AppendCommand append4 = new AppendCommand(new ImapPath(path, creds), appends);
  assertFalse(append.isDuplicate(append4), "different length appends");

  appends = makeAppends();
  AppendMessage appendMsg = appends.remove(0);
  List<Part> parts = new ArrayList<Part>();
  AppendMessage appendMsg2 = new AppendMessage(appendMsg.getPersistentFlagNames(), appendMsg.getDate(), parts);
  parts.add(makeAppendPart(appendMsg2, 215, (byte) 215));

  appends.add(0, appendMsg2);
  AppendCommand append5 = new AppendCommand(new ImapPath(path, creds), appends);
  assertFalse(append.isDuplicate(append5), "different append parts");

  parts = new ArrayList<Part>();
  appendMsg2 = new AppendMessage(appendMsg.getPersistentFlagNames(), new Date(), parts);
  parts.add(makeAppendPart(appendMsg2, 123, (byte) 99));
  appends.remove(0);
  appends.add(0, appendMsg2);
  AppendCommand append6 = new AppendCommand(new ImapPath(path, creds), appends);
  assertFalse(append.isDuplicate(append6), "different date");

  parts = new ArrayList<Part>();
  List<String> flagNames = new ArrayList<String>();
  flagNames.add("F1");
  flagNames.add("F3");
  appendMsg2 = new AppendMessage(flagNames, appendMsg.getDate(), parts);
  parts.add(makeAppendPart(appendMsg2, 123, (byte) 99));
  appends.remove(0);
  appends.add(0, appendMsg2);
  AppendCommand append7 = new AppendCommand(new ImapPath(path, creds), appends);
  assertFalse(append.isDuplicate(append7), "different flag names");
 }

 @Test
 void list() {
  String refName = "refName";
  Set<String> mailboxNames = new HashSet<String>(Arrays.asList(new String[]{"mbox1", "mbox2", "mbox3"}));
  byte selectOptions = (byte) 24;
  byte returnOptions = (byte) 38;
  byte status = (byte) 67;
  AbstractListCommand list = new ListCommand(refName, mailboxNames, selectOptions, returnOptions, status);

  assertTrue(list.isDuplicate(list), "same obj");

  AbstractListCommand list2 = new ListCommand(refName, mailboxNames, selectOptions, returnOptions, status);
  assertTrue(list.isDuplicate(list2), "same fields");

  list2 = new ListCommand(refName + "foo", mailboxNames, selectOptions, returnOptions, status);
  assertFalse(list.isDuplicate(list2), "different ref name");

  list2 = new ListCommand(refName, new HashSet<String>(Arrays.asList(new String[]{"mbox1", "mbox2"})),
    selectOptions, returnOptions, status);
  assertFalse(list.isDuplicate(list2), "different mailbox names");

  list2 = new ListCommand(refName, mailboxNames, (byte) 99, returnOptions, status);
  assertFalse(list.isDuplicate(list2), "different selectOptions");

  list2 = new ListCommand(refName, mailboxNames, selectOptions, (byte) 99, status);
  assertFalse(list.isDuplicate(list2), "different returnOptions");

  list2 = new ListCommand(refName, mailboxNames, selectOptions, returnOptions, (byte) 99);
  assertFalse(list.isDuplicate(list2), "different status");
 }

    private ImapSearch makeSearch(String flagName) {
        ImapSearch sequenceSearch = new SequenceSearch("tag", "subseq", true);
        ImapSearch flagSearch = new FlagSearch(flagName);
        ImapSearch andSearch = new AndOperation(sequenceSearch, flagSearch);

        ImapSearch allSearch = new AllSearch();
        ImapSearch orSearch = new OrOperation(andSearch, allSearch);
        return orSearch;
    }

 @Test
 void search() {
  String flagName = "flagName";

  SearchCommand search = new SearchCommand(makeSearch(flagName), 123);

  assertTrue(search.isDuplicate(search), "same obj");

  SearchCommand search2 = new SearchCommand(makeSearch(flagName), 123);
  assertTrue(search.isDuplicate(search2), "same fields");

  search2 = new SearchCommand(makeSearch(flagName), 456);
  assertFalse(search.isDuplicate(search2), "different options");

  search2 = new SearchCommand(makeSearch(flagName + "foo"), 456);
  assertFalse(search.isDuplicate(search2), "different search params");
 }

 @Test
 void sort() {
  String flagName = "flagName";

  SortCommand sort = new SortCommand(makeSearch(flagName), 123);

  assertTrue(sort.isDuplicate(sort), "same obj");

  SortCommand sort2 = new SortCommand(makeSearch(flagName), 123);
  assertTrue(sort.isDuplicate(sort2), "same fields");

  sort2 = new SortCommand(makeSearch(flagName), 456);
  assertFalse(sort.isDuplicate(sort2), "different options");

  sort2 = new SortCommand(makeSearch(flagName + "foo"), 456);
  assertFalse(sort.isDuplicate(sort2), "different search params");

  SearchCommand search = new SearchCommand(makeSearch(flagName), 123);
  assertFalse(sort.isDuplicate(search), "different class (search vs sort)");
 }

 @Test
 void create() {
  String pathName = "folder123";
  CreateCommand create = new CreateCommand(new ImapPath(pathName, creds));

  assertTrue(create.isDuplicate(create), "same obj");

  CreateCommand create2 = new CreateCommand(new ImapPath(pathName, creds));
  assertTrue(create.isDuplicate(create2), "same fields");

  create2 = new CreateCommand(new ImapPath("foo", creds));
  assertFalse(create.isDuplicate(create2), "different path");

  for (int repeats = 0; repeats < LC.imap_throttle_command_limit.intValue(); repeats++) {
   create2 = new CreateCommand(new ImapPath("foo" + repeats, creds));
   assertFalse(create2.throttle(create));
   create = create2;
  }
  assertTrue(create2.throttle(create));
 }

    private List<String> makeFlagNames() {
        List<String> list = new ArrayList<String>();
        list.add("F1");
        list.add("F2");
        list.add("F3");
        return list;
    }

 @Test
 void store() {
  String seqSet = "1:200";
  StoreCommand store = new StoreCommand(seqSet, makeFlagNames(), StoreAction.ADD, 0);

  assertTrue(store.isDuplicate(store), "same obj");

  StoreCommand store2 = new StoreCommand(seqSet, makeFlagNames(), StoreAction.ADD, 0);
  assertTrue(store.isDuplicate(store2), "same fields");

  store2 = new StoreCommand("1:400", makeFlagNames(), StoreAction.ADD, 0);
  assertFalse(store.isDuplicate(store2), "different sequence");

  List<String> flagNames = makeFlagNames();
  flagNames.remove(0);
  store2 = new StoreCommand(seqSet, flagNames, StoreAction.ADD, 0);
  assertFalse(store.isDuplicate(store2), "different flag names");

  store2 = new StoreCommand(seqSet, makeFlagNames(), StoreAction.REMOVE, 0);
  assertFalse(store.isDuplicate(store2), "different action");

  store2 = new StoreCommand(seqSet, makeFlagNames(), StoreAction.ADD, 999);
  assertFalse(store.isDuplicate(store2), "different mod seq");
 }
}
