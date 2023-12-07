package com.zextras.mailbox.health;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

public class DatabaseServiceDependency extends ServiceDependency {

  private final DbPool dbPool;
  private Long lastExecMillis;
  private final int pollingIntervalMillis;
  private boolean lastHealthCheckedValue = false;
  private final Supplier<Long> currentTimeProvider;

  public DatabaseServiceDependency(DbPool dbPool, Supplier<Long> currentTimeProvider) {
    this(dbPool, 5000, currentTimeProvider);
   }

  public DatabaseServiceDependency(DbPool dbPool, int pollingIntervalMillis, Supplier<Long> currentTimeProvider) {
    super("MariaDb", ServiceType.REQUIRED);
    this.pollingIntervalMillis = pollingIntervalMillis;
    this.currentTimeProvider = currentTimeProvider;
    this.dbPool = dbPool;
  }


  @Override
  public boolean isReady() {
    return this.canConnectToDatabase();
  }

  private boolean doCheckStatus() {
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

  public boolean canConnectToDatabase() {
    if (lastExecMillis == null) {
      lastHealthCheckedValue = doCheckStatus();
      lastExecMillis = currentTimeProvider.get();
    } else {
      final long currentTime = currentTimeProvider.get();
      if (currentTime > lastExecMillis + pollingIntervalMillis) {
        lastHealthCheckedValue = doCheckStatus();
      }
    }
    return lastHealthCheckedValue;
  }

  @Override
  public boolean isLive() {
    return this.canConnectToDatabase();
  }
}
