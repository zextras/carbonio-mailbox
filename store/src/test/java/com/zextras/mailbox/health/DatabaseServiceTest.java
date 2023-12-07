// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.health;

import com.zextras.mailbox.health.ServiceDependency.ServiceType;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DatabaseServiceTest {

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

    final DatabaseService databaseService = new DatabaseService("MariaDB", ServiceType.REQUIRED,
        dbPool);

    Assertions.assertTrue(databaseService.isReady());
    Assertions.assertTrue(databaseService.isLive());
  }


  @Test
  void shouldNotBeLiveAndReadyIfDbPoolConnectionCannotBeTaken() throws ServiceException, SQLException {
    final DbPool dbPool = Mockito.mock(DbPool.class);
    final ServiceException dbConnectionException = Mockito.mock(ServiceException.class);
    Mockito.when(dbPool.getDatabaseConnection())
        .thenThrow(dbConnectionException);

    final DatabaseService databaseService = new DatabaseService("MariaDB", ServiceType.REQUIRED,
        dbPool);

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

    final DatabaseService databaseService = new DatabaseService("MariaDB", ServiceType.REQUIRED,
        dbPool);

    Assertions.assertFalse(databaseService.isReady());
    Assertions.assertFalse(databaseService.isLive());
  }

}