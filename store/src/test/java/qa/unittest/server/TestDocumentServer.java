// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest.server;

import com.zimbra.cs.mailbox.Document;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import qa.unittest.TestUtil;

public final class TestDocumentServer {
  @Rule public TestName testInfo = new TestName();
  private static String USER_NAME = null;
  private static final String NAME_PREFIX = TestDocumentServer.class.getSimpleName();
  private long mOriginalCompressionThreshold;
  private boolean mOriginalCompressBlobs;

  @Before
  public void setUp() throws Exception {
    String prefix = NAME_PREFIX + "-" + testInfo.getMethodName() + "-";
    USER_NAME = prefix + "user";
    cleanUp();
    TestUtil.createAccount(USER_NAME);
    Volume vol = VolumeManager.getInstance().getCurrentMessageVolume();
    mOriginalCompressBlobs = vol.isCompressBlobs();
    mOriginalCompressionThreshold = vol.getCompressionThreshold();
  }

  private int getBlobCount(File dir, int id) throws Exception {
    int count = 0;
    String prefix = id + "-";
    for (File file : dir.listFiles()) {
      if (file.getName().startsWith(prefix)) {
        count++;
      }
    }
    return count;
  }

  private File getBlobDir(Document doc) throws Exception {
    MailboxBlob mblob = StoreManager.getInstance().getMailboxBlob(doc);
    File blobFile = mblob.getLocalBlob().getFile();
    return blobFile.getParentFile();
  }

  @After
  public void tearDown() throws Exception {
    // Delete documents.
    Mailbox mbox = TestUtil.getMailbox(USER_NAME);
    for (MailItem item : mbox.getItemList(null, MailItem.Type.DOCUMENT)) {
      if (item.getName().contains(NAME_PREFIX)) {
        mbox.delete(null, item.getId(), item.getType());
      }
    }
    cleanUp();
    TestUtil.deleteAccountIfExists(USER_NAME);
  }

  private void cleanUp() throws Exception {
    // Restore volume compression settings.
    VolumeManager mgr = VolumeManager.getInstance();
    Volume current = mgr.getCurrentMessageVolume();
    Volume vol =
        Volume.builder(current)
            .setCompressBlobs(mOriginalCompressBlobs)
            .setCompressionThreshold(mOriginalCompressionThreshold)
            .build();
    mgr.update(vol);
  }
}
