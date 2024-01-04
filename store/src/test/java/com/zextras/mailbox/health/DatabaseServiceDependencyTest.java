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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DatabaseServiceDependencyTest {

  final int CacheIntervalMillis_1000 = 1000;

  @Test
  void shouldBeLiveAndReadyIfDbPoolConnectionOk() throws Exception {
    final var dbPool = setupValidDBPoolConnection();
    final DatabaseServiceDependency databaseService =
        new DatabaseServiceDependency(dbPool, System::currentTimeMillis);

    assertHealthy(databaseService);
  }

  @Test
  @DisplayName(
      "First time DB connection is OK. Check ready/live is true. Second time, after cache interval"
          + " elapsed, db connection fails. Check ready/live is false.")
  void shouldBeNotLiveAndReadyIfDbPoolConnectionOkOnSecondEvaluation() throws Exception {
    final var dbPool = setupValidDBPoolConnection();
    final var timer = FakeTimer.atZero();
    final DatabaseServiceDependency databaseService =
        new DatabaseServiceDependency(dbPool, CacheIntervalMillis_1000, timer);

    assertHealthy(databaseService);

    timer.timeElapsed(CacheIntervalMillis_1000 + 500L);
    Mockito.when(dbPool.getDatabaseConnection())
        .thenThrow(ServiceException.FAILURE("Oops, cannot open connection to database"));

    assertUnhealthy(databaseService);
  }

  @Test
  @DisplayName(
      "First time DB connection is OK. Check ready/live is true. "
          + "Second time, within polling interval elapsed, check ready/live is still true.")
  void shouldBeLiveAndReadyIfDbPoolConnectionOkOnSecondTryWithinPoolingInterval() throws Exception {
    final var dbPool = setupValidDBPoolConnection();
    final var timer = FakeTimer.atZero();
    final DatabaseServiceDependency databaseService =
        new DatabaseServiceDependency(dbPool, CacheIntervalMillis_1000, timer);

    assertHealthy(databaseService);

    timer.timeElapsed(500L);
    Mockito.when(dbPool.getDatabaseConnection())
        .thenThrow(ServiceException.FAILURE("Ooops, query failed"));

    assertHealthy(databaseService);
  }

  @Test
  void shouldNotBeLiveAndReadyIfDbPoolConnectionCannotBeTaken() throws Exception {
    final DbPool dbPool = Mockito.mock(DbPool.class);
    final ServiceException dbConnectionException = Mockito.mock(ServiceException.class);
    Mockito.when(dbPool.getDatabaseConnection()).thenThrow(dbConnectionException);

    final DatabaseServiceDependency databaseService =
        new DatabaseServiceDependency(dbPool, System::currentTimeMillis);

    assertUnhealthy(databaseService);
  }

  @Test
  void shouldNotBeLiveAndReadyIfPrepareStatementThrowsSqlException() throws Exception {
    final DbPool dbPool = Mockito.mock(DbPool.class);
    final DbConnection dbConnection = Mockito.mock(DbConnection.class);
    Mockito.when(dbPool.getDatabaseConnection()).thenReturn(dbConnection);
    Mockito.when(dbConnection.prepareStatement("SELECT 1")).thenThrow(new SQLException());

    final DatabaseServiceDependency databaseService =
        new DatabaseServiceDependency(dbPool, System::currentTimeMillis);

    assertUnhealthy(databaseService);
  }

  private static void assertHealthy(DatabaseServiceDependency databaseService) {
    Assertions.assertTrue(databaseService.isReady());
    Assertions.assertTrue(databaseService.isLive());
  }

  private static void assertUnhealthy(DatabaseServiceDependency databaseService) {
    Assertions.assertFalse(databaseService.isReady());
    Assertions.assertFalse(databaseService.isLive());
  }

  private static DbPool setupValidDBPoolConnection() throws ServiceException, SQLException {
    final DbPool dbPool = Mockito.mock(DbPool.class);
    final DbConnection dbConnection = Mockito.mock(DbConnection.class);
    final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    final ResultSet resultSet = Mockito.mock(ResultSet.class);
    Mockito.when(dbPool.getDatabaseConnection()).thenReturn(dbConnection);
    Mockito.when(dbConnection.prepareStatement("SELECT 1")).thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(true);
    return dbPool;
  }

  private static class FakeTimer implements Supplier<Long> {

    private Long currentTime;

    FakeTimer(long startTime) {
      this.currentTime = startTime;
    }

    @Override
    public Long get() {
      return currentTime;
    }

    public void timeElapsed(Long millis) {
      currentTime += millis;
    }

    public static FakeTimer atZero() {
      return new FakeTimer(0L);
    }
  }
}
