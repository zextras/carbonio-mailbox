package com.zextras.mailbox.feature;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

public class MailboxSchemaMigration {

  public static final String SCHEMA = "mailbox";

  private final DataSource dataSource;

  public MailboxSchemaMigration(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void migrate() {
    Flyway.configure(getClass().getClassLoader())
        .dataSource(dataSource)
        .schemas(SCHEMA)
        .locations("classpath:db/migration/" + SCHEMA)
        .load()
        .migrate();
  }
}
