// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.health;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class DatabaseHealthCheck implements HealthCheck {


  private final DbPool dbPool;

  public DatabaseHealthCheck(DbPool dbPool) {
    this.dbPool = dbPool;
  }

  @Override
  public boolean isLive() {
    PreparedStatement stmt = null;
    ResultSet resultSet = null;
    DbConnection connection = null;
    try {
      connection = dbPool.getDatabaseConnection();
      stmt = connection.prepareStatement("SELECT 1");
      resultSet = stmt.executeQuery();
      return true;
    } catch (ServiceException | SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (!Objects.isNull(resultSet)) resultSet.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
        try {
          if (!Objects.isNull(stmt)) stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
        try {
          if (!Objects.isNull(connection)) connection.close();
        } catch (ServiceException e) {
          e.printStackTrace();
        }
    }
    return false;
  }

  @Override
  public boolean isReady() {
    //FIXME: maybe have a method that says: dbPool is Configured?
    return true;
  }
}
