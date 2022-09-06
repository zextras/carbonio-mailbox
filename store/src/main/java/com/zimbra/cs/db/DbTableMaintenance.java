// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/**
 * @author bburtin
 */
package com.zimbra.cs.db;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;

public class DbTableMaintenance {

  public static int runMaintenance() throws ServiceException {
    if (!(Db.getInstance() instanceof MySQL)) {
      ZimbraLog.mailbox.warn("Table maintenance only supported for MySQL.");
      return 0;
    }

    int numTables = 0;
    DbResults results =
        DbUtil.executeQuery(
            "SELECT table_schema, table_name "
                + "FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE table_schema = 'zimbra' "
                + "OR table_schema LIKE '"
                + DbMailbox.DB_PREFIX_MAILBOX_GROUP
                + "%'");

    while (results.next()) {
      String dbName = results.getString("TABLE_SCHEMA");
      String tableName = results.getString("TABLE_NAME");
      String sql = String.format("ANALYZE TABLE %s.%s", dbName, tableName);
      ZimbraLog.mailbox.info("Running %s", sql);
      DbUtil.executeUpdate(sql);
      numTables++;
    }

    return numTables;
  }
}
