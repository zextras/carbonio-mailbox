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

public class DatabaseHealthCheck implements HealthCheck {


  private final DbPool dbPool;

  public DatabaseHealthCheck(DbPool dbPool) {
    this.dbPool = dbPool;
  }

  @Override
  public boolean isLive() {
    try (DbConnection connection = dbPool.getDatabaseConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1");
        ResultSet resultSet = preparedStatement.executeQuery()
    ) {
      resultSet.next();
      return true;
    } catch (ServiceException | SQLException e) {
      return false;
    }
  }

  @Override
  public boolean isReady() {
    //FIXME: maybe have a method that says: dbPool is Configured?
    return true;
  }
}
