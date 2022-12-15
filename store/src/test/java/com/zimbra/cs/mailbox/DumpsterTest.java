// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mime.ParsedDocument;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.mime.ParsedMessageOptions;

public class DumpsterTest {

    private Mailbox mbox;
    private Folder folder;

    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret",
                new HashMap<String, Object>()).setDumpsterEnabled(true);
    }

    @Before
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
        mbox = MailboxManager.getInstance().getMailboxByAccountId(
                MockProvisioning.DEFAULT_ACCOUNT_ID);
        folder = mbox.createFolder(null, "/Briefcase/f",
                new Folder.FolderOptions()
                        .setDefaultView(MailItem.Type.DOCUMENT));
        createDocument("doc.txt", "This is a document");
    }

    @Test
    public void recoverItem() throws Exception {
        MailItem doc = createDocument("doc1.txt", "This is a document");
        // move to trash
        mbox.move(null, doc.mId, MailItem.Type.DOCUMENT,
                Mailbox.ID_FOLDER_TRASH);
        doc = mbox.getItemById(null, doc.mId, MailItem.Type.DOCUMENT);
        // delete the item from trash to move to dumpster
        mbox.delete(null, doc.mId, MailItem.Type.DOCUMENT);
        // recover
        List<MailItem> recovered = mbox.recover(null, new int[] { doc.mId },
                MailItem.Type.DOCUMENT, folder.getId());
        Assert.assertEquals(recovered.size(), 1);
    }

    // Verify we cannot change tags or flags on items in dumpster
    @Test
    public void flagDumpsterItem() throws Exception {
        MailItem doc = createDocument("doc2.txt", "This is a document");
        // hard delete to move to dumpster
        mbox.delete(null, doc.mId, MailItem.Type.DOCUMENT);
        doc = mbox.getItemById(null, doc.mId, MailItem.Type.DOCUMENT, true);
        boolean success = false;
        boolean immutableException = false;
        try {
            mbox.beginTransaction("alterTag", null);
            doc.alterTag(Flag.FlagInfo.FLAGGED.toFlag(mbox), true);
            success = true;
        } catch (MailServiceException e) {
            immutableException = MailServiceException.IMMUTABLE_OBJECT.equals(e
                    .getCode());
            if (!immutableException) {
                throw e;
            }
        } finally {
            mbox.endTransaction(success);
        }
        Assert.assertTrue("expected IMMUTABLE_OBJECT exception",
                immutableException);
    }

    @Test
    public void moveDumpsterDoc() throws Exception {
        MailItem doc = createDocument("doc2.txt", "This is a document");
        // hard delete to move to dumpster
        mbox.delete(null, doc.mId, MailItem.Type.DOCUMENT);
        doc = mbox.getItemById(null, doc.mId, MailItem.Type.DOCUMENT, true);
        boolean success = false;
        boolean immutableException = false;
        try {
            mbox.move(null, doc.mId, MailItem.Type.DOCUMENT,
                    Mailbox.ID_FOLDER_BRIEFCASE);
            success = true;
        } catch (MailServiceException e) {
            immutableException = MailServiceException.NO_SUCH_DOC.equals(e
                    .getCode());
            if (!immutableException) {
                throw e;
            }
        } finally {
            mbox.endTransaction(success);
        }
        Assert.assertTrue("expected NO_SUCH_DOC exception", immutableException);
    }

    @Test
    public void moveDumpsterEmail() throws Exception {
        DeliveryOptions opt = new DeliveryOptions();
        opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
        Message msg = mbox.addMessage(null, new ParsedMessage(
                "From: test@zimbra.com\r\nTo: test@zimbra.com".getBytes(),
                false), opt, null);

        mbox.delete(null, msg.mId, MailItem.Type.MESSAGE);
        msg = (Message) mbox.getItemById(null, msg.mId, MailItem.Type.MESSAGE,
                true);
        boolean success = false;
        boolean immutableException = false;
        try {
            mbox.move(null, msg.mId, MailItem.Type.MESSAGE,
                    Mailbox.ID_FOLDER_INBOX);
            success = true;
        } catch (MailServiceException e) {
            immutableException = MailServiceException.NO_SUCH_MSG.equals(e
                    .getCode());
            if (!immutableException) {
                throw e;
            }
        } finally {
            mbox.endTransaction(success);
        }

        Assert.assertTrue("expected NO_SUCH_DOC exception", immutableException);
    }

    @Test
    public void reanalyzeDumpsterItem() throws Exception {
        DeliveryOptions opt = new DeliveryOptions();
        opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
        Message msg = mbox.addMessage(null, new ParsedMessage(
                "From: test@zimbra.com\r\nTo: test@zimbra.com".getBytes(),
                false), opt, null);

        mbox.delete(null, msg.mId, MailItem.Type.MESSAGE);
        msg = (Message) mbox.getItemById(null, msg.mId, MailItem.Type.MESSAGE,
                true);
        try {
            ParsedMessage pm = null;
            mbox.lock.lock();
            try {
                // force the pm's received-date to be the correct one
                ParsedMessageOptions messageOptions = new ParsedMessageOptions()
                        .setContent(msg.getMimeMessage(false))
                        .setReceivedDate(msg.getDate())
                        .setAttachmentIndexing(
                                mbox.attachmentsIndexingEnabled())
                        .setSize(msg.getSize()).setDigest(msg.getDigest());
                pm = new ParsedMessage(messageOptions);
            } finally {
                mbox.lock.release();
            }

            pm.setDefaultCharset(mbox.getAccount().getPrefMailDefaultCharset());
            mbox.reanalyze(msg.mId, MailItem.Type.MESSAGE, pm, msg.getSize());
        } catch (MailServiceException e) {
            Assert.fail("should not be throwing an exception");
        }
    }

    @Test
    public void msgInConversationTest() throws Exception {
        DeliveryOptions dopt = new DeliveryOptions()
                .setFolderId(Mailbox.ID_FOLDER_INBOX);
        Message msg1 = mbox.addMessage(null,
                MailboxTestUtil.generateMessage("test subject"), dopt, null);
        int msgId = msg1.getId();
        Message msg2 = mbox.addMessage(null,
                MailboxTestUtil.generateMessage("Re: test subject"),
                dopt.setConversationId(-msgId), null);
        Message msg3 = mbox.addMessage(null,
                MailboxTestUtil.generateMessage("Fwd: test subject"),
                dopt.setConversationId(-msgId), null);

        // make sure they're all grouped in a single conversation
        int convId = msg3.getConversationId();
        Assert.assertEquals("3 messages in conv", 3,
                mbox.getConversationById(null, convId).getSize());
        mbox.move(null, convId, MailItem.Type.CONVERSATION,
                Mailbox.ID_FOLDER_TRASH);
        mbox.delete(null, convId, MailItem.Type.CONVERSATION);
        msg3 = (Message) mbox.getItemById(null, msg3.mId,
                MailItem.Type.MESSAGE, true);
        boolean noSuchObjException = false;
        try {
            mbox.move(null, msg3.mId, MailItem.Type.MESSAGE,
                    Mailbox.ID_FOLDER_INBOX);
            Assert.fail("should not be able to move message from a conversation that is already in dumpster");
        } catch (MailServiceException e) {
            noSuchObjException = MailServiceException.NO_SUCH_MSG.equals(e
                    .getCode());
            if (!noSuchObjException) {
                throw e;
            }
        }

        Assert.assertTrue("expected NO_SUCH_MSG exception", noSuchObjException);
    }

    @Test
    public void conversationTest() throws Exception {
        DeliveryOptions dopt = new DeliveryOptions()
                .setFolderId(Mailbox.ID_FOLDER_INBOX);
        Message msg1 = mbox.addMessage(null,
                MailboxTestUtil.generateMessage("test subject"), dopt, null);
        int msgId = msg1.getId();
        mbox.addMessage(null,
                MailboxTestUtil.generateMessage("Re: test subject"),
                dopt.setConversationId(-msgId), null);
        Message msg3 = mbox.addMessage(null,
                MailboxTestUtil.generateMessage("Fwd: test subject"),
                dopt.setConversationId(-msgId), null);

        // make sure they're all grouped in a single conversation
        int convId = msg3.getConversationId();
        Assert.assertEquals("3 messages in conv", 3,
                mbox.getConversationById(null, convId).getSize());
        mbox.move(null, convId, MailItem.Type.CONVERSATION,
                Mailbox.ID_FOLDER_TRASH);
        mbox.delete(null, convId, MailItem.Type.CONVERSATION);
        boolean noSuchObjException = false;
        try {
            mbox.move(null, convId, MailItem.Type.CONVERSATION,
                    Mailbox.ID_FOLDER_INBOX);
            Assert.fail("should not be able to move a conversation that is already in dumpster");
        } catch (MailServiceException e) {
            noSuchObjException = MailServiceException.NO_SUCH_CONV.equals(e
                    .getCode());
            if (!noSuchObjException) {
                throw e;
            }
        }

        Assert.assertTrue("expected NO_SUCH_CONV exception", noSuchObjException);
    }

    @Test
    public void folderTest() throws Exception {
        Folder.FolderOptions fopt = new Folder.FolderOptions()
                .setDefaultView(MailItem.Type.DOCUMENT);
        Folder rootSource = mbox
                .createFolder(null, "/DumpsterTestSource", fopt);
        Folder subFolder1 = mbox.createFolder(null,
                "/DumpsterTestSource/test1", fopt);

        DeliveryOptions dopt = new DeliveryOptions()
                .setFolderId(subFolder1.mId);
        Message msg = mbox.addMessage(null,
                MailboxTestUtil.generateMessage("test subject"), dopt, null);
        try {
            mbox.getFolderByPath(null, "/DumpsterTestSource");
            mbox.getFolderByPath(null, "/DumpsterTestSource/test1");
        } catch (Exception e) {
            Assert.fail();
        }
        boolean noSuchObjException = false;
        // delete the root folder and make sure it and all the leaves are gone
        mbox.move(null, rootSource.mId, MailItem.Type.FOLDER,
                Mailbox.ID_FOLDER_TRASH);
        mbox.delete(null, subFolder1.mId, MailItem.Type.FOLDER);
        mbox.delete(null, rootSource.mId, MailItem.Type.FOLDER);
        try {
            msg = (Message) mbox.getItemById(null, msg.mId,
                    MailItem.Type.MESSAGE, true);
            Assert.assertNotNull("should find the message in dumpster", msg);
        } catch (Exception e) {
            Assert.fail("should find the message in dumpster");
        }
        try {
            mbox.move(null, msg.mId, MailItem.Type.MESSAGE,
                    Mailbox.ID_FOLDER_INBOX);
            Assert.fail("should throw NO_SUCH_MSG exception");
        } catch (MailServiceException e) {
            noSuchObjException = MailServiceException.NO_SUCH_MSG.equals(e
                    .getCode());
            if (!noSuchObjException) {
                throw e;
            }
        }
        Assert.assertTrue("expected NO_SUCH_MSG exception", noSuchObjException);
    }

    private MailItem createDocument(String name, String content)
            throws Exception {
        InputStream in = new ByteArrayInputStream(content.getBytes());
        ParsedDocument pd = new ParsedDocument(in, name, "text/plain",
                System.currentTimeMillis(), null, null);
        return mbox.createDocument(null, folder.getId(), pd,
                MailItem.Type.DOCUMENT, 0);
    }
}
