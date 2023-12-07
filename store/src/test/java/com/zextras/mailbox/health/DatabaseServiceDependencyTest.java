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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DatabaseServiceDependencyTest {

  @Test
  void shouldBeLiveAndReadyIfDbPoolConnectionOk() throws ServiceException, SQLException {
    final DbPool dbPool = Mockito.mock(DbPool.class);
    final DbConnection dbConnection = Mockito.mock(DbConnection.class);
    final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    final ResultSet resultSet = Mockito.mock(ResultSet.class);
    Mockito.when(dbPool.getDatabaseConnection())
        .thenReturn(dbConnection);
    Mockito.when(dbConnection.prepareStatement("SELECT 1"))
        .thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery())
        .thenReturn(resultSet);
    final DatabaseServiceDependency databaseService = new DatabaseServiceDependency(dbPool, System::currentTimeMillis);

    Assertions.assertTrue(databaseService.isReady());
    Assertions.assertTrue(databaseService.isLive());
  }

  @Test
  @DisplayName("First time DB connection is OK. Check ready/live is true. "
      + "Second time, after polling interval elapsed, db connection fails. Check ready/live is false.")
  void shouldBeNotLiveAndReadyIfDbPoolConnectionOkOnSecondEvaluation() throws ServiceException, SQLException {
    final DbPool dbPool = Mockito.mock(DbPool.class);
    final DbConnection dbConnection = Mockito.mock(DbConnection.class);
    final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    final ResultSet resultSet = Mockito.mock(ResultSet.class);
    Mockito.when(dbPool.getDatabaseConnection())
        .thenReturn(dbConnection);
    Mockito.when(dbConnection.prepareStatement("SELECT 1"))
        .thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery())
        .thenReturn(resultSet);
    Supplier<Long> currentTimeSupplier = Mockito.mock(Supplier.class);

    final int pollingIntervalMillis = 1000;
    final DatabaseServiceDependency databaseService = new DatabaseServiceDependency(dbPool, pollingIntervalMillis, currentTimeSupplier);

    Mockito.when(currentTimeSupplier.get()).thenReturn(0L);
    Assertions.assertTrue(databaseService.isReady());
    Assertions.assertTrue(databaseService.isLive());

    Mockito.when(dbPool.getDatabaseConnection())
        .thenThrow(ServiceException.FAILURE("Ooops, query failed"));
    Mockito.when(currentTimeSupplier.get()).thenReturn(1500L);

    Assertions.assertFalse(databaseService.isReady());
    Assertions.assertFalse(databaseService.isLive());
  }

  @Test
  @DisplayName("First time DB connection is OK. Check ready/live is true. "
      + "Second time, within polling interval elapsed, check ready/live is still true.")
  void shouldBeLiveAndReadyIfDbPoolConnectionOkOnSecondTryWithinPoolingInterval() throws ServiceException, SQLException {
    final DbPool dbPool = Mockito.mock(DbPool.class);
    final DbConnection dbConnection = Mockito.mock(DbConnection.class);
    final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    final ResultSet resultSet = Mockito.mock(ResultSet.class);
    Mockito.when(dbPool.getDatabaseConnection())
        .thenReturn(dbConnection);
    Mockito.when(dbConnection.prepareStatement("SELECT 1"))
        .thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery())
        .thenReturn(resultSet);
    Supplier<Long> currentTimeSupplier = Mockito.mock(Supplier.class);

    final int pollingIntervalMillis = 1000;
    final DatabaseServiceDependency databaseService = new DatabaseServiceDependency(dbPool, pollingIntervalMillis, currentTimeSupplier);

    Mockito.when(currentTimeSupplier.get()).thenReturn(0L);
    Assertions.assertTrue(databaseService.isReady());
    Assertions.assertTrue(databaseService.isLive());

    Mockito.when(dbPool.getDatabaseConnection())
        .thenThrow(ServiceException.FAILURE("Ooops, query failed"));
    Mockito.when(currentTimeSupplier.get()).thenReturn(500L);
    Assertions.assertTrue(databaseService.isReady());
    Assertions.assertTrue(databaseService.isLive());
  }


  @Test
  void shouldNotBeLiveAndReadyIfDbPoolConnectionCannotBeTaken() throws ServiceException, SQLException {
    final DbPool dbPool = Mockito.mock(DbPool.class);
    final ServiceException dbConnectionException = Mockito.mock(ServiceException.class);
    Mockito.when(dbPool.getDatabaseConnection())
        .thenThrow(dbConnectionException);

    final DatabaseServiceDependency databaseService = new DatabaseServiceDependency(dbPool, System::currentTimeMillis);

    Assertions.assertFalse(databaseService.isReady());
    Assertions.assertFalse(databaseService.isLive());
  }

  @Test
  void shouldNotBeLiveAndReadyIfPrepareStatementThrowsSqlException()
      throws ServiceException, SQLException {
    final DbPool dbPool = Mockito.mock(DbPool.class);
    final DbConnection dbConnection = Mockito.mock(DbConnection.class);
    Mockito.when(dbPool.getDatabaseConnection())
        .thenReturn(dbConnection);
    Mockito.when(dbConnection.prepareStatement("SELECT 1")).thenThrow(new SQLException());

    final DatabaseServiceDependency databaseService = new DatabaseServiceDependency(dbPool, System::currentTimeMillis);

    Assertions.assertFalse(databaseService.isReady());
    Assertions.assertFalse(databaseService.isLive());
  }

}