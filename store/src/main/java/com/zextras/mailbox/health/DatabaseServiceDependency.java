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
import java.util.function.Supplier;

/**
 * Class represents MariaDB service dependency of mailbox
 */
public class DatabaseServiceDependency extends ServiceDependency {

  private final DbPool dbPool;

  public DatabaseServiceDependency(DbPool dbPool, Supplier<Long> currentTimeProvider) {
    this(dbPool, 5000, currentTimeProvider);
  }

  public DatabaseServiceDependency(DbPool dbPool, int pollingIntervalMillis,
      Supplier<Long> currentTimeProvider) {
    super("MariaDb", ServiceType.REQUIRED, pollingIntervalMillis, currentTimeProvider);
    this.dbPool = dbPool;
  }

  @Override
  public boolean isReady() {
    return canConnectToService();
  }

  @Override
  public boolean isLive() {
    return canConnectToService();
  }

  @Override
  protected boolean doCheckStatus() {
    try (DbConnection connection = dbPool.getDatabaseConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1");
        ResultSet resultSet = preparedStatement.executeQuery()) {
      return resultSet.next();
    } catch (ServiceException | SQLException e) {
      return false;
    }
  }
}
