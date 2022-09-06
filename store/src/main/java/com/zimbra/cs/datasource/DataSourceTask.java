// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.ScheduledTask;

public class DataSourceTask extends ScheduledTask {

  private static final String KEY_DATA_SOURCE_ID = "dsid";

  /** Constructor with no arguments required for task instantiation. */
  public DataSourceTask() {}

  public DataSourceTask(int mailboxId, String accountId, String dataSourceId, long intervalMillis) {
    if (StringUtil.isNullOrEmpty(accountId)) {
      throw new IllegalArgumentException("accountId cannot be null or empty");
    }
    if (StringUtil.isNullOrEmpty(dataSourceId)) {
      throw new IllegalArgumentException("dataSourceId cannot be null or empty");
    }

    setMailboxId(mailboxId);
    setProperty(KEY_DATA_SOURCE_ID, dataSourceId);
    setIntervalMillis(intervalMillis);
  }

  @Override
  public String getName() {
    return getDataSourceId();
  }

  public String getDataSourceId() {
    return getProperty(KEY_DATA_SOURCE_ID);
  }

  @Override
  public Void call() {
    ZimbraLog.clearContext();
    ZimbraLog.addMboxToContext(getMailboxId());
    ZimbraLog.datasource.debug("Running scheduled import for DataSource %s", getDataSourceId());
    Mailbox mbox = null;

    try {
      // Look up mailbox, account and data source
      mbox = MailboxManager.getInstance().getMailboxById(getMailboxId());
      Account account = mbox.getAccount();
      ZimbraLog.addAccountNameToContext(account.getName());
      Provisioning prov = Provisioning.getInstance();
      DataSource ds = prov.get(account, Key.DataSourceBy.id, getDataSourceId());
      if (ds != null) {
        ZimbraLog.addDataSourceNameToContext(ds.getName());
        if (!ds.isEnabled()) {
          ZimbraLog.datasource.info("DataSource is disabled.  Cancelling future tasks.");
          DataSourceManager.cancelTask(mbox, getDataSourceId());
          return null;
        }

        // Do the work
        DataSourceManager.importData(ds);
      } else {
        ZimbraLog.datasource.info(
            "DataSource %s was deleted.  Cancelling future tasks.", getDataSourceId());
        DataSourceManager.cancelTask(mbox, getDataSourceId());
      }
    } catch (ServiceException e) {
      ZimbraLog.datasource.warn("Scheduled DataSource import failed.", e);
      return null;
    }

    ZimbraLog.clearContext();
    return null;
  }
}
