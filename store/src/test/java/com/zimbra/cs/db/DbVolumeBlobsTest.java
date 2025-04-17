// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.store.MailboxBlob.MailboxBlobInfo;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.file.BlobReference;
import com.zimbra.cs.store.file.FileBlobStore;
import com.zimbra.cs.util.SpoolingCache;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DbVolumeBlobsTest {

  private DbConnection conn;
  private StoreManager originalStoreManager;
  private Volume originalVolume;
	private Mailbox mailbox;
	private static Account account;

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
		account = prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>(
				Map.of(Provisioning.A_zimbraId, UUID.randomUUID().toString())
		));
    System.setProperty("zimbra.native.required", "false");

		prov.createAccount("test2@zimbra.com", "secret", new HashMap<String, Object>(
				Map.of(Provisioning.A_zimbraId, UUID.randomUUID().toString())
		));
    // need MVCC since the VolumeManager code creates connections internally
    HSQLDB db = (HSQLDB) Db.getInstance();
    db.useMVCC(null);
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
    conn = DbPool.getConnection();
    originalStoreManager = StoreManager.getInstance();
    originalVolume = VolumeManager.getInstance().getCurrentMessageVolume();
    LC.zimbra_tmp_directory.setDefault(System.getProperty("user.dir") + "/build/tmp");
    StoreManager.setInstance(new FileBlobStore());
    StoreManager.getInstance().startup();
    // need to create mailbox after each cleanup
    mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
  }

  @AfterEach
  public void tearDown() throws Exception {
    conn.close();
    if (VolumeManager.getInstance().getCurrentMessageVolume() != originalVolume) {
      ZimbraLog.store.info("setting back to original volume");
      VolumeManager.getInstance().setCurrentVolume(Volume.TYPE_MESSAGE, originalVolume.getId());
    }
    StoreManager.getInstance().shutdown();
    StoreManager.setInstance(originalStoreManager);
  }

  private String getPath(BlobReference ref) throws ServiceException {
    return FileBlobStore.getBlobPath(
        ref.getMailboxId(), ref.getItemId(), ref.getRevision(), ref.getVolumeId());
  }

 @Test
 void writeBlobInfo() throws Exception {
  Mailbox mbox =
    mailbox;
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg =
    mbox.addMessage(
      null,
      new ParsedMessage("From: from1@zimbra.com\r\nTo: to1@zimbra.com".getBytes(), false),
      opt,
      null);

  Volume vol = VolumeManager.getInstance().getCurrentMessageVolume();

  DbVolumeBlobs.addBlobReference(conn, mbox, vol, msg);

  String digest = msg.getBlob().getDigest();
  String path = msg.getBlob().getLocalBlob().getFile().getPath();
  List<BlobReference> blobs = DbVolumeBlobs.getBlobReferences(conn, digest, vol);
  assertEquals(1, blobs.size());
  BlobReference ref = blobs.get(0);

  assertEquals(path, getPath(ref));
 }

 @Test
 void testDuplicateRow() throws Exception {
  Mailbox mbox =
    mailbox;

  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg =
    mbox.addMessage(
      null,
      new ParsedMessage("From: from1@zimbra.com\r\nTo: to1@zimbra.com".getBytes(), false),
      opt,
      null);

  Volume vol = VolumeManager.getInstance().getCurrentMessageVolume();
  MailboxBlobInfo blobInfo =
    new MailboxBlobInfo(
      null,
      mbox.getId(),
      msg.getId(),
      msg.getSavedSequence(),
      String.valueOf(vol.getId()),
      null);
  DbVolumeBlobs.addBlobReference(conn, blobInfo);
  try {
   DbVolumeBlobs.addBlobReference(conn, blobInfo);
   fail("expected exception");
  } catch (ServiceException e) {
   // expected
  }
 }

 @Test
 void testIncrementalBlobs() throws Exception {
  Mailbox mbox =
    mailbox;
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  int ts1 = (int) (System.currentTimeMillis() / 1000);
  Message msg1 =
    mbox.addMessage(
      null,
      new ParsedMessage("From: from1@zimbra.com\r\nTo: to1@zimbra.com".getBytes(), false),
      opt,
      null);
  Thread.sleep(1000);
  int ts2 = (int) (System.currentTimeMillis() / 1000);
  Message msg2 =
    mbox.addMessage(
      null,
      new ParsedMessage("From: from1@zimbra.com\r\nTo: to1@zimbra.com".getBytes(), false),
      opt,
      null);
  Thread.sleep(1000);
  int ts3 = (int) (System.currentTimeMillis() / 1000);
  Iterable<MailboxBlobInfo> allBlobs = null;
  Volume vol = VolumeManager.getInstance().getCurrentMessageVolume();
  allBlobs = DbMailItem.getAllBlobs(conn, mbox.getSchemaGroupId(), vol.getId(), ts1, ts2);
  assertEquals(msg1.getId(), allBlobs.iterator().next().itemId);
  allBlobs = DbMailItem.getAllBlobs(conn, mbox.getSchemaGroupId(), vol.getId(), ts2, ts3);
  assertEquals(msg2.getId(), allBlobs.iterator().next().itemId);
 }

 @Test
 void writeAllBlobRefs() throws Exception {
  Mailbox mbox =
    mailbox;
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  Map<String, String> digestToPath = new HashMap<String, String>();
  Volume vol = VolumeManager.getInstance().getCurrentMessageVolume();

  for (int i = 0; i < 10; i++) {
   Message msg =
     mbox.addMessage(
       null,
       new ParsedMessage(
         ("From: from" + i + "@zimbra.com\r\nTo: to1@zimbra.com").getBytes(), false),
       opt,
       null);
   digestToPath.put(msg.getDigest(), msg.getBlob().getLocalBlob().getFile().getPath());
  }
  Iterable<MailboxBlobInfo> allBlobs = null;
  allBlobs = DbMailItem.getAllBlobs(conn, mbox.getSchemaGroupId(), vol.getId(), -1, -1);
  for (MailboxBlobInfo info : allBlobs) {
   DbVolumeBlobs.addBlobReference(conn, info);
  }

  List<BlobReference> blobs = DbVolumeBlobs.getBlobReferences(conn, vol);
  assertEquals(digestToPath.size(), blobs.size());
  for (BlobReference blob : blobs) {
   String path = digestToPath.remove(blob.getDigest());
   assertNotNull(path);
   assertEquals(path, getPath(blob));
  }

  assertTrue(digestToPath.isEmpty());
 }

 @Test
 void testUniqueBlobDigests() throws Exception {
  Mailbox mbox =
    mailbox;
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  Volume vol = VolumeManager.getInstance().getCurrentMessageVolume();

  for (int i = 0; i < 5; i++) {
   mbox.addMessage(
     null,
     new ParsedMessage(
       ("From: from" + i + "@zimbra.com\r\nTo: to1@zimbra.com").getBytes(), false),
     opt,
     null);
   mbox.addMessage(
     null,
     new ParsedMessage(
       ("From: from" + i + "@zimbra.com\r\nTo: to1@zimbra.com").getBytes(), false),
     opt,
     null);
  }
  Iterable<MailboxBlobInfo> allBlobs = null;
  allBlobs = DbMailItem.getAllBlobs(conn, mbox.getSchemaGroupId(), vol.getId(), -1, -1);
  for (MailboxBlobInfo info : allBlobs) {
   DbVolumeBlobs.addBlobReference(conn, info);
  }
  SpoolingCache<String> digests = DbVolumeBlobs.getUniqueDigests(conn, vol);
  assertEquals(5, digests.size());
 }

 @Test
 void dumpsterBlobs() throws Exception {
  Mailbox mbox =
    mailbox;
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  Map<String, String> digestToPath = new HashMap<String, String>();
  Volume vol = VolumeManager.getInstance().getCurrentMessageVolume();

  for (int i = 0; i < 10; i++) {
   Message msg =
     mbox.addMessage(
       null,
       new ParsedMessage(
         ("From: from" + i + "@zimbra.com\r\nTo: to1@zimbra.com").getBytes(), false),
       opt,
       null);
   digestToPath.put(msg.getDigest(), msg.getBlob().getLocalBlob().getFile().getPath());
   mbox.delete(null, msg.getId(), msg.getType());
  }

  mbox.emptyFolder(null, Mailbox.ID_FOLDER_TRASH, false);

  Iterable<MailboxBlobInfo> allBlobs = null;
  allBlobs = DbMailItem.getAllBlobs(conn, mbox.getSchemaGroupId(), vol.getId(), -1, -1);
  for (MailboxBlobInfo info : allBlobs) {
   DbVolumeBlobs.addBlobReference(conn, info);
  }

  List<BlobReference> blobs = DbVolumeBlobs.getBlobReferences(conn, vol);
  assertEquals(digestToPath.size(), blobs.size());
  for (BlobReference blob : blobs) {
   String path = digestToPath.remove(blob.getDigest());
   assertNotNull(path);
   assertEquals(path, getPath(blob));
  }

  assertTrue(digestToPath.isEmpty());
 }

 @Test
 void deleteBlobRef() throws Exception {
  Mailbox mbox =
    mailbox;
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg =
    mbox.addMessage(
      null,
      new ParsedMessage("From: from1@zimbra.com\r\nTo: to1@zimbra.com".getBytes(), false),
      opt,
      null);

  Volume vol = VolumeManager.getInstance().getCurrentMessageVolume();

  DbVolumeBlobs.addBlobReference(conn, mbox, vol, msg);

  String digest = msg.getBlob().getDigest();
  String path = msg.getBlob().getLocalBlob().getFile().getPath();
  List<BlobReference> blobs = DbVolumeBlobs.getBlobReferences(conn, digest, vol);
  assertEquals(1, blobs.size());
  assertEquals(path, getPath(blobs.get(0)));

  DbVolumeBlobs.deleteBlobRef(conn, blobs.get(0).getId());

  blobs = DbVolumeBlobs.getBlobReferences(conn, digest, vol);
  assertEquals(0, blobs.size());
 }

 @Test
 void deleteAllBlobRef() throws Exception {
  Mailbox mbox =
    mailbox;
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  Message msg =
    mbox.addMessage(
      null,
      new ParsedMessage("From: from1@zimbra.com\r\nTo: to1@zimbra.com".getBytes(), false),
      opt,
      null);

  Volume vol = VolumeManager.getInstance().getCurrentMessageVolume();

  DbVolumeBlobs.addBlobReference(conn, mbox, vol, msg);

  List<BlobReference> blobs = DbVolumeBlobs.getBlobReferences(conn, vol);
  assertEquals(1, blobs.size());

  DbVolumeBlobs.deleteAllBlobRef(conn);

  blobs = DbVolumeBlobs.getBlobReferences(conn, vol);
  assertEquals(0, blobs.size());
 }

 @Test
 void blobsByMbox() throws Exception {
  Mailbox mbox =
    mailbox;
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  ParsedMessage pm =
    new ParsedMessage("From: from1@zimbra.com\r\nTo: to1@zimbra.com".getBytes(), false);
  Message msg = mbox.addMessage(null, pm, opt, null);

  Volume vol = VolumeManager.getInstance().getCurrentMessageVolume();

  DbVolumeBlobs.addBlobReference(conn, mbox, vol, msg);

  String digest = msg.getBlob().getDigest();
  String path = msg.getBlob().getLocalBlob().getFile().getPath();
  List<BlobReference> blobs = DbVolumeBlobs.getBlobReferences(conn, digest, vol);
  assertEquals(1, blobs.size());
  assertEquals(path, getPath(blobs.get(0)));

  Account acct2 = Provisioning.getInstance().getAccount("test2@zimbra.com");
  Mailbox mbox2 = MailboxManager.getInstance().getMailboxByAccount(acct2);
  Message msg2 = mbox2.addMessage(null, pm, opt, null);

  DbVolumeBlobs.addBlobReference(conn, mbox2, vol, msg2);

  blobs = DbVolumeBlobs.getBlobReferences(conn, digest, vol);

  Set<String> paths = new HashSet<String>();
  paths.add(path);
  paths.add(msg2.getBlob().getLocalBlob().getFile().getPath());

  assertEquals(2, blobs.size());
  for (BlobReference ref : blobs) {
   assertTrue(paths.remove(getPath(ref)));
  }

  assertTrue(paths.isEmpty());

  DbVolumeBlobs.deleteBlobRef(conn, mbox);

  blobs = DbVolumeBlobs.getBlobReferences(conn, digest, vol);
  assertEquals(1, blobs.size());
  BlobReference ref = blobs.get(0);
  path = msg2.getBlob().getLocalBlob().getFile().getPath();

  assertEquals(path, getPath(ref));
  assertEquals(mbox2.getId(), ref.getMailboxId());
 }

 @Test
 void blobsByVolume() throws Exception {
  Mailbox mbox =
    mailbox;
  DeliveryOptions opt = new DeliveryOptions();
  opt.setFolderId(Mailbox.ID_FOLDER_INBOX);
  ParsedMessage pm =
    new ParsedMessage("From: from1@zimbra.com\r\nTo: to1@zimbra.com".getBytes(), false);
  Message msg = mbox.addMessage(null, pm, opt, null);

  Volume vol = VolumeManager.getInstance().getCurrentMessageVolume();

  DbVolumeBlobs.addBlobReference(conn, mbox, vol, msg);

  String volPath = vol.getRootPath().replace("store", "store2");
  File volFile = new File(volPath);
  volFile.mkdirs();

  Volume vol2 =
    Volume.builder()
      .setPath(volFile.getAbsolutePath(), true)
      .setType(Volume.TYPE_MESSAGE)
      .setName("volume2")
      .build();

  vol2 = VolumeManager.getInstance().create(vol2);

  VolumeManager.getInstance().setCurrentVolume(Volume.TYPE_MESSAGE, vol2.getId());

  Message msg2 = mbox.addMessage(null, pm, opt, null);
  DbVolumeBlobs.addBlobReference(conn, mbox, vol2, msg2);

  String digest = msg.getBlob().getDigest();

  // add same msg to two different volumes

  List<BlobReference> blobs = DbVolumeBlobs.getBlobReferences(conn, vol);

  assertEquals(1, blobs.size());

  Set<String> paths = new HashSet<String>();
  paths.add(msg.getBlob().getLocalBlob().getFile().getPath());
  for (BlobReference ref : blobs) {
   assertTrue(paths.remove(getPath(ref)));
   assertEquals(vol.getId(), ref.getVolumeId());
  }

  blobs = DbVolumeBlobs.getBlobReferences(conn, vol2);

  assertEquals(1, blobs.size());

  paths = new HashSet<String>();
  paths.add(msg2.getBlob().getLocalBlob().getFile().getPath());
  for (BlobReference ref : blobs) {
   assertTrue(paths.remove(getPath(ref)));
   assertEquals(vol2.getId(), ref.getVolumeId());
  }

  blobs = DbVolumeBlobs.getBlobReferences(conn, digest, vol);

  paths = new HashSet<String>();
  paths.add(msg.getBlob().getLocalBlob().getFile().getPath());

  assertEquals(1, blobs.size());
  for (BlobReference ref : blobs) {
   assertTrue(paths.remove(getPath(ref)));
  }

  blobs = DbVolumeBlobs.getBlobReferences(conn, digest, vol2);

  paths = new HashSet<String>();
  paths.add(msg2.getBlob().getLocalBlob().getFile().getPath());

  assertEquals(1, blobs.size());
  for (BlobReference ref : blobs) {
   assertTrue(paths.remove(getPath(ref)));
  }

  // delete from vol1
  DbVolumeBlobs.deleteBlobRef(conn, vol);

  blobs = DbVolumeBlobs.getBlobReferences(conn, digest, vol2);

  paths = new HashSet<String>();
  paths.add(msg2.getBlob().getLocalBlob().getFile().getPath());

  assertEquals(1, blobs.size());
  for (BlobReference ref : blobs) {
   assertTrue(paths.remove(getPath(ref)));
  }

  blobs = DbVolumeBlobs.getBlobReferences(conn, vol);
  assertEquals(0, blobs.size());

  blobs = DbVolumeBlobs.getBlobReferences(conn, vol2);
  assertEquals(1, blobs.size());

  paths = new HashSet<String>();
  paths.add(msg2.getBlob().getLocalBlob().getFile().getPath());
  for (BlobReference ref : blobs) {
   assertTrue(paths.remove(getPath(ref)));
  }

  blobs = DbVolumeBlobs.getBlobReferences(conn, digest, vol2);

  paths = new HashSet<String>();
  paths.add(msg2.getBlob().getLocalBlob().getFile().getPath());

  assertEquals(1, blobs.size());
  for (BlobReference ref : blobs) {
   assertTrue(paths.remove(getPath(ref)));
  }
 }
}
