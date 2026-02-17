// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static com.zimbra.cs.mailbox.Mailbox.ID_FOLDER_ARCHIVE;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.UUIDUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem.Type;

public class AddArchiveFolderUtil {

  private static final byte ARCHIVE_SYSTEM_FOLDER_ATTRIBUTES = Folder.FOLDER_IS_IMMUTABLE;
  private static final String ARCHIVE_FOLDER_NAME = "Archive";
  private static final String ARCHIVE_BACKUP_NAME_PREFIX = "Archive_old";

  private AddArchiveFolderUtil() {
    // utility class, do not instantiate
  }

  public static void addArchiveFolderToAccount(String accountId, Provisioning provisioning)
      throws ServiceException {

    Account account = provisioning.getAccountById(accountId);
    if (account == null) {
      ZimbraLog.mailbox.info("Account not found: %s", accountId);
      return;
    }

    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
    if (mailbox == null) {
      ZimbraLog.mailbox.info("Mailbox not found for account: %s", accountId);
      return;
    }

    if (archiveSystemFolderExistsIn(mailbox)) {
      ZimbraLog.mailbox.info("Archive system folder already exists for account: %s", accountId);
      return;
    }

    createArchiveSystemFolder(mailbox, accountId);
  }

  private static boolean archiveSystemFolderExistsIn(Mailbox mailbox) {
    try {
      mailbox.getFolderById(null, Mailbox.ID_FOLDER_ARCHIVE);
      return true;
    } catch (ServiceException e) {
      return false;
    }
  }

  private static void createArchiveSystemFolder(Mailbox mailbox, String accountId)
      throws ServiceException {

    mailbox.lock(true);

    try {
      // Handle user archive folder rename BEFORE starting transaction
      // since rename manages its own transaction
      handleExistingUserArchiveFolder(mailbox, accountId);

      // Now create the system folder in a transaction
      mailbox.beginTransaction("createArchiveFolder", null);
      try {
        Folder userRoot = mailbox.getFolderById(null, Mailbox.ID_FOLDER_USER_ROOT);
        if (userRoot == null) {
          throw ServiceException.FAILURE(
              "USER_ROOT folder not found in mailbox for account: " + accountId, null);
        }

        createArchiveFolder(mailbox, userRoot, ARCHIVE_SYSTEM_FOLDER_ATTRIBUTES);

        mailbox.endTransaction(true);
        ZimbraLog.mailbox.info("Created Archive system folder for account %s", accountId);

      } catch (ServiceException e) {
        mailbox.endTransaction(false);
        throw e;
      }
    } finally {
      mailbox.unlock();
    }
  }

  private static void handleExistingUserArchiveFolder(Mailbox mailbox, String accountId)
      throws ServiceException {

    try {
      Folder userRoot = mailbox.getFolderById(null, Mailbox.ID_FOLDER_USER_ROOT);
      Folder existingArchive = userRoot.findSubfolder(ARCHIVE_FOLDER_NAME);

      if (existingArchive != null && existingArchive.getId() != ID_FOLDER_ARCHIVE) {
        String availableName = findAvailableFolderName(userRoot, ARCHIVE_BACKUP_NAME_PREFIX);

        ZimbraLog.mailbox.info(
            "Renaming existing user Archive folder (ID=%d) to '%s' for account %s",
            existingArchive.getId(), availableName, accountId);

        // rename manages its own transaction, so we don't wrap it
        mailbox.rename(
            null,
            existingArchive.getId(),
            existingArchive.getType(),
            availableName,
            existingArchive.getFolderId());
      }
    } catch (MailServiceException.NoSuchItemException e) {
      // USER_ROOT doesn't exist yet - that's fine, just continue
      ZimbraLog.mailbox.debug("USER_ROOT not found for account %s", accountId);
    }
  }

  public static void createArchiveFolder(
      Mailbox mailbox, Folder userRoot, byte systemFolderAttributes) throws ServiceException {

    Folder.create(
        ID_FOLDER_ARCHIVE,
        UUIDUtil.generateUUID(),
        mailbox,
        userRoot,
        ARCHIVE_FOLDER_NAME,
        systemFolderAttributes,
        Type.MESSAGE,
        0,
        MailItem.DEFAULT_COLOR_RGB,
        null,
        null,
        null);
  }

  @SuppressWarnings("SameParameterValue")
  private static String findAvailableFolderName(Folder parent, String baseName) {
    String candidate = baseName;
    int suffix = 1;

    while (parent.findSubfolder(candidate) != null) {
      candidate = baseName + "_" + suffix;
      suffix++;
    }

    return candidate;
  }
}