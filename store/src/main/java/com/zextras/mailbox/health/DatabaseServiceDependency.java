// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.health;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

/** Class represents MariaDB service dependency of mailbox */
public class DatabaseServiceDependency extends ServiceDependency {

  private final DbPool dbPool;
  private final int cacheIntervalMillis;
  private final Supplier<Long> currentTimeProvider;
  private Long lastExecMillis;
  private boolean lastHealthCheckedValue = false;

  public DatabaseServiceDependency(DbPool dbPool, Supplier<Long> currentTimeProvider) {
    this(dbPool, 5000, currentTimeProvider);
  }

  public DatabaseServiceDependency(
      DbPool dbPool, int cacheIntervalMillis, Supplier<Long> currentTimeProvider) {
    super("MariaDb", ServiceType.REQUIRED);
    this.dbPool = dbPool;
    this.cacheIntervalMillis = cacheIntervalMillis;
    this.currentTimeProvider = currentTimeProvider;
  }

  @Override
  public boolean isReady() {
    return canConnectToDatabase();
  }

  @Override
  public boolean isLive() {
    return this.canConnectToDatabase();
  }

  private boolean doCheckStatus() {
    try (DbConnection connection = dbPool.getDatabaseConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1");
        ResultSet resultSet = preparedStatement.executeQuery()) {
      resultSet.next();
      return true;
    } catch (ServiceException | SQLException e) {
      return false;
    }
  }

  private boolean canConnectToDatabase() {
    final long currentTime = currentTimeProvider.get();

    if (lastExecMillis == null || currentTime > lastExecMillis + cacheIntervalMillis) {
      lastHealthCheckedValue = doCheckStatus();
      lastExecMillis = currentTime;
    }

    return lastHealthCheckedValue;
  }
}
