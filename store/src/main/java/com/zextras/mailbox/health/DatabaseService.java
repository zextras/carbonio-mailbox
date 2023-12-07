package com.zextras.mailbox.health;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseService extends ServiceDependency {

  private final DbPool dbPool;

  public DatabaseService(String name, ServiceType type, DbPool dbPool) {
    super(name, type);

    this.dbPool = dbPool;
   }


  @Override
  public boolean isReady() {
    return this.canConnectToDatabase();
  }

  private boolean canConnectToDatabase() {
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
  public boolean isLive() {
    return this.canConnectToDatabase();
  }
}
