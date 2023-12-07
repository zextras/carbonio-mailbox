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
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DatabaseHealthCheckTest {

//  @Container
//  MariaDBContainer mariaDBContainer = new MariaDBContainer().withDatabaseName("zimbra");
//
//  @BeforeEach
//  void beforeEach() {
//    LC.mysql_root_password.setDefault(mariaDBContainer.getPassword());
//    LC.zimbra_mysql_password.setDefault(mariaDBContainer.getPassword());
//    LC.zimbra_mysql_user.setDefault("root");
//    LC.mysql_bind_address.setDefault("127.0.0.1");
//    LC.mysql_port.setDefault(mariaDBContainer.getMappedPort(3306));
//    DbPool.startup();
//  }
//
//  @AfterEach
//  void afterEach() throws Exception {
//    DbPool.shutdown();
//    DbPool.clearConfig();
//  }

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

}