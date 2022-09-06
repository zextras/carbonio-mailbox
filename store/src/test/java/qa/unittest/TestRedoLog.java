// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.DevNullOutputStream;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.redolog.RedoPlayer;
import com.zimbra.cs.redolog.util.RedoLogVerify;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StoreManager;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/** Tests redolog operations */
public class TestRedoLog {

  @Rule public TestName testInfo = new TestName();

  private static String USER_NAME;
  private static String RESTORED_NAME;
  private static String NAME_PREFIX;

  @Before
  public void setUp() throws Exception {
    NAME_PREFIX = this.getClass().getSimpleName();
    String prefix = NAME_PREFIX + "-" + testInfo.getMethodName() + "-";
    USER_NAME = prefix + "user";
    RESTORED_NAME = prefix + "restored-user";
    tearDown();
  }

  @After
  public void tearDown() throws Exception {
    TestUtil.deleteAccountIfExists(USER_NAME);
    TestUtil.deleteAccountIfExists(RESTORED_NAME);
  }

  @Test
  public void testRedoLogVerify() throws Exception {
    try (PrintStream ps = new PrintStream(new DevNullOutputStream())) {
      RedoLogVerify verify = new RedoLogVerify(null, ps);
      assertTrue(
          "RedoLogVerify.verifyFile should have been true", verify.verifyFile(getRedoLogFile()));
    }
  }

  /**
   * Verifies that redolog replay successfully copies a message from one mailbox to another and
   * leaves the original blob intact. See bug 22873.
   */
  @Test
  public void testTestRestoreMessageToNewAccount() throws Exception {
    TestUtil.createAccount(USER_NAME);
    Mailbox sourceMbox =
        TestUtil.getMailbox(USER_NAME); // make sure mailbox is pre-created as well as account
    // Add message to source account.
    long startTime = System.currentTimeMillis();
    Message sourceMsg =
        TestUtil.addMessage(sourceMbox, NAME_PREFIX + " testRestoreMessageToNewAccount");
    String sourceContent = new String(sourceMsg.getContent());
    assertTrue("Message.getContent() length should not be 0", sourceContent.length() != 0);

    // Replay log to destination account.
    Account destAccount = TestUtil.createAccount(RESTORED_NAME);
    RedoPlayer player = new RedoPlayer(false, true, false, false, false);
    Map<Integer, Integer> idMap = new HashMap<Integer, Integer>();
    Mailbox destMbox = MailboxManager.getInstance().getMailboxByAccount(destAccount);
    idMap.put(sourceMbox.getId(), destMbox.getId());
    ZimbraLog.test.info("Source Mailbox ID=%s Dest ID=%s", sourceMbox.getId(), destMbox.getId());
    player.scanLog(getRedoLogFile(), true, idMap, startTime, Long.MAX_VALUE);

    // Get destination message and compare content.
    List<Integer> destIds =
        TestUtil.search(destMbox, "in:inbox " + NAME_PREFIX, MailItem.Type.MESSAGE);
    assertEquals("Search should should only find 1 message", 1, destIds.size());
    Message destMsg = destMbox.getMessageById(null, destIds.get(0));
    String destContent = new String(destMsg.getContent());
    assertEquals("Expected=sourceContent Actual=destContent", sourceContent, destContent);

    // Make sure source content is still on disk.
    MailboxBlob blob = sourceMsg.getBlob();
    assertNotNull("Blob for sourceMsg should not be null", blob);
    sourceContent =
        new String(
            ByteUtil.getContent(
                StoreManager.getInstance().getContent(blob), sourceContent.length()));
    assertEquals("From disk Expected=destContent Actual=sourceContent", destContent, sourceContent);
  }

  private File getRedoLogFile() {
    return new File("/opt/zextras/redolog/redo.log");
  }
}
