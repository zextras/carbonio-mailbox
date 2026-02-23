package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AddArchiveFolderUtilTest extends MailboxTestSuite {

  private Account testAccount;
  private Mailbox testMailbox;
  private Provisioning provisioning;

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
  }

  @Test
  void shouldCreateSystemArchiveFolderToNewAccount() throws Exception {
    Account account =
        createAccount()
            .withUsername("archive-test-2")
            .withDomain(DEFAULT_DOMAIN_NAME)
            .withPassword("secret")
            .create();

    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);

    Folder archiveFolder = mailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
    assertNotNull(archiveFolder, "Archive folder should exist after account creation");
    assertEquals("Archive", archiveFolder.getName(), "Archive folder should have correct name");
  }

  @Test
  void archiveFolderIsImmutableAndCannotBeDeleted() throws Exception {
    Account account = Provisioning.getInstance().getAccountByName(testAccount.getName());

    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
    Folder archiveFolder = mailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);

    final ServiceException serviceException =
        assertThrows(
            ServiceException.class,
            () -> mailbox.deleteFolder(null, String.valueOf(archiveFolder.getId())),
            "Should not be able to delete the archive folder");

    assertEquals("cannot modify immutable object: 20", serviceException.getMessage());
  }

  @Test
  void shouldNotDuplicateArchiveFolderWhenCalledMultipleTimes() throws Exception {
    Account account = Provisioning.getInstance().getAccountByName(testAccount.getName());

    AddArchiveFolderUtil.addArchiveFolderToAccount(account.getId(), provisioning);
    Folder firstArchive = testMailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
    assertNotNull(firstArchive, "Archive folder should exist after first call");

    AddArchiveFolderUtil.addArchiveFolderToAccount(account.getId(), provisioning);
    Folder secondArchive = testMailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
    assertNotNull(secondArchive, "Archive folder should still exist");
    assertEquals(
        firstArchive.getId(), secondArchive.getId(), "Archive folder should not be duplicated");
  }

  @Test
  void shouldHandleNonExistentAccountIdGracefully() {
    String nonExistentAccountId = "non-existent-account-id-12345";

    assertDoesNotThrow(
        () -> AddArchiveFolderUtil.addArchiveFolderToAccount(nonExistentAccountId, provisioning),
        "Should not throw exception for non-existent account");
  }
}
