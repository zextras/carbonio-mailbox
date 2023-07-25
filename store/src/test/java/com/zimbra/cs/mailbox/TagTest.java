// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.common.collect.Maps;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Sets;
import com.zimbra.common.account.Key;
import com.zimbra.common.mailbox.BaseItemInfo;
import com.zimbra.common.mailbox.Color;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbTagTestUtil;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import com.zimbra.cs.mailbox.MailboxTest.MockListener;
import com.zimbra.cs.mailbox.util.TagUtil;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.session.PendingModifications.Change;

public class TagTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();

        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", Maps.<String, Object>newHashMap());

        Map<String, Object> attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        prov.createAccount("test2@zimbra.com", "secret", attrs);
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

    private static void checkName(String description, String input, String expectedOutput) {
        try {
            String sanitized = Tag.validateItemName(input);
            if (expectedOutput == null) {
                fail(description);
            } else {
                assertEquals(expectedOutput, sanitized, description);
            }
        } catch (ServiceException e) {
            if (expectedOutput == null) {
                assertEquals(MailServiceException.INVALID_NAME, e.getCode(), description);
            } else {
                fail(description);
            }
        }
    }

 @Test
 void name() throws Exception {
  checkName("null tag name", null, null);
  checkName("empty tag name", "", null);
  checkName("whitespace tag name", "   \t  \r\n", null);
  checkName("valid tag name", "xyz", "xyz");
  checkName("valid tag name with symbols", "\"xyz\" -- foo!", "\"xyz\" -- foo!");
  checkName("valid tag name: only symbols", "!@#$%^&*()`~-_=+[{]}|;\"',<.>/?", "!@#$%^&*()`~-_=+[{]}|;\"',<.>/?");
  checkName("trim leading whitespace", "   foo", "foo");
  checkName("trim trailing whitespace", "foo   ", "foo");
  checkName("trim leading/trailing whitespace", "   foo   ", "foo");
  checkName("convert whitespace", "foo\tbar\nbaz", "foo bar baz");
  checkName("invalid tag name (':')", "foo:bar", null);
  checkName("invalid tag name ('\\')", "foo\\bar", null);
  checkName("invalid tag name (control)", "foo\u0004bar", null);
  // Note: ZWC currently disallows creation of tags containing double quotes but the server allows them
  checkName("contains spaces and double quote", "Andrew \"Barney\"  Rubble", "Andrew \"Barney\"  Rubble");
 }

    private static final String tag1 = "foo", tag2 = "bar", tag3 = "baz", tag4 = "qux";

 @Test
 void rename() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  mbox.createTag(null, tag1, MailItem.DEFAULT_COLOR);
  try {
   mbox.createTag(null, tag1, MailItem.DEFAULT_COLOR);
   fail("failed to detect naming conflict when creating tag");
  } catch (MailServiceException e) {
   assertEquals(MailServiceException.ALREADY_EXISTS, e.getCode(), "incorrect error code when creating tag");
  }

  Tag tag = mbox.createTag(null, tag2, MailItem.DEFAULT_COLOR);
  int tagId = tag.getId();

  mbox.rename(null, tag.getId(), tag.getType(), tag3, -1);
  assertEquals(tag3, tag.getName(), "tag rename");
  mbox.purge(MailItem.Type.TAG);
  try {
   tag = mbox.getTagByName(null, tag3);
   assertEquals(tagId, tag.getId(), "fetching renamed tag");
  } catch (NoSuchItemException nsie) {
   fail("renamed tag could not be fetched");
  }

  try {
   mbox.rename(null, tag.getId(), tag.getType(), tag1, -1);
   fail("failed to detect naming conflict when renaming tag");
  } catch (MailServiceException e) {
   assertEquals(MailServiceException.ALREADY_EXISTS, e.getCode(), "incorrect error code when renaming tag");
  }
 }

 @Test
 void color() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  // color specified as byte
  Tag tag = mbox.createTag(null, tag1, (byte) 2);
  assertEquals(2, tag.getColor(), "tag color 2");

  mbox.purge(MailItem.Type.TAG);
  tag = mbox.getTagByName(null, tag1);
  assertEquals(2, tag.getColor(), "tag color 2");
  DbTagTestUtil.debugConsistencyCheck(mbox);

  // color specified as rgb
  Color color = new Color(0x668822);
  mbox.setColor(null, new int[]{tag.getId()}, MailItem.Type.TAG, color);
  tag = mbox.getTagByName(null, tag1);
  assertEquals(color, tag.getRgbColor(), "tag color " + color);

  mbox.purge(MailItem.Type.TAG);
  tag = mbox.getTagByName(null, tag1);
  assertEquals(color, tag.getRgbColor(), "tag color " + color);
  DbTagTestUtil.debugConsistencyCheck(mbox);

  // color specified as default
  mbox.setColor(null, new int[]{tag.getId()}, MailItem.Type.TAG, MailItem.DEFAULT_COLOR_RGB);
  tag = mbox.getTagByName(null, tag1);
  assertEquals(MailItem.DEFAULT_COLOR, tag.getColor(), "default tag color");
  assertEquals(MailItem.DEFAULT_COLOR_RGB, tag.getRgbColor(), "default tag color");

  mbox.purge(MailItem.Type.TAG);
  tag = mbox.getTagByName(null, tag1);
  assertEquals(MailItem.DEFAULT_COLOR, tag.getColor(), "default tag color");
  assertEquals(MailItem.DEFAULT_COLOR_RGB, tag.getRgbColor(), "default tag color");
  DbTagTestUtil.debugConsistencyCheck(mbox);
 }

    private void checkInboxCounts(String msg, Mailbox mbox, int count, int unread, int deleted, int deletedUnread) throws Exception {
        // check folder counts against in-memory folder object
        Folder inbox = mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
        assertEquals(count, inbox.getSize(), msg + " (folder messages)");
        assertEquals(unread, inbox.getUnreadCount(), msg + " (folder unread)");
        assertEquals(deleted, inbox.getDeletedCount(), msg + " (folder deleted)");
        assertEquals(deletedUnread, inbox.getDeletedUnreadCount(), msg + " (folder deleted unread)");

        // then force a reload from DB to validate persisted data
        mbox.purge(MailItem.Type.FOLDER);
        inbox = mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
        assertEquals(count, inbox.getSize(), msg + " (folder messages)");
        assertEquals(unread, inbox.getUnreadCount(), msg + " (folder unread)");
        assertEquals(deleted, inbox.getDeletedCount(), msg + " (folder deleted)");
        assertEquals(deletedUnread, inbox.getDeletedUnreadCount(), msg + " (folder deleted unread)");
    }

    private void checkTagCounts(String msg, Mailbox mbox, String tagName, int count, int unread) throws Exception {
        try {
            Tag tag = mbox.getTagByName(null, tagName);
            assertEquals(count, tag.getSize(), msg + " (tag messages)");
            assertEquals(unread, tag.getUnreadCount(), msg + " (tag unread)");
        } catch (MailServiceException.NoSuchItemException nsie) {
            assertEquals(count, 0, msg + " (tag messages)");
            assertEquals(unread, 0, msg + " (tag unread)");
        }
    }

    private void doubleCheckTagCounts(String msg, Mailbox mbox, String tagName, int count, int unread) throws Exception {
        // check folder counts against in-memory tag object
        checkTagCounts(msg, mbox, tagName, count, unread);
        // then force a reload from DB to validate persisted data
        mbox.purge(MailItem.Type.TAG);
        checkTagCounts(msg, mbox, tagName, count, unread);
    }

 @Test
 void markRead() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  checkInboxCounts("empty folder", mbox, 0, 0, 0, 0);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD);
  int msgId = mbox.addMessage(null, ThreaderTest.getRootMessage(), dopt, null).getId();
  checkInboxCounts("added message", mbox, 1, 1, 0, 0);
  checkItemTags(mbox, msgId, Flag.BITMASK_UNREAD);

  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, Flag.FlagInfo.DELETED, true, null);
  checkInboxCounts("marked message \\Deleted", mbox, 1, 1, 1, 1);
  checkItemTags(mbox, msgId, Flag.BITMASK_UNREAD | Flag.BITMASK_DELETED);

  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, Flag.FlagInfo.UNREAD, false, null);
  checkInboxCounts("marked message read", mbox, 1, 0, 1, 0);
  checkItemTags(mbox, msgId, Flag.BITMASK_DELETED);

  Tag tag = mbox.createTag(null, tag1, (byte) 4);
  assertEquals(tag1, tag.getName(), "tag names match");
  doubleCheckTagCounts("created tag", mbox, tag1, 0, 0);
  checkItemTags(mbox, msgId, Flag.BITMASK_DELETED);

  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag1, true, null);
  checkInboxCounts("tagged message", mbox, 1, 0, 1, 0);
  doubleCheckTagCounts("tagged message", mbox, tag1, 0, 0);
  checkItemTags(mbox, msgId, Flag.BITMASK_DELETED, tag1);

  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, Flag.FlagInfo.UNREAD, true, null);
  checkInboxCounts("marked message unread", mbox, 1, 1, 1, 1);
  doubleCheckTagCounts("marked message unread", mbox, tag1, 0, 0);
  checkItemTags(mbox, msgId, Flag.BITMASK_UNREAD | Flag.BITMASK_DELETED, tag1);

  mbox.alterTag(null, tag.getId(), MailItem.Type.TAG, Flag.FlagInfo.UNREAD, false, null);
  checkInboxCounts("marked tag read", mbox, 1, 0, 1, 0);
  doubleCheckTagCounts("marked tag read", mbox, tag1, 0, 0);
  checkItemTags(mbox, msgId, Flag.BITMASK_DELETED, tag1);

  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, Flag.FlagInfo.UNREAD, true, null);
  checkInboxCounts("marked message unread", mbox, 1, 1, 1, 1);
  doubleCheckTagCounts("marked message unread", mbox, tag1, 0, 0);
  checkItemTags(mbox, msgId, Flag.BITMASK_UNREAD | Flag.BITMASK_DELETED, tag1);

  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, Flag.FlagInfo.DELETED, false, null);
  checkInboxCounts("unmarked message \\Deleted", mbox, 1, 1, 0, 0);
  doubleCheckTagCounts("unmarked message \\Deleted", mbox, tag1, 1, 1);
  checkItemTags(mbox, msgId, Flag.BITMASK_UNREAD, tag1);

  mbox.alterTag(null, tag.getId(), MailItem.Type.TAG, Flag.FlagInfo.UNREAD, false, null);
  checkInboxCounts("marked tag read", mbox, 1, 0, 0, 0);
  doubleCheckTagCounts("marked tag read", mbox, tag1, 1, 0);
  checkItemTags(mbox, msgId, 0, tag1);
 }

    private void checkItemTags(Mailbox mbox, int itemId, int expectedFlags, String... expectedTags) throws Exception {
        MailItem item = mbox.getItemById(null, itemId, MailItem.Type.UNKNOWN);
        assertEquals(expectedFlags, item.getFlagBitmask(), "flags match on item");
        assertTrue(TagUtil.tagsMatch(item.getTags(), expectedTags), "tags match on item: " + TagUtil.encodeTags(item.getTags()));

        mbox.purge(MailItem.Type.MESSAGE);

        item = mbox.getItemById(null, itemId, MailItem.Type.UNKNOWN);
        assertEquals(expectedFlags, item.getFlagBitmask(), "flags match on item");
        assertTrue(TagUtil.tagsMatch(item.getTags(), expectedTags), "tags match on item: " + TagUtil.encodeTags(item.getTags()));

        DbTagTestUtil.debugConsistencyCheck(mbox);
    }

 @Test
 void implicitCreate() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  // implicitly create two tags by including them in an addMessage() call
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD).setTags(new String[]{tag1, tag2});
  int msgId = mbox.addMessage(null, ThreaderTest.getRootMessage(), dopt, null).getId();
  checkItemTags(mbox, msgId, Flag.BITMASK_UNREAD, tag1, tag2);

  // implicitly create a third tag via alterTag()
  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag3, true, null);
  checkItemTags(mbox, msgId, Flag.BITMASK_UNREAD, tag1, tag2, tag3);

  // implicitly create a fourth by overriding item tags
  mbox.setTags(null, msgId, MailItem.Type.MESSAGE, MailItem.FLAG_UNCHANGED, new String[]{tag1, tag3, tag4}, null);
  checkItemTags(mbox, msgId, Flag.BITMASK_UNREAD, tag1, tag3, tag4);

  // removing a nonexistent tag should *not* do an implicit create
  String bad = "badbadbad";
  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, bad, false, null);
  try {
   mbox.getTagByName(null, bad);
   fail("removing nonexistent tag should not autocreate");
  } catch (NoSuchItemException nsie) {
  }

  DbTagTestUtil.debugConsistencyCheck(mbox);

  // validate counts on the tag objects
  checkTagCounts(tag1, mbox, tag1, 1, 1);
  checkTagCounts(tag2, mbox, tag2, 0, 0);
  checkTagCounts(tag3, mbox, tag3, 1, 1);
  checkTagCounts(tag4, mbox, tag4, 1, 1);

  // verify that the tags got persisted to the database
  mbox.purge(MailItem.Type.MESSAGE);
  checkItemTags(mbox, msgId, Flag.BITMASK_UNREAD, tag1, tag3, tag4);

  // re-fetch the tags from the database
  mbox.purge(MailItem.Type.TAG);
  checkTagCounts(tag1, mbox, tag1, 1, 1);
  checkTagCounts(tag2, mbox, tag2, 0, 0);
  checkTagCounts(tag3, mbox, tag3, 1, 1);
  checkTagCounts(tag4, mbox, tag4, 1, 1);
  try {
   mbox.getTagByName(null, bad);
   fail("removing nonexistent tag should not autocreate");
  } catch (NoSuchItemException nsie) {
  }
 }

    private void checkThreeTagCounts(String msg, Mailbox mbox, int count1, int unread1, int count2, int unread2, int count3, int unread3) throws Exception {
        checkTagCounts(msg + ": tag " + tag1, mbox, tag1, count1, unread1);
        checkTagCounts(msg + ": tag " + tag2, mbox, tag2, count2, unread2);
        checkTagCounts(msg + ": tag " + tag3, mbox, tag3, count3, unread3);

        mbox.purge(MailItem.Type.TAG);
        checkTagCounts(msg + ": tag " + tag1 + " [reloaded]", mbox, tag1, count1, unread1);
        checkTagCounts(msg + ": tag " + tag2 + " [reloaded]", mbox, tag2, count2, unread2);
        checkTagCounts(msg + ": tag " + tag3 + " [reloaded]", mbox, tag3, count3, unread3);

        DbTagTestUtil.debugConsistencyCheck(mbox);
    }

 @Test
 void itemDelete() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  // precreate some but not all of the tags
  mbox.createTag(null, tag2, (byte) 4);
  mbox.createTag(null, tag3, new Color(0x8800FF));
  DbTagTestUtil.debugConsistencyCheck(mbox);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD).setTags(new String[]{tag1, tag2});
  int msgId = mbox.addMessage(null, ThreaderTest.getRootMessage(), dopt, null).getId();
  checkThreeTagCounts("add an unread message", mbox, 1, 1, 1, 1, 0, 0);

  dopt.setFlags(0).setTags(new String[]{tag1, tag3});
  int msgId2 = mbox.addMessage(null, new ParsedMessage(ThreaderTest.getSecondMessage(), false), dopt, null).getId();
  checkItemTags(mbox, msgId2, 0, tag1, tag3);
  checkThreeTagCounts("add a read message", mbox, 2, 1, 1, 1, 1, 0);

  mbox.delete(null, msgId, MailItem.Type.MESSAGE);
  checkThreeTagCounts("delete the unread message explicitly", mbox, 1, 0, 0, 0, 1, 0);

  mbox.emptyFolder(null, Mailbox.ID_FOLDER_INBOX, true);
  checkThreeTagCounts("delete the read message by emptying its folder", mbox, 0, 0, 0, 0, 0, 0);

  dopt.setFlags(Flag.BITMASK_UNREAD | Flag.BITMASK_DELETED).setTags(new String[]{tag1, tag2});
  int msgId3 = mbox.addMessage(null, ThreaderTest.getRootMessage(), dopt, null).getId();
  checkItemTags(mbox, msgId3, Flag.BITMASK_UNREAD | Flag.BITMASK_DELETED, tag1, tag2);
  checkThreeTagCounts("add an unread \\Deleted message", mbox, 0, 0, 0, 0, 0, 0);

  dopt.setFlags(Flag.BITMASK_UNREAD).setTags(new String[]{tag1, tag3});
  int msgId4 = mbox.addMessage(null, new ParsedMessage(ThreaderTest.getSecondMessage(), false), dopt, null).getId();
  checkItemTags(mbox, msgId4, Flag.BITMASK_UNREAD, tag1, tag3);
  checkThreeTagCounts("add an unread non-\\Deleted message", mbox, 1, 1, 0, 0, 1, 1);

  mbox.delete(null, msgId3, MailItem.Type.MESSAGE);
  checkThreeTagCounts("delete the unread \\Deleted message explicitly", mbox, 1, 1, 0, 0, 1, 1);

  mbox.alterTag(null, msgId4, MailItem.Type.MESSAGE, Flag.FlagInfo.DELETED, true, null);
  checkItemTags(mbox, msgId4, Flag.BITMASK_UNREAD | Flag.BITMASK_DELETED, tag1, tag3);
  checkThreeTagCounts("mark the remaining message as \\Deleted", mbox, 0, 0, 0, 0, 0, 0);

  mbox.emptyFolder(null, Mailbox.ID_FOLDER_INBOX, true);
  checkThreeTagCounts("delete that remaining message by emptying its folder", mbox, 0, 0, 0, 0, 0, 0);

  Message msg5 = mbox.addMessage(null, ThreaderTest.getRootMessage(), dopt, null);
  checkThreeTagCounts("add the conversation root", mbox, 1, 1, 0, 0, 1, 1);

  dopt.setConversationId(msg5.getConversationId()).setFlags(0);
  Message msg6 = mbox.addMessage(null, new ParsedMessage(ThreaderTest.getSecondMessage(), false), dopt, null);
  checkThreeTagCounts("add the conversation reply", mbox, 2, 1, 0, 0, 2, 1);

  mbox.setTags(null, msg6.getId(), MailItem.Type.MESSAGE, Flag.BITMASK_UNREAD, new String[]{tag2, tag3});
  checkThreeTagCounts("retag reply and mark unread", mbox, 1, 1, 1, 1, 2, 2);

  mbox.alterTag(null, msg5.getId(), MailItem.Type.MESSAGE, Flag.FlagInfo.UNREAD, false, null);
  checkThreeTagCounts("mark root read", mbox, 1, 0, 1, 1, 2, 1);

  mbox.delete(null, msg6.getConversationId(), MailItem.Type.CONVERSATION);
  checkThreeTagCounts("delete the entire conversation", mbox, 0, 0, 0, 0, 0, 0);
 }

 @Test
 void folder() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  mbox.alterTag(null, Mailbox.ID_FOLDER_INBOX, MailItem.Type.FOLDER, Flag.FlagInfo.SUBSCRIBED, true, null);
  mbox.alterTag(null, Mailbox.ID_FOLDER_INBOX, MailItem.Type.FOLDER, Flag.FlagInfo.SUBSCRIBED, false, null);

  try {
   mbox.alterTag(null, Mailbox.ID_FOLDER_INBOX, MailItem.Type.FOLDER, Flag.FlagInfo.FORWARDED, false, null);
   fail("failed to error on invalid flag on folder");
  } catch (MailServiceException e) {
   assertEquals(MailServiceException.CANNOT_TAG, e.getCode(), "incorrect error code when tagging folder");
  }
 }

    private void checkItemTags(Mailbox mbox, int itemId, String[] expectedTags) throws Exception {
        String[] tags = mbox.getMessageById(null, itemId).getTags();
        assertEquals(expectedTags.length, tags.length, "number of tags on item");
        for (int i = 0; i < expectedTags.length; i++) {
            assertEquals(expectedTags[i], tags[i], "item tag #" + i);
        }

        mbox.purge(MailItem.Type.MESSAGE);

        tags = mbox.getMessageById(null, itemId).getTags();
        assertEquals(expectedTags.length, tags.length, "number of tags on item");
        for (int i = 0; i < expectedTags.length; i++) {
            assertEquals(expectedTags[i], tags[i], "item tag #" + i);
        }
    }

 @Test
 void alterTag() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD).setTags(new String[]{tag1});
  int msgId = mbox.addMessage(null, ThreaderTest.getRootMessage(), dopt, null).getId();
  checkThreeTagCounts("add an unread message", mbox, 1, 1, 0, 0, 0, 0);

  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag2, true, null);
  checkThreeTagCounts("add a second tag", mbox, 1, 1, 1, 1, 0, 0);

  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag1, false, null);
  checkThreeTagCounts("remove the first tag", mbox, 0, 0, 1, 1, 0, 0);

  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag1, false, null);
  checkThreeTagCounts("duplicate remove the first tag", mbox, 0, 0, 1, 1, 0, 0);

  mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag1, true, null);
  checkThreeTagCounts("add the first tag back", mbox, 1, 1, 1, 1, 0, 0);
  checkItemTags(mbox, msgId, new String[]{tag2, tag1});
 }

 @Test
 void permissions() throws Exception {
  Account acct2 = Provisioning.getInstance().get(Key.AccountBy.name, "test2@zimbra.com");
  OperationContext octxt2 = new OperationContext(acct2);

  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  int tagId1 = mbox.createTag(null, tag1, (byte) 0).getId();

  // need full perms on account to fetch a tag by ID
  try {
   mbox.getTagById(octxt2, tagId1);
   fail("fetched tag by ID without permissions");
  } catch (ServiceException e) {
   assertEquals(ServiceException.PERM_DENIED, e.getCode(), "unexpected error when fetching tag by ID");
  }

  // need full perms on account to fetch a tag by name
  try {
   mbox.getTagByName(octxt2, tag1);
   fail("fetched tag by name without permissions");
  } catch (ServiceException e) {
   assertEquals(ServiceException.PERM_DENIED, e.getCode(), "unexpected error when fetching tag by name");
  }

  // need full perms on account to get the tag list
  try {
   mbox.getTagList(octxt2);
   fail("fetched tag list without permissions");
  } catch (ServiceException e) {
   assertEquals(ServiceException.PERM_DENIED, e.getCode(), "unexpected error when fetching tag list");
  }

  // need full perms on account to create a tag in the tag list
  try {
   mbox.createTag(octxt2, tag2, (byte) 0);
   fail("created tag without permissions");
  } catch (ServiceException e) {
   assertEquals(ServiceException.PERM_DENIED, e.getCode(), "unexpected error when creating tag");
  }

  // just need insert or write to implicitly create a tag
  mbox.grantAccess(null, Mailbox.ID_FOLDER_INBOX, acct2.getId(), ACL.GRANTEE_USER, ACL.RIGHT_INSERT, null);
  int msgid = -1;
  try {
   DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD).setTags(new String[]{tag2});
   msgid = mbox.addMessage(octxt2, ThreaderTest.getRootMessage(), dopt, null).getId();
  } catch (ServiceException e) {
   fail("unable to insert message with implicit tag");
  }

  // similar rights are needed to tag an existing item
  mbox.grantAccess(null, Mailbox.ID_FOLDER_INBOX, acct2.getId(), ACL.GRANTEE_USER, ACL.RIGHT_WRITE, null);
  try {
   mbox.alterTag(octxt2, msgid, MailItem.Type.MESSAGE, tag3, true, null);
  } catch (ServiceException e) {
   fail("unable to tag existing message with implicit tag");
  }
  try {
   mbox.alterTag(octxt2, msgid, MailItem.Type.MESSAGE, tag1, true, null);
  } catch (ServiceException e) {
   fail("unable to tag existing message with existing tag");
  }

  // still need full perms to "create" an existing but unlisted tag
  try {
   mbox.createTag(octxt2, tag2, (byte) 0);
   fail("switched tag from unlisted to listed without permissions");
  } catch (ServiceException e) {
   assertEquals(ServiceException.PERM_DENIED, e.getCode(), "unexpected error when \"creating\" existing unlisted tag");
  }

  // need full perms to rename a tag
  try {
   mbox.rename(octxt2, tagId1, MailItem.Type.TAG, tag4);
   fail("renamed tag without permissions");
  } catch (ServiceException e) {
   assertEquals(ServiceException.PERM_DENIED, e.getCode(), "unexpected error when renaming tag");
  }

  // need full perms to delete a tag
  try {
   mbox.delete(octxt2, tagId1, MailItem.Type.TAG);
   fail("deleted tag without permissions");
  } catch (ServiceException e) {
   assertEquals(ServiceException.PERM_DENIED, e.getCode(), "unexpected error when deleting tag");
  }
 }

 @Test
 void listed() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  // create one tag explicitly
  mbox.createTag(null, tag1, (byte) 5);

  // create two more tags implicitly
  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD).setTags(new String[]{tag2, tag3});
  mbox.addMessage(null, ThreaderTest.getRootMessage(), dopt, null).getId();

  // make sure only the explicitly-created tag is listed
  List<Tag> tags = mbox.getTagList(null);
  assertEquals(1, tags.size(), "only 1 tag listed");
  assertEquals(tag1, tags.get(0).getName(), tag1 + " is listed");

  // purge the cache and double-check against the DB contents
  mbox.purge(MailItem.Type.TAG);
  mbox.getTagList(null);
  assertEquals(1, tags.size(), "only 1 tag still listed");
  assertEquals(tag1, tags.get(0).getName(), tag1 + " is still listed");

  // mark one of the implicit tags as listed
  mbox.createTag(null, tag3, (byte) 3);

  tags = mbox.getTagList(null);
  Set<String> expectedTagNames = Sets.newHashSet(tag1, tag3);
  assertEquals(2, tags.size(), "2 tags listed");
  for (Tag tag : tags) {
   assertNotNull(expectedTagNames.remove(tag.getName()), tag.getName() + " is listed");
  }

  // purge the cache and double-check against the DB contents
  mbox.purge(MailItem.Type.TAG);
  tags = mbox.getTagList(null);
  expectedTagNames = Sets.newHashSet(tag1, tag3);
  assertEquals(2, tags.size(), "2 tags listed");
  for (Tag tag : tags) {
   assertNotNull(expectedTagNames.remove(tag.getName()), tag.getName() + " is listed");
  }
 }

 @Test
 void notifications() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  MockListener ml = new MockListener();
  MailboxListener.register(ml);

  try {
   // new implicit tags should not be included in notifications
   DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD).setTags(new String[]{tag2});
   mbox.addMessage(null, ThreaderTest.getRootMessage(), dopt, null);
   for (BaseItemInfo item : ml.pms.created.values()) {
    assertFalse(item instanceof Tag, "implicit tags should not be notified");
   }

   ml.clear();
   // new real tags *should* be included in notifications
   mbox.createTag(null, tag1, (byte) 0);
   assertFalse(ml.pms.created.isEmpty(), "explicit tag create must produce notifications");
   assertTrue(ml.pms.created.values().iterator().next() instanceof Tag, "explicit tags must be notified");

   ml.clear();
   // changes to implicit tags should not be included in notifications
   int msgId = mbox.addMessage(null, ThreaderTest.getRootMessage(), dopt, null).getId();
   for (Change chg : ml.pms.modified.values()) {
    assertFalse(chg.what instanceof Tag, "implicit tag changes should not be notified");
   }

   ml.clear();
   // changes to real tags *should* be included in notifications
   mbox.alterTag(null, msgId, MailItem.Type.MESSAGE, tag1, true, null);
   assertFalse(ml.pms.modified == null || ml.pms.modified.isEmpty(), "explicit tag apply must produce notifications");
   boolean found = false;
   for (Change chg : ml.pms.modified.values()) {
    found |= chg.what instanceof Tag;
   }
   assertTrue(found, "explicit tag apply must be notified");
  } finally {
   MailboxListener.unregister(ml);
  }
 }

 @Test
 void lowercase() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  mbox.createTag(null, "foo", (byte) 5);
  try {
   mbox.getTagByName(null, "FOO");
  } catch (ServiceException e) {
   fail("could not find differently-cased tag");
  }

  mbox.createTag(null, "Foo2", (byte) 3);
  try {
   mbox.getTagByName(null, "foo2");
  } catch (ServiceException e) {
   fail("could not find differently-cased tag");
  }
 }
}
