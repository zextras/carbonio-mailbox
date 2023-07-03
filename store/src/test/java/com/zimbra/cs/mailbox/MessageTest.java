// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.google.common.io.ByteStreams;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.db.DbUtil;
import com.zimbra.cs.index.IndexDocument;
import com.zimbra.cs.index.LuceneFields;
import com.zimbra.cs.index.SearchParams;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.index.ZimbraQueryResults;
import com.zimbra.cs.mailbox.Flag.FlagInfo;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.mail.ToXML;
import com.zimbra.cs.service.util.ItemIdFormatter;
import qa.unittest.TestUtil;

/**
 * Unit test for {@link Message}.
 *
 * @author ysasaki
 */
public final class MessageTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();

        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void indexRawMimeMessage() throws Exception {
  Account account = Provisioning.getInstance().getAccountById(MockProvisioning.DEFAULT_ACCOUNT_ID);
  account.setPrefMailDefaultCharset("ISO-2022-JP");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  byte[] raw = ByteStreams.toByteArray(getClass().getResourceAsStream("raw-jis-msg.txt"));
  ParsedMessage pm = new ParsedMessage(raw, false);
  Message message = mbox.addMessage(null, pm, dopt, null);

  assertEquals("\u65e5\u672c\u8a9e", pm.getFragment(null));
  List<IndexDocument> docs = message.generateIndexData();
  assertEquals(2, docs.size());
  String subject = docs.get(0).toDocument().get(LuceneFields.L_H_SUBJECT);
  String body = docs.get(0).toDocument().get(LuceneFields.L_CONTENT);
  assertEquals("\u65e5\u672c\u8a9e", subject);
  assertEquals("\u65e5\u672c\u8a9e", body.trim());
 }

 @Test
 void getSortRecipients() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg1 = mbox.addMessage(null, new ParsedMessage(
    "From: from1@zimbra.com\r\nTo: to1@zimbra.com".getBytes(), false), opt, null);
  Message msg2 = mbox.addMessage(null, new ParsedMessage(
    "From: from2@zimbra.com\r\nTo: to2 <to2@zimbra.com>".getBytes(), false), opt, null);
  Message msg3 = mbox.addMessage(null, new ParsedMessage(
    "From: from3@zimbra.com\r\nTo: to3-1 <to3-1@zimbra.com>, to3-2 <to3-2@zimbra.com>".getBytes(),
    false), opt, null);

  assertEquals("to1@zimbra.com", msg1.getSortRecipients());
  assertEquals("to2", msg2.getSortRecipients());
  assertEquals("to3-1, to3-2", msg3.getSortRecipients());

  DbConnection conn = DbPool.getConnection(mbox);
  assertEquals("to1@zimbra.com", DbUtil.executeQuery(conn,
    "SELECT recipients FROM mboxgroup1.mail_item WHERE mailbox_id = ? AND id = ?",
    mbox.getId(), msg1.getId()).getString(1));
  assertEquals("to2", DbUtil.executeQuery(conn,
    "SELECT recipients FROM mboxgroup1.mail_item WHERE mailbox_id = ? AND id = ?",
    mbox.getId(), msg2.getId()).getString(1));
  assertEquals("to3-1, to3-2", DbUtil.executeQuery(conn,
    "SELECT recipients FROM mboxgroup1.mail_item WHERE mailbox_id = ? AND id = ?",
    mbox.getId(), msg3.getId()).getString(1));
  conn.closeQuietly();
 }

 @Test
 @Disabled("Fix me. Assertions fails. Standard error: missing .platform")
 void moveOutOfSpam() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  mbox.getAccount().setJunkMessagesIndexingEnabled(false);
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_SPAM);
  Message msg = mbox.addMessage(null, new ParsedMessage(
    "From: spammer@zimbra.com\r\nTo: test@zimbra.com".getBytes(), false), opt, null);
  MailboxTestUtil.index(mbox);

  SearchParams params = new SearchParams();
  params.setSortBy(SortBy.NONE);
  params.setTypes(EnumSet.of(MailItem.Type.MESSAGE));
  params.setQueryString("from:spammer");
  ZimbraQueryResults result = mbox.index.search(SoapProtocol.Soap12, new OperationContext(mbox), params);
  assertFalse(result.hasNext());

  mbox.move(new OperationContext(mbox), msg.getId(), MailItem.Type.MESSAGE, Mailbox.ID_FOLDER_INBOX);
  MailboxTestUtil.index(mbox);

  result = mbox.index.search(SoapProtocol.Soap12, new OperationContext(mbox), params);
  assertTrue(result.hasNext());
  assertEquals(msg.getId(), result.getNext().getItemId());
 }

 @Test
 void post() throws Exception {
  // Create post.
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  opt.setFlags(FlagInfo.POST.toBitmask());
  Message msg = mbox.addMessage(null, new ParsedMessage(
    "From: test@zimbra.com\r\nTo: test@zimbra.com".getBytes(), false), opt, null);

  // Validate flag.
  assertTrue((msg.getFlagBitmask() & Flag.FlagInfo.POST.toBitmask()) != 0);

  // Search by flag.
  List<Integer> ids = TestUtil.search(mbox, "tag:\\post", MailItem.Type.MESSAGE);
  assertEquals(1, ids.size());
  assertEquals(msg.getId(), ids.get(0).intValue());

  // Make sure that the post flag is serialized to XML.
  Element eMsg = ToXML.encodeMessageAsMIME(new XMLElement("test"), new ItemIdFormatter(), (OperationContext) null,
    msg, (String) null /* part */, false /* mustInline */, false /* mustNotInline */,
    false /* serializeType */, ToXML.NOTIFY_FIELDS);

  assertEquals("^", eMsg.getAttribute(MailConstants.A_FLAGS));

  // Try unsetting the post flag.
  mbox.setTags(null, msg.getId(), MailItem.Type.MESSAGE, 0, null);
  msg = mbox.getMessageById(null, msg.getId());
  // make sure post flag is still set
  assertTrue((msg.getFlagBitmask() & Flag.FlagInfo.POST.toBitmask()) != 0, "POST flag set");
  assertEquals(msg.getIdInMailbox(), msg.getImapUid(), "IMAP UID should be same as ID");
 }

 @Test
 void msgToPost() throws Exception {
  // Create msg.
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg = mbox.addMessage(null, new ParsedMessage(
    "From: test@zimbra.com\r\nTo: test@zimbra.com".getBytes(), false), opt, null);
  // try setting the post flag
  mbox.setTags(null, msg.getId(), MailItem.Type.MESSAGE, FlagInfo.POST.toBitmask(), null);
  msg = mbox.getMessageById(null, msg.getId());
  // make sure post flag is not set
  assertEquals((msg.getFlagBitmask() & Flag.FlagInfo.POST.toBitmask()), 0);
 }
}
