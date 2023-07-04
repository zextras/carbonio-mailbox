// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.common.util.ByteUtil;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.ThreaderTest;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.store.external.ExternalStoreManager;
import qa.unittest.TestUtil;

public abstract class AbstractStoreManagerTest {

    static StoreManager originalStoreManager;

    @BeforeAll
    public static void init() throws Exception {
    System.setProperty("zimbra.config", "../store/src/test/resources/localconfig-test.xml");
        MailboxTestUtil.initServer();
        MailboxTestUtil.initProvisioning();
        Provisioning.getInstance().createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    /**
     * Return an instance of the StoreManager implementation being tested
     */
    protected abstract StoreManager getStoreManager();

    @BeforeEach
    public void setUp() throws Exception {
        originalStoreManager = StoreManager.getInstance();
        StoreManager.setInstance(getStoreManager());
        StoreManager.getInstance().startup();
    }

    @AfterEach
    public void tearDown() throws Exception {
        StoreManager.getInstance().shutdown();
        StoreManager.setInstance(originalStoreManager);
    }

 @Test
 void store() throws Exception {
  ParsedMessage pm = ThreaderTest.getRootMessage();
  byte[] mimeBytes = TestUtil.readInputStream(pm.getRawInputStream());

  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  StoreManager sm = StoreManager.getInstance();
  Blob blob = sm.storeIncoming(pm.getRawInputStream());

  assertEquals(pm.getRawData().length, blob.getRawSize(), "blob size = message size");
  assertTrue(TestUtil.bytesEqual(mimeBytes, blob.getInputStream()), "blob content = mime content");

  StagedBlob staged = sm.stage(blob, mbox);
  assertEquals(blob.getRawSize(), staged.getSize(), "staged size = blob size");

  MailboxBlob mblob = sm.link(staged, mbox, 0, 0);
  assertEquals(staged.getSize(), mblob.getSize(), "link size = staged size");
  assertTrue(TestUtil.bytesEqual(mimeBytes, mblob.getLocalBlob().getInputStream()), "link content = mime content");

  mblob = sm.getMailboxBlob(mbox, 0, 0, staged.getLocator());
  assertEquals(staged.getSize(), mblob.getSize(), "mblob size = staged size");
  assertTrue(TestUtil.bytesEqual(mimeBytes, mblob.getLocalBlob().getInputStream()), "mailboxblob content = mime content");

  InputStream stream = sm.getContent(mblob);
  assertTrue(TestUtil.bytesEqual(mimeBytes, stream), "stream content = mime content");

  sm.delete(mblob);

 }

 /**
  * Tests putting two copies of the same message into the store (bug 67969).
  */
 @Test
 void sameDigest() throws Exception {
  ParsedMessage pm = ThreaderTest.getRootMessage();
  StoreManager sm = StoreManager.getInstance();
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  Blob blob1 = sm.storeIncoming(pm.getRawInputStream());
  StagedBlob staged1 = sm.stage(blob1, mbox);
  MailboxBlob mblob1 = sm.link(staged1, mbox, 0, 0);

  Blob blob2 = sm.storeIncoming(pm.getRawInputStream());
  StagedBlob staged2 = sm.stage(blob2, mbox);
  MailboxBlob mblob2 = sm.link(staged2, mbox, 0, 0);

  mblob1.getLocalBlob();
  mblob2.getLocalBlob();
  sm.delete(mblob1);
  sm.delete(mblob2);
 }

 @Test
 void incoming() throws Exception {
  Random rand = new Random();
  byte[] bytes = new byte[1000000];
  rand.nextBytes(bytes);

  StoreManager sm = StoreManager.getInstance();
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  IncomingBlob incoming = sm.newIncomingBlob("foo", null);

  OutputStream out = incoming.getAppendingOutputStream();
  out.write(bytes);
  assertEquals(bytes.length, incoming.getCurrentSize());
  Blob blob = incoming.getBlob();

  assertEquals(bytes.length, blob.getRawSize(), "blob size = incoming written");

  assertTrue(TestUtil.bytesEqual(bytes, blob.getInputStream()), "blob content = mime content");

  StagedBlob staged = sm.stage(blob, mbox);
  assertEquals(blob.getRawSize(), staged.getSize(), "staged size = blob size");

  MailboxBlob mblob = sm.link(staged, mbox, 0, 0);
  assertEquals(staged.getSize(), mblob.getSize(), "link size = staged size");
  assertTrue(TestUtil.bytesEqual(bytes, mblob.getLocalBlob().getInputStream()), "link content = mime content");

  mblob = sm.getMailboxBlob(mbox, 0, 0, staged.getLocator());
  assertEquals(staged.getSize(), mblob.getSize(), "mblob size = staged size");
  assertTrue(TestUtil.bytesEqual(bytes, mblob.getLocalBlob().getInputStream()), "mailboxblob content = mime content");

  sm.delete(mblob);
 }

 @Test
 void incomingMultipost() throws Exception {
  byte[] bytes = "AAAAStrinGBBB".getBytes();
  StoreManager sm = StoreManager.getInstance();
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  IncomingBlob incoming = sm.newIncomingBlob("foo", null);

  OutputStream out = incoming.getAppendingOutputStream();
  byte[] b1 = "AAAA".getBytes();
  byte[] b2 = "StrinG".getBytes();
  byte[] b3 = "BBB".getBytes();
  out.write(b1);
  int written = b1.length;
  assertEquals(written, incoming.getCurrentSize());
  out.close();
  out = incoming.getAppendingOutputStream();
  out.write(b2);
  out.close();
  written += b2.length;
  assertEquals(written, incoming.getCurrentSize());
  out = incoming.getAppendingOutputStream();
  out.write(b3);
  out.close();
  written += b3.length;
  assertEquals(written, incoming.getCurrentSize());
  Blob blob = incoming.getBlob();

  assertEquals(bytes.length, blob.getRawSize(), "blob size = incoming written");

  assertTrue(TestUtil.bytesEqual(bytes, blob.getInputStream()), "blob content = mime content");

  StagedBlob staged = sm.stage(blob, mbox);
  assertEquals(blob.getRawSize(), staged.getSize(), "staged size = blob size");

  MailboxBlob mblob = sm.link(staged, mbox, 0, 0);
  assertEquals(staged.getSize(), mblob.getSize(), "link size = staged size");
  assertTrue(TestUtil.bytesEqual(bytes, mblob.getLocalBlob().getInputStream()), "link content = mime content");

  mblob = sm.getMailboxBlob(mbox, 0, 0, staged.getLocator());
  assertEquals(staged.getSize(), mblob.getSize(), "mblob size = staged size");
  assertTrue(TestUtil.bytesEqual(bytes, mblob.getLocalBlob().getInputStream()), "mailboxblob content = mime content");

  sm.delete(mblob);
 }

 @Test
 void incomingByteUtilCopy() throws Exception {
  //similar to incoming, but uses ByteUtil.copy() which mimics behavior of FileUploaderResource
  Random rand = new Random();
  byte[] bytes = new byte[1000000];

  rand.nextBytes(bytes);
  StoreManager sm = StoreManager.getInstance();
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  IncomingBlob incoming = sm.newIncomingBlob("foo", null);

  OutputStream out = incoming.getAppendingOutputStream();
  ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
  ByteUtil.copy(bais, true, out, true);
  assertEquals(bytes.length, incoming.getCurrentSize());

  Blob blob = incoming.getBlob();

  assertEquals(bytes.length, blob.getRawSize(), "blob size = incoming written");

  assertTrue(TestUtil.bytesEqual(bytes, blob.getInputStream()), "blob content = mime content");

  StagedBlob staged = sm.stage(blob, mbox);
  assertEquals(blob.getRawSize(), staged.getSize(), "staged size = blob size");

  MailboxBlob mblob = sm.link(staged, mbox, 0, 0);
  assertEquals(staged.getSize(), mblob.getSize(), "link size = staged size");
  assertTrue(TestUtil.bytesEqual(bytes, mblob.getLocalBlob().getInputStream()), "link content = mime content");

  mblob = sm.getMailboxBlob(mbox, 0, 0, staged.getLocator());
  assertEquals(staged.getSize(), mblob.getSize(), "mblob size = staged size");
  assertTrue(TestUtil.bytesEqual(bytes, mblob.getLocalBlob().getInputStream()), "mailboxblob content = mime content");

  sm.delete(mblob);
 }

 @Test
 void emptyBlob() throws Exception {
  StoreManager sm = StoreManager.getInstance();
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  IncomingBlob incoming = sm.newIncomingBlob("foo", null);
  Blob blob = incoming.getBlob();
  assertEquals(0, blob.getRawSize(), "blob size = incoming written");
  if (sm instanceof ExternalStoreManager) {
   ((ExternalStoreManager) sm).clearCache();
  }
  StagedBlob staged = sm.stage(blob, mbox);
  assertEquals(blob.getRawSize(), staged.getSize(), "staged size = blob size");

  if (sm instanceof ExternalStoreManager) {
   ((ExternalStoreManager) sm).clearCache();
  }
  MailboxBlob mblob = sm.link(staged, mbox, 0, 0);
  assertEquals(staged.getSize(), mblob.getSize(), "link size = staged size");


  if (sm instanceof ExternalStoreManager) {
   ((ExternalStoreManager) sm).clearCache();
  }
  mblob = sm.getMailboxBlob(mbox, 0, 0, staged.getLocator());
  assertEquals(staged.getSize(), mblob.getSize(), "mblob size = staged size");

  if (sm instanceof ExternalStoreManager) {
   ((ExternalStoreManager) sm).clearCache();
  }
  assertEquals(0, mblob.getLocalBlob().getRawSize());

  sm.delete(mblob);

 }

 @Test
 void nonExistingBlob() throws Exception {
  StoreManager sm = StoreManager.getInstance();
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  MailboxBlob blob = sm.getMailboxBlob(mbox, 999, 1, "1");
  assertNull(blob, "expect null blob");
 }
}
