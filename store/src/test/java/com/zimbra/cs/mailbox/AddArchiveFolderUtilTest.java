package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AddArchiveFolderUtilTest extends MailboxTestSuite {

  private Account testAccount;
  private Mailbox testMailbox;
  private Provisioning provisioning;
  private AddArchiveFolderUtil addArchiveFolderUtil;

  @BeforeEach
  public void setUp() throws Exception {
    clearData();
    initData();
    provisioning = Provisioning.getInstance();

    testAccount =
        createAccount()
            .withUsername("archive-test")
            .withDomain(DEFAULT_DOMAIN_NAME)
            .withPassword("secret")
            .create();

    testMailbox = MailboxManager.getInstance().getMailboxByAccount(testAccount);

    // clear the immutable attribute so we can delete the auto-created archive folder,
    // then delete it so each test starts from a clean state where createArchiveSystemFolder runs
    Folder archiveFolder = testMailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
    archiveFolder.attributes &= ~Folder.FOLDER_IS_IMMUTABLE;
    testMailbox.delete(null, archiveFolder.getId(), MailItem.Type.FOLDER);

    addArchiveFolderUtil = new AddArchiveFolderUtil(mailbox -> false);
  }

  @Test
  void createArchiveSystemFolderCreatesFolder() throws Exception {
    addArchiveFolderUtil.addArchiveFolderToAccount(testAccount.getId(), provisioning);

    Folder archiveFolder = testMailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
    assertNotNull(archiveFolder);
    assertEquals("Archive", archiveFolder.getName());
  }

  @Test
  void createArchiveSystemFolderCreatesMessageTypeFolder() throws Exception {
    addArchiveFolderUtil.addArchiveFolderToAccount(testAccount.getId(), provisioning);

    Folder archiveFolder = testMailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
    assertEquals(Type.MESSAGE, archiveFolder.getDefaultView());
  }

  @Test
  void createArchiveSystemFolderCreatesUnderUserRoot() throws Exception {
    Folder userRoot = testMailbox.getFolderById(null, Mailbox.ID_FOLDER_USER_ROOT);

    addArchiveFolderUtil.addArchiveFolderToAccount(testAccount.getId(), provisioning);

    Folder archiveFolder = testMailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
    assertEquals(userRoot.getId(), archiveFolder.getFolderId());
  }

  @Test
  void createArchiveSystemFolderSetsImmutableAttribute() throws Exception {
    addArchiveFolderUtil.addArchiveFolderToAccount(testAccount.getId(), provisioning);

    Folder archiveFolder = testMailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
    assertEquals(Folder.FOLDER_IS_IMMUTABLE, archiveFolder.getAttributes());
  }

  @Test
  void createArchiveSystemFolderCommitsTransaction() throws Exception {
    addArchiveFolderUtil.addArchiveFolderToAccount(testAccount.getId(), provisioning);

    testMailbox.purge(Type.FOLDER);

    Folder archiveFolder = testMailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
    assertNotNull(
        archiveFolder, "Folder should survive a cache purge meaning the transaction was committed");
  }

  @Test
  void createArchiveSystemFolderReleasesLockOnFailure() throws Exception {
    addArchiveFolderUtil.addArchiveFolderToAccount(testAccount.getId(), provisioning);

    AddArchiveFolderUtil failingAddArchiveFolderToAccount = new AddArchiveFolderUtil(mailbox -> false);
    assertThrows(
        ServiceException.class,
        () -> failingAddArchiveFolderToAccount.addArchiveFolderToAccount(testAccount.getId(), provisioning),
        "Second call should fail since the archive folder id is already taken");

    assertDoesNotThrow(() -> testMailbox.lock(true), "Lock should be released even after failure");
    testMailbox.unlock();
  }

  @Test
  void shouldNotCreateArchiveFolderWhenCheckerReturnsTrue() throws Exception {
    addArchiveFolderUtil.addArchiveFolderToAccount(testAccount.getId(), provisioning);
    Folder existingArchive = testMailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);

    AddArchiveFolderUtil skipSut = new AddArchiveFolderUtil(mailbox -> true);
    skipSut.addArchiveFolderToAccount(testAccount.getId(), provisioning);

    Folder archiveFolder = testMailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
    assertEquals(existingArchive.getId(), archiveFolder.getId(), "Folder should not be recreated");
  }

  @Test
  void shouldSkipCreationWhenAccountNotFound() {
    assertDoesNotThrow(
        () ->
            addArchiveFolderUtil.addArchiveFolderToAccount(
                "non-existent-account-id", provisioning));
  }
}
