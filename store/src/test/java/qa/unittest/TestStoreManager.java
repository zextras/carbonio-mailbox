// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.StagedBlob;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.util.JMSession;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class TestStoreManager {

  @Rule public TestName testInfo = new TestName();
  private static String USER_NAME = null;
  private static final String NAME_PREFIX = TestStoreManager.class.getSimpleName();

  public static ParsedMessage getMessage() throws Exception {
    MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession());
    mm.setHeader("From", " Jimi <jimi@example.com>");
    mm.setHeader("To", " Janis <janis@example.com>");
    mm.setHeader("Subject", "Hello");
    mm.setHeader("Message-ID", "<sakfuslkdhflskjch@oiwm.example.com>");
    mm.setText("nothing to see here" + RandomStringUtils.random(1024));
    return new ParsedMessage(mm, false);
  }

  @Before
  public void setUp() throws Exception {
    String prefix = NAME_PREFIX + "-" + testInfo.getMethodName() + "-";
    USER_NAME = prefix + "user";
    cleanUp();
    TestUtil.createAccount(USER_NAME);
  }

  @After
  public void tearDown() throws Exception {
    cleanUp();
  }

  private void cleanUp() throws Exception {
    TestUtil.deleteAccountIfExists(USER_NAME);
  }

  @Test
  public void testStore() throws Exception {
    ParsedMessage pm = getMessage();
    byte[] mimeBytes = TestUtil.readInputStream(pm.getRawInputStream());

    Mailbox mbox = TestUtil.getMailbox(USER_NAME);

    StoreManager sm = StoreManager.getInstance();
    Blob blob = sm.storeIncoming(pm.getRawInputStream());

    Assert.assertEquals("blob size = message size", pm.getRawData().length, blob.getRawSize());
    Assert.assertTrue(
        "blob content = mime content", TestUtil.bytesEqual(mimeBytes, blob.getInputStream()));

    StagedBlob staged = sm.stage(blob, mbox);
    Assert.assertEquals("staged size = blob size", blob.getRawSize(), staged.getSize());

    MailboxBlob mblob = sm.link(staged, mbox, 0, 0);
    Assert.assertEquals("link size = staged size", staged.getSize(), mblob.getSize());
    Assert.assertTrue(
        "link content = mime content",
        TestUtil.bytesEqual(mimeBytes, mblob.getLocalBlob().getInputStream()));

    mblob = sm.getMailboxBlob(mbox, 0, 0, staged.getLocator());
    Assert.assertEquals("mblob size = staged size", staged.getSize(), mblob.getSize());
    Assert.assertTrue(
        "mailboxblob content = mime content",
        TestUtil.bytesEqual(mimeBytes, mblob.getLocalBlob().getInputStream()));
  }
}
