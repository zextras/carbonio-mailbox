// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.db;

import com.zimbra.common.localconfig.LC;
import java.sql.SQLException;

public record MariaDB(String host, int port, String database) implements Db {

  public static MariaDB fromLocalConfig() {
    return new MariaDB(LC.mysql_bind_address.value(), LC.mysql_port.intValue(), "zimbra");
  }

  @Override
  public boolean matchesError(SQLException e, DbError error) {
    int code = switch (error) {
      case DUPLICATE_ROW -> 1062;
      case FOREIGN_KEY_CHILD_EXISTS -> 1451;
      case NO_SUCH_TABLE -> 1146;
      case TABLE_FULL -> 1114;
    };
    return e.getErrorCode() == code;
  }
}
