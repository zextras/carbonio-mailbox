// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.callback.CallbackUtil;
import com.zimbra.cs.util.Config;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContactBackupThread extends Thread {
  private static final String OPERATION = "ContactBackup";
  private static volatile ContactBackupThread backupThread = null;
  private static Object THREAD_LOCK = new Object();
  private static boolean shutdownRequested = false;
  private static final String CT_TYPE = "application/x-compressed-tar";
  private static final String CONTACT_RES_URL = "?fmt=tgz&types=contact";
  private static final String FILE_NAME = "Contacts";
  private static final String FILE_DESC = "Contact Backup at ";
  private static final String DATE_FORMAT = "yyyy-MM-dd-HHmmss";
  private boolean success = true;

  private ContactBackupThread() {
    setName(OPERATION);
  }

  public static void startup() {
    synchronized (THREAD_LOCK) {
      if (isRunning()) {
        ZimbraLog.contactbackup.warn("can not start another thread");
        return;
      }
      backupThread = new ContactBackupThread();
      shutdownRequested = false;
      backupThread.start();
    }
  }

  public static void shutdown() {
    synchronized (THREAD_LOCK) {
      if (backupThread != null) {
        shutdownRequested = true;
        backupThread.interrupt();
        backupThread = null;
        ZimbraLog.contactbackup.debug("shutdown done");
      } else {
        ZimbraLog.contactbackup.debug("shutdown requested but %s is not running", OPERATION);
      }
    }
  }

  public static synchronized boolean isRunning() {
    synchronized (THREAD_LOCK) {
      return backupThread != null;
    }
  }

  private static void setContactBackupLastMailboxId(int id) {
    try {
      Config.setInt(Config.CONTACT_BACKUP_LAST_MAILBOX_ID, id);
      ZimbraLog.contactbackup.debug("setting contact backup last mailbox id with %d", id);
    } catch (ServiceException se) {
      ZimbraLog.contactbackup.warn(
          "exception occured while setting contact backup last mailbox id with %d", id);
      ZimbraLog.contactbackup.debug(se);
    }
  }

  /**
   * Iterate over list of mailbox ids and start backup on each one of them. Sleep for
   * zimbraFeatureContactBackupFrequency once the thread cycle is over.
   */
  @Override
  public void run() {
    List<Integer> mailboxIds = new ArrayList<Integer>();
    try {
      mailboxIds = CallbackUtil.getSortedMailboxIdList();
    } catch (ServiceException e) {
      ZimbraLog.contactbackup.warn("can not get list of mailboxes, shutting down thread.");
      ZimbraLog.contactbackup.debug(e);
      backupThread = null;
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("mailboxId,email,contact backup").append("\n");
    Date startTime = new Date();
    ZimbraLog.contactbackup.debug("starting iteration on mailboxes at %s", startTime.toString());
    for (Integer mailboxId : mailboxIds) {
      if (shutdownRequested) {
        ZimbraLog.contactbackup.info("shutting down thread.");
        return;
      }
      ZimbraLog.contactbackup.debug("starting to work with mailbox %d", mailboxId);
      ZimbraLog.addMboxToContext(mailboxId);
      success = true;
      try {
        Mailbox mbox = MailboxManager.getInstance().getMailboxById(mailboxId);
        sb.append(mailboxId).append(",");
        Account account = mbox.getAccount();
        sb.append(account.getMail()).append(",");
        ZimbraLog.addAccountNameToContext(account.getName());
        if (account.isFeatureContactBackupEnabled()
            && !account.isIsSystemAccount()
            && !account.isIsSystemResource()
            && account.isAccountStatusActive()) {
          success = false;
          ZimbraLog.contactbackup.info(
              "contact backup folder not found for %d, continuing to next mailbox", mailboxId);
          // set current mailbox id as last processed mailbox
          if (success) {
            setContactBackupLastMailboxId(mailboxId);
            sb.append("success");
          }
        } else {
          ZimbraLog.contactbackup.debug(
              "contact backup skipped for %d: feature is disabled/account is inactive/it's a system"
                  + " account",
              mailboxId);
          sb.append("skipped");
        }
      } catch (Exception e) {
        ZimbraLog.contactbackup.warn(
            "backup/purge failed for mailbox %d, continuing to next mailbox", mailboxId);
        ZimbraLog.contactbackup.debug(e);
        sb.append("failed");
      }
      sb.append("\n");
      ZimbraLog.clearContext();
    }
    setContactBackupLastMailboxId(0);
    createReport(sb, startTime);
    long endTime = System.currentTimeMillis();
    long diff = endTime - startTime.getTime();
    ZimbraLog.contactbackup.debug("finished iteration on mailboxes, iteration took %d ms", diff);
    ContactBackupThread.shutdown();
  }

  private void createReport(StringBuilder builder, Date startTime) {
    File folderLog = new File(LC.zimbra_log_directory.value());
    if (!folderLog.exists() || !folderLog.isDirectory() || !folderLog.canWrite()) {
      ZimbraLog.contactbackup.debug("Failed to find log folder to save contact backup report.");
      return;
    }
    File reportFile =
        new File(
            folderLog,
            "ContactBackupReport.csv." + new SimpleDateFormat("yyyyMMddHHmmss").format(startTime));
    FileWriter writer = null;
    boolean success = true;
    try {
      writer = new FileWriter(reportFile);
      writer.write(builder.toString());
    } catch (IOException ioe) {
      ZimbraLog.contactbackup.debug("Failed to write in report file.");
      ZimbraLog.contactbackup.debug(ioe);
      success = false;
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException ioe) {
          ZimbraLog.contactbackup.debug("Error occurred while closing writer.");
          ZimbraLog.contactbackup.debug(ioe);
          success = false;
        }
      }
    }
    if (success) {
      ZimbraLog.contactbackup.debug(
          "Contact backup report stored at %s", reportFile.getAbsolutePath());
    }
  }
}
