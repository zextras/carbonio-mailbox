// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import java.io.File;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import com.zimbra.cs.account.MockProvisioning;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.ThreaderTest;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.store.AbstractStoreManagerTest;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobInputStream;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StagedBlob;
import com.zimbra.cs.store.StoreManager;
import qa.unittest.TestUtil;

public abstract class AbstractExternalStoreManagerTest extends AbstractStoreManagerTest {

 @Test
 void testUncachedSubstream() throws Exception {
  ParsedMessage pm = ThreaderTest.getRootMessage();
  byte[] mimeBytes = TestUtil.readInputStream(pm.getRawInputStream());
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  StoreManager sm = StoreManager.getInstance();
  Blob blob = sm.storeIncoming(pm.getRawInputStream());
  StagedBlob staged = sm.stage(blob, mbox);
  MailboxBlob mblob = sm.link(staged, mbox, 0, 0);
  mblob = sm.getMailboxBlob(mbox, 0, 0, staged.getLocator());

  Blob localBlob = mblob.getLocalBlob();
  InputStream stream = sm.getContent(localBlob);

  assertTrue(stream instanceof BlobInputStream, "input stream external");

  if (sm instanceof ExternalStoreManager) {
   ((ExternalStoreManager) sm).clearCache();
  }
  blob.getFile().delete();
  assertFalse(blob.getFile().exists());

  //create new stream spanning the whole blob
  InputStream newStream = ((BlobInputStream) stream).newStream(0, -1);
  assertNotNull(newStream);
  assertTrue(TestUtil.bytesEqual(mimeBytes, newStream), "stream content = mime content");
 }

 @Test
 void testUncachedFile() throws Exception {
  ParsedMessage pm = ThreaderTest.getRootMessage();
  byte[] mimeBytes = TestUtil.readInputStream(pm.getRawInputStream());
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  StoreManager sm = StoreManager.getInstance();
  Blob blob = sm.storeIncoming(pm.getRawInputStream());
  StagedBlob staged = sm.stage(blob, mbox);
  MailboxBlob mblob = sm.link(staged, mbox, 0, 0);
  mblob = sm.getMailboxBlob(mbox, 0, 0, staged.getLocator());

  Blob localBlob = mblob.getLocalBlob();
  InputStream stream = sm.getContent(localBlob);

  assertTrue(stream instanceof BlobInputStream, "input stream external");

  if (sm instanceof ExternalStoreManager) {
   ((ExternalStoreManager) sm).clearCache();
  }
  blob.getFile().delete();
  assertFalse(blob.getFile().exists());

  //now get it again. this would bomb if it only looked in cache
  stream = sm.getContent(mblob.getLocalBlob());
  assertTrue(stream instanceof ExternalBlobInputStream, "input stream external");
  ExternalBlobInputStream extStream = (ExternalBlobInputStream) stream;
  File file = extStream.getRootFile();
  assertTrue(file.exists());

  assertTrue(TestUtil.bytesEqual(mimeBytes, stream), "stream content = mime content");
 }

}
