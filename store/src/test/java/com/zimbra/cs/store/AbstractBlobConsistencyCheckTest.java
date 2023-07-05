// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.common.service.ServiceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.store.file.BlobConsistencyChecker;
import com.zimbra.cs.store.file.BlobConsistencyChecker.BlobInfo;
import com.zimbra.cs.store.file.BlobConsistencyChecker.Results;

public abstract class AbstractBlobConsistencyCheckTest {

    static StoreManager originalStoreManager;
    protected final Log log = ZimbraLog.store;

    protected abstract StoreManager getStoreManager();
    protected abstract BlobConsistencyChecker getChecker();
    protected abstract Collection<Short> getVolumeIds();
    protected abstract void deleteAllBlobs() throws ServiceException, IOException;
    protected abstract void appendText(MailboxBlob blob, String text) throws IOException;
    protected abstract String createUnexpectedBlob(int index) throws ServiceException, IOException;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        MailboxTestUtil.initProvisioning();
        Provisioning.getInstance().createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
        //don't fail test even if native libraries not installed
        //this makes it easier to run unit tests from command line
        System.setProperty("zimbra.native.required", "false");
    }

    @BeforeEach
    public void setUp() throws Exception {
        originalStoreManager = StoreManager.getInstance();
        StoreManager.setInstance(getStoreManager());
        StoreManager.getInstance().startup();
        MailboxTestUtil.clearData();
        deleteAllBlobs();
    }


 @Test
 void singleBlob() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  mbox.addMessage(null, new ParsedMessage("From: test1-1@sub1.zimbra.com".getBytes(), false), dopt, null);

  BlobConsistencyChecker checker = getChecker();
  Results results = checker.check(getVolumeIds(), mbox.getId(), true, false);
  assertEquals(0, results.unexpectedBlobs.size());
  assertEquals(0, results.missingBlobs.size());
  assertEquals(0, results.usedBlobs.size());
  assertEquals(0, results.incorrectSize.size());
  assertEquals(0, results.incorrectModContent.size());
 }

 @Test
 void missingBlobs() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  int msgs = 10;
  for (int i = 0; i < msgs; i++) {
   mbox.addMessage(null, new ParsedMessage("From: test1-1@sub1.zimbra.com".getBytes(), false), dopt, null);
  }

  deleteAllBlobs();

  BlobConsistencyChecker checker = getChecker();
  Results results = checker.check(getVolumeIds(), mbox.getId(), true, false);

  assertEquals(msgs, results.missingBlobs.size());

  assertEquals(0, results.unexpectedBlobs.size());
  assertEquals(0, results.usedBlobs.size());
  assertEquals(0, results.incorrectSize.size());
  assertEquals(0, results.incorrectModContent.size());
 }

 @Test
 void unexpectedBlobs() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  String path = createUnexpectedBlob(0);

  BlobConsistencyChecker checker = getChecker();
  Results results = checker.check(getVolumeIds(), mbox.getId(), true, false);

  assertEquals(0, results.missingBlobs.size());

  assertEquals(1, results.unexpectedBlobs.size());
  BlobInfo info = results.unexpectedBlobs.values().iterator().next();
  assertEquals(path, info.path);

  assertEquals(0, results.usedBlobs.size());
  assertEquals(0, results.incorrectSize.size());
  assertEquals(0, results.incorrectModContent.size());

  deleteAllBlobs();

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  mbox.addMessage(null, new ParsedMessage("From: test1-1@sub1.zimbra.com".getBytes(), false), dopt, null);


  int msgs = 10;
  for (int i = 0; i < msgs; i++) {
   createUnexpectedBlob(i);
  }

  results = checker.check(getVolumeIds(), mbox.getId(), true, false);

  assertEquals(0, results.missingBlobs.size());

  assertEquals(msgs, results.unexpectedBlobs.size());

  assertEquals(0, results.usedBlobs.size());
  assertEquals(0, results.incorrectSize.size());
  assertEquals(0, results.incorrectModContent.size());
 }

 @Test
 void wrongSize() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg = mbox.addMessage(null, new ParsedMessage("From: test1-1@sub1.zimbra.com".getBytes(), false), dopt, null);

  MailboxBlob blob = msg.getBlob();
  String text = "some garbage";
  appendText(blob, text);

  BlobConsistencyChecker checker = getChecker();
  Results results = checker.check(getVolumeIds(), mbox.getId(), true, false);

  assertEquals(0, results.missingBlobs.size());
  assertEquals(0, results.unexpectedBlobs.size());
  assertEquals(0, results.usedBlobs.size());

  assertEquals(1, results.incorrectSize.size());
  BlobInfo info = results.incorrectSize.values().iterator().next();
  assertEquals(blob.size + text.length(), (long) info.fileDataSize);

  assertEquals(0, results.incorrectModContent.size());
 }

 @Test
 void allBlobs() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
  int msgs = 10;
  for (int i = 0; i < msgs; i++) {
   mbox.addMessage(null, new ParsedMessage("From: test1-1@sub1.zimbra.com".getBytes(), false), dopt, null);
  }

  BlobConsistencyChecker checker = getChecker();
  Results results = checker.check(getVolumeIds(), mbox.getId(), true, true);

  assertEquals(0, results.missingBlobs.size());
  assertEquals(0, results.unexpectedBlobs.size());

  assertEquals(msgs, results.usedBlobs.size());

  assertEquals(0, results.incorrectSize.size());
  assertEquals(0, results.incorrectModContent.size());
 }

    @AfterEach
    public void tearDown() throws Exception {
        StoreManager.getInstance().shutdown();
        StoreManager.setInstance(originalStoreManager);
    }

}
