// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;

import com.zimbra.client.ZMailbox;
import com.zimbra.client.ZMessage;
import com.zimbra.client.ZMessage.ZMimePart;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ByteUtil;
import java.io.File;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class TestConversion {

  @Rule public TestName testInfo = new TestName();
  private String testName = null;
  private String USER_NAME = null;
  private static final String NAME_PREFIX = TestConversion.class.getSimpleName();

  @Before
  public void setUp() throws Exception {
    testName = testInfo.getMethodName();
    USER_NAME = NAME_PREFIX + "-" + testName + "-user";
    tearDown();
    TestUtil.createAccount(USER_NAME);
  }

  @After
  public void tearDown() throws Exception {
    TestUtil.deleteAccountIfExists(USER_NAME);
  }

  /** Tests downloading attachments from a TNEF message (bug 44263). */
  @Test
  public void downloadAttachmentsFromTNEFmsg() throws Exception {
    ZMailbox mbox = TestUtil.getZMailbox(USER_NAME);
    String msgName = "/unittest/tnef.msg";
    File tnefMsg = new File(LC.zimbra_home.value() + msgName);
    Assert.assertTrue(
        String.format("To run this test copy data%1$s to /opt/zextras%1$s", msgName),
        tnefMsg.exists() && tnefMsg.canRead());

    // Add the TNEF message
    String msgContent = new String(ByteUtil.getContent(tnefMsg));
    TestUtil.addMessageLmtp(new String[] {USER_NAME}, USER_NAME, msgContent);

    // Test downloading attachments.
    ZMessage msg =
        TestUtil.getMessage(mbox, "in:inbox subject:\"" + NAME_PREFIX + " Rich text (TNEF) test\"");
    byte[] data = TestUtil.getContent(mbox, msg.getId(), "upload.gif");
    Assert.assertEquals(73, data.length);
    data = TestUtil.getContent(mbox, msg.getId(), "upload2.gif");
    Assert.assertEquals(851, data.length);

    ZMimePart part = TestUtil.getPart(msg, "upload.gif");
    checkPartSize(73, part.getSize());
    part = TestUtil.getPart(msg, "upload2.gif");
    checkPartSize(851, part.getSize());
  }

  /** The part size is calculated from the base64 content, so it may be off by a few bytes. */
  private void checkPartSize(long expected, long actual) {
    Assert.assertTrue(
        "expected " + expected + " +/- 4 bytes, got " + actual, Math.abs(expected - actual) <= 4);
  }

  public static void main(String[] args) throws Exception {
    TestUtil.cliSetup();
    TestUtil.runTest(TestConversion.class);
  }
}
