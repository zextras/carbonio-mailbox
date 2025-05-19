// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.external;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.util.AccountUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.IncomingBlob;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StagedBlob;
import com.zimbra.cs.store.StoreManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qa.unittest.TestUtil;

public class StoreManagerNegativeTest {

  static StoreManager originalStoreManager;

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

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

    protected StoreManager getStoreManager() {
        return new BrokenStreamingStoreManager();
    }

 @Test
 void nullLocator() throws Exception {
   Random rand = new Random();
  byte[] bytes = new byte[10000];
  rand.nextBytes(bytes);

  StoreManager sm = StoreManager.getInstance();
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(AccountUtil.createAccount());

  IncomingBlob incoming = sm.newIncomingBlob("foo", null);

  OutputStream out = incoming.getAppendingOutputStream();
  out.write(bytes);

  Blob blob = incoming.getBlob();

  assertEquals(bytes.length, blob.getRawSize(), "blob size = incoming written");

  assertTrue(TestUtil.bytesEqual(bytes, blob.getInputStream()), "blob content = mime content");

  StagedBlob staged = sm.stage(blob, mbox);
  assertEquals(blob.getRawSize(), staged.getSize(), "staged size = blob size");

  MailboxBlob mblob = sm.link(staged, mbox, 0, 0);
  assertEquals(staged.getSize(), mblob.getSize(), "link size = staged size");
  try {
   mblob.getLocalBlob().getInputStream();
   fail("Expected IOException since locator is not handled correctly");
  } catch (IOException io) {
   //expected
  } finally {
   sm.delete(mblob);
  }
 }

 @Test
 void incorrectRemoteSize() throws Exception {
  Random rand = new Random();
  byte[] bytes = new byte[10000];
  rand.nextBytes(bytes);

  StoreManager sm = StoreManager.getInstance();
  IncomingBlob incoming = sm.newIncomingBlob("foo", null);

  OutputStream out = incoming.getAppendingOutputStream();
  out.write(bytes);

  try {
   incoming.getCurrentSize();
   fail("Expected exception since remote size is incorrect");
  } catch (IOException ioe) {
   //expected
  }
 }



    private class BrokenStreamingStoreManager extends SimpleStreamingStoreManager implements ExternalResumableUpload {

        @Override
        public String finishUpload(ExternalUploadedBlob blob) throws IOException, ServiceException {
            return null;
        }

        @Override
        public String writeStreamToStore(InputStream in, long actualSize, Mailbox mbox) throws IOException {
            super.writeStreamToStore(in, actualSize, mbox);
            return null;
        }

        @Override
        public InputStream readStreamFromStore(String locator, Mailbox mbox) throws IOException {
            return null;
        }

        @Override
        public boolean deleteFromStore(String locator, Mailbox mbox) throws IOException {
            return false;
        }

        @Override
        public ExternalResumableIncomingBlob newIncomingBlob(String id, Object ctxt) throws IOException,
                        ServiceException {
            return new SimpleStreamingIncomingBlob(id, getBlobBuilder(), ctxt);
        }

        private class SimpleStreamingIncomingBlob extends ExternalResumableIncomingBlob {

            private final File file;

            public SimpleStreamingIncomingBlob(String id, BlobBuilder blobBuilder, Object ctx) throws ServiceException,
                            IOException {
                super(id, blobBuilder, ctx);
                String baseName = uploadDirectory + "/upload-" + id;
                String name = baseName;

                synchronized (this) {
                    int count = 1;
                    File upFile = new File(name + ".upl");
                    while (upFile.exists()) {
                        name = baseName + "_" + count++;
                        upFile = new File(name + ".upl");
                    }
                    if (upFile.createNewFile()) {
                        ZimbraLog.store.debug("writing to new file %s", upFile.getName());
                        file = upFile;
                    } else {
                        throw new IOException("unable to create new file");
                    }
                }
            }

            @Override
            protected ExternalResumableOutputStream getAppendingOutputStream(BlobBuilder blobBuilder)
                            throws IOException {
                return new SimpleStreamingOutputStream(blobBuilder, file);
            }

            @Override
            protected long getRemoteSize() throws IOException {
                return file.length() - 1; //size returned wrong to test getCurrentSize() mismatches
            }

            @Override
            public Blob getBlob() throws IOException, ServiceException {
                return new ExternalUploadedBlob(blobBuilder.finish(), file.getCanonicalPath());
            }
        }

        private class SimpleStreamingOutputStream extends ExternalResumableOutputStream {

            private final FileOutputStream fos;

            public SimpleStreamingOutputStream(BlobBuilder blobBuilder, File file) throws IOException {
                super(blobBuilder);
                this.fos = new FileOutputStream(file);
            }

            @Override
            protected void writeToExternal(byte[] b, int off, int len) throws IOException {
                fos.write(b, off, len);
            }
        }
    }
}
