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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DatabaseHealthCheckTest {

  @Test
  void shouldBeReadyIfDbPoolInitialized() {
    final DbPool dbPool = Mockito.mock(DbPool.class);

    final DatabaseHealthCheck databaseHealthCheck = new DatabaseHealthCheck(dbPool);

    Assertions.assertTrue(databaseHealthCheck.isReady());
  }

  @Test
  void shouldBeLiveIfDbPoolConnectionOk() throws ServiceException, SQLException {
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

    final DatabaseHealthCheck databaseHealthCheck = new DatabaseHealthCheck(dbPool);

    Assertions.assertTrue(databaseHealthCheck.isLive());
  }


  @Test
  void shouldNotBeLiveIfDbPoolConnectionCannotBeTaken() throws ServiceException, SQLException {
    final DbPool dbPool = Mockito.mock(DbPool.class);
    final ServiceException dbConnectionException = Mockito.mock(ServiceException.class);
    Mockito.when(dbPool.getDatabaseConnection())
        .thenThrow(dbConnectionException);

    final DatabaseHealthCheck databaseHealthCheck = new DatabaseHealthCheck(dbPool);

    Assertions.assertFalse(databaseHealthCheck.isLive());
  }

  @Test
  void shouldNotBeLiveIfPrepareStatementThrowsSqlException() throws ServiceException, SQLException {
    final DbPool dbPool = Mockito.mock(DbPool.class);
    final DbConnection dbConnection = Mockito.mock(DbConnection.class);

    Mockito.when(dbPool.getDatabaseConnection())
        .thenReturn(dbConnection);
    Mockito.when(dbConnection.prepareStatement("SELECT 1")).thenThrow(new SQLException());

    final DatabaseHealthCheck databaseHealthCheck = new DatabaseHealthCheck(dbPool);

    Assertions.assertFalse(databaseHealthCheck.isLive());
  }

}