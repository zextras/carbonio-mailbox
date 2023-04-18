// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;

import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Document;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import java.io.ByteArrayInputStream;
import java.io.File;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class TestDocument {

  @Rule public TestName testInfo = new TestName();

  private static final String NAME_PREFIX = TestDocument.class.getSimpleName();
  private String USER_NAME;
  private String USER2_NAME;

  @Before
  public void setUp() throws Exception {
    String prefix = NAME_PREFIX + "-" + testInfo.getMethodName() + "-";
    USER_NAME = prefix + "user1";
    USER2_NAME = prefix + "user2";
    tearDown();
  }

  @After
  public void tearDown() throws Exception {
    TestUtil.deleteAccountIfExists(USER_NAME);
    TestUtil.deleteAccountIfExists(USER2_NAME);
  }

  /** Tests deletion of blob when document revision is deleted. */
  @Test
  public void testPurgeRevision() throws Exception {
    TestUtil.createAccount(USER_NAME);
    // Create document
    Mailbox mbox = TestUtil.getMailbox(USER_NAME);
    Folder.FolderOptions fopt = new Folder.FolderOptions().setDefaultView(MailItem.Type.DOCUMENT);
    Folder folder = mbox.createFolder(null, "/" + NAME_PREFIX + " testPurgeRevisions", fopt);
    Document doc =
        mbox.createDocument(
            null,
            folder.getId(),
            "test1.txt",
            "text/plain",
            NAME_PREFIX,
            "testPurgeRevisions",
            new ByteArrayInputStream("One".getBytes()));
    int id = doc.getId();
    File file1 = doc.getBlob().getLocalBlob().getFile();
    Assert.assertTrue(file1.exists());
    // Create revisions
    doc =
        mbox.addDocumentRevision(
            null,
            id,
            NAME_PREFIX,
            "test1.txt",
            "testPurgeRevisions",
            new ByteArrayInputStream("Two".getBytes()));
    int version = doc.getVersion();
    File file2 = doc.getBlob().getLocalBlob().getFile();
    Assert.assertTrue(file2.exists());
    mbox.addDocumentRevision(
        null,
        id,
        NAME_PREFIX,
        "test1.txt",
        "testPurgeRevisions",
        new ByteArrayInputStream("Three".getBytes()));
    // remove the first revision
    mbox.purgeRevision(null, id, version, false);
    Assert.assertTrue(file1.exists());
    Assert.assertFalse(file2.exists());
  }

  /** Tests deletion of blobs when document revisions are deleted. */
  @Test
  public void testPurgeRevisions() throws Exception {
    TestUtil.createAccount(USER_NAME);
    // Create document
    Mailbox mbox = TestUtil.getMailbox(USER_NAME);
    Folder.FolderOptions fopt = new Folder.FolderOptions().setDefaultView(MailItem.Type.DOCUMENT);
    Folder folder = mbox.createFolder(null, "/" + NAME_PREFIX + " testPurgeRevisions", fopt);
    Document doc =
        mbox.createDocument(
            null,
            folder.getId(),
            "test2.txt",
            "text/plain",
            NAME_PREFIX,
            "testPurgeRevisions",
            new ByteArrayInputStream("One".getBytes()));
    int id = doc.getId();
    File file1 = doc.getBlob().getLocalBlob().getFile();
    Assert.assertTrue(file1.exists());
    // Create revisions
    doc =
        mbox.addDocumentRevision(
            null,
            id,
            NAME_PREFIX,
            "test2.txt",
            "testPurgeRevisions",
            new ByteArrayInputStream("Two".getBytes()));
    int version = doc.getVersion();
    File file2 = doc.getBlob().getLocalBlob().getFile();
    Assert.assertTrue(file2.exists());
    mbox.addDocumentRevision(
        null,
        id,
        NAME_PREFIX,
        "test2.txt",
        "testPurgeRevisions",
        new ByteArrayInputStream("Three".getBytes()));
    // remove the first two revisions
    mbox.purgeRevision(null, id, version, true);
    Assert.assertFalse(file1.exists());
    Assert.assertFalse(file2.exists());
  }

  /**
   * Test REST access to publicly shared folder
   *
   * @throws Exception
   */
  @Test
  public void testPublicShare() throws Exception {
    TestUtil.createAccount(USER_NAME);
    Mailbox mbox = TestUtil.getMailbox(USER_NAME);
    String folderName = NAME_PREFIX + "testPublicSharing";
    Folder.FolderOptions fopt = new Folder.FolderOptions().setDefaultView(MailItem.Type.DOCUMENT);
    Folder folder = mbox.createFolder(null, "/" + folderName, fopt);
    Document doc =
        mbox.createDocument(
            null,
            folder.getId(),
            "test2.txt",
            "text/plain",
            NAME_PREFIX,
            "testPublicSharing",
            new ByteArrayInputStream("A Test String".getBytes()));
    mbox.grantAccess(
        null, folder.getId(), null, ACL.GRANTEE_PUBLIC, ACL.stringToRights("rw"), null);

    String URL = TestUtil.getBaseUrl() + "/home/" + mbox.getAccount().getName() + "/" + folderName;
    HttpClient eve = ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient().build();
    HttpGet get = new HttpGet(URL);
    HttpResponse response = HttpClientUtil.executeMethod(eve, get);
    int statusCode = response.getStatusLine().getStatusCode();
    Assert.assertEquals(
        "This request should succeed. Getting status code " + statusCode,
        HttpStatus.SC_OK,
        statusCode);
    String respStr = EntityUtils.toString(response.getEntity());
    Assert.assertFalse("Should not contain AUTH_EXPIRED", respStr.contains("AUTH_EXPIRED"));
    Assert.assertTrue("Should contain shared content ", respStr.contains("test2.txt"));
  }

  public static void main(String[] args) throws Exception {
    TestUtil.cliSetup();
    TestUtil.runTest(TestDocument.class);
  }
}
