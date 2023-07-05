// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.triton;

import java.io.ByteArrayInputStream;
import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.zimbra.cs.account.MockProvisioning;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.store.AbstractStoreManagerTest;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StagedBlob;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.StoreManager.StoreFeature;
import com.zimbra.cs.store.external.ContentAddressableStoreManager;
import com.zimbra.cs.store.triton.TritonBlobStoreManager.HashType;
import qa.unittest.TestUtil;

@Disabled("requires Triton server")
public class TritonBlobStoreManagerTest extends AbstractStoreManagerTest {
    @Override
    protected StoreManager getStoreManager() {
        return new TritonBlobStoreManager("http://192.168.2.107", HashType.SHA0) {
//        return new TritonBlobStoreManager("http://10.33.30.77", HashType.SHA0) {

            @Override
            public boolean supports(StoreFeature feature) {
                switch (feature) {
                    //normally TBSM only supports SIS if using SHA256, force it here
                    case SINGLE_INSTANCE_SERVER_CREATE : return true;
                    default: return super.supports(feature);
                }
            }
        };
    }

 @Test
 void sis() throws Exception {
  TritonBlobStoreManager sm = (TritonBlobStoreManager) StoreManager.getInstance();
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  assertTrue(sm instanceof ContentAddressableStoreManager, "StoreManager is content addressable");
  assertTrue(sm.supports(StoreFeature.SINGLE_INSTANCE_SERVER_CREATE), "StoreManager supports SIS check");

  Random rand = new Random();
  byte[] bytes = new byte[10000];

  rand.nextBytes(bytes);

  Blob blob = sm.storeIncoming(new ByteArrayInputStream(bytes));

  //blob has not yet been finalized, so it shouldn't exist in remote system yet

  byte[] hash = sm.getHash(blob);

  assertNull(sm.getSisBlob(hash), "object not yet created");

  assertEquals(bytes.length, blob.getRawSize(), "blob size = incoming written");

  assertTrue(TestUtil.bytesEqual(bytes, blob.getInputStream()), "blob content = mime content");

  StagedBlob staged = sm.stage(blob, mbox);
  assertEquals(blob.getRawSize(), staged.getSize(), "staged size = blob size");

  MailboxBlob mblob = sm.link(staged, mbox, 0, 0);
  assertEquals(staged.getSize(), mblob.getSize(), "link size = staged size");
  assertTrue(TestUtil.bytesEqual(bytes, mblob.getLocalBlob().getInputStream()), "link content = mime content");

  //blob uploaded, now sis should return true and increment ref count
  Blob sisBlob = sm.getSisBlob(hash);
  assertNotNull(sisBlob, "object created");

  assertEquals(bytes.length, sisBlob.getRawSize(), "blob size = incoming written");
  assertTrue(TestUtil.bytesEqual(bytes, sisBlob.getInputStream()), "blob content = mime content");

  //delete once, should still exist;
  sm.delete(mblob);
  assertNotNull(sm.getSisBlob(hash), "object still ref'd");

  //delete twice (once for original, once since we just did a sisCheck above)
  sm.delete(mblob);
  sm.delete(mblob);

  assertNull(sm.getSisBlob(hash), "object deleted");
 }

 @Test
 void sisEmpty() throws Exception {
  TritonBlobStoreManager sm = (TritonBlobStoreManager) StoreManager.getInstance();
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);

  assertTrue(sm instanceof ContentAddressableStoreManager, "StoreManager is content addressable");
  assertTrue(sm.supports(StoreFeature.SINGLE_INSTANCE_SERVER_CREATE), "StoreManager supports SIS check");

  byte[] bytes = new byte[0];

  Blob blob = sm.storeIncoming(new ByteArrayInputStream(bytes));

  //blob has not yet been finalized, so it shouldn't exist in remote system yet

  byte[] hash = sm.getHash(blob);

  assertNotNull(sm.getSisBlob(hash), "empty blob always exists");

  assertEquals(bytes.length, blob.getRawSize(), "blob size = incoming written");

  assertTrue(TestUtil.bytesEqual(bytes, blob.getInputStream()), "blob content = mime content");

  StagedBlob staged = sm.stage(blob, mbox);
  assertEquals(blob.getRawSize(), staged.getSize(), "staged size = blob size");

  MailboxBlob mblob = sm.link(staged, mbox, 0, 0);
  assertEquals(staged.getSize(), mblob.getSize(), "link size = staged size");
  assertTrue(TestUtil.bytesEqual(bytes, mblob.getLocalBlob().getInputStream()), "link content = mime content");


  //blob uploaded, now sis should return true and increment ref count
  Blob sisBlob = sm.getSisBlob(hash);
  assertNotNull(sisBlob, "object created");


  assertEquals(bytes.length, sisBlob.getRawSize(), "blob size = incoming written");
  assertTrue(TestUtil.bytesEqual(bytes, sisBlob.getInputStream()), "blob content = mime content");
 }
}
