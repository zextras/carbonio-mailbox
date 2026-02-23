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

  private AddArchiveFolderUtil() {
    // utility class, do not instantiate
  }

  public static void addArchiveFolderToAccount(String accountId, Provisioning provisioning)
      throws ServiceException {

    Account account = provisioning.getAccountById(accountId);
    if (account == null) {
      ZimbraLog.mailbox.warn("Account not found: %s", accountId);
      return;
    }

    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
    if (mailbox == null) {
      ZimbraLog.mailbox.warn("Mailbox not found for account: %s", accountId);
      return;
    }

    if (archiveSystemFolderExistsIn(mailbox)) {
      ZimbraLog.mailbox.warn("Archive system folder already exists for account: %s", accountId);
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
      mailbox.beginTransaction("createArchiveFolder", null);
      try {
        Folder userRoot = mailbox.getFolderById(null, Mailbox.ID_FOLDER_USER_ROOT);
        if (userRoot == null) {
          ZimbraLog.mailbox.warn(
              "USER_ROOT folder not found in mailbox for account: %s", accountId);
          throw ServiceException.FAILURE(
              "USER_ROOT folder not found in mailbox for account: " + accountId, null);
        }

        createArchiveFolder(mailbox, userRoot, ARCHIVE_SYSTEM_FOLDER_ATTRIBUTES);

        mailbox.endTransaction(true);
        ZimbraLog.mailbox.info(
            "Successfully created Archive system folder for account: %s", accountId);

      } catch (ServiceException e) {
        mailbox.endTransaction(false);
        throw e;
      }
    } finally {
      mailbox.unlock();
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
}
