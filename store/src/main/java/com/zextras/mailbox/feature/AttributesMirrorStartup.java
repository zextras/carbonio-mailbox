package com.zextras.mailbox.feature;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zextras.mailbox.client.ServiceDiscoverHttpClient;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.ldap.LdapProvisioning;
import io.vavr.control.Try;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class AttributesMirrorStartup {

  private static final Path SERVICE_DISCOVER_TOKEN =
      Path.of("/etc/carbonio/mailbox/service-discover/token");
  private static final String MAILBOX_SERVICE = "carbonio-mailbox";
  private static final int CONNECT_TIMEOUT_SECONDS = 5;
  private static final int MAXIMUM_POOL_SIZE = 5;
  private static final int MINIMUM_IDLE = 1;

  private final ServiceDiscoverHttpClient serviceDiscover;
  private final String host;
  private final int port;

  public AttributesMirrorStartup(ServiceDiscoverHttpClient serviceDiscover, String host, int port) {
    this.serviceDiscover = serviceDiscover;
    this.host = host;
    this.port = port;
  }

  public static void enableInBackground(LdapProvisioning provisioning) {
    if (!LC.mailbox_features_mirror_enabled.booleanValue()) {
      return;
    }
    Thread startup = new Thread(() -> enable(provisioning), "features-mirror-startup");
    startup.setDaemon(true);
    startup.start();
  }

  private static void enable(LdapProvisioning provisioning) {
    Try.of(() -> Files.readString(SERVICE_DISCOVER_TOKEN).trim())
        .map(
            token ->
                new AttributesMirrorStartup(
                    ServiceDiscoverHttpClient.defaultUrl().withToken(token),
                    LC.mailbox_postgres_host.value(),
                    LC.mailbox_postgres_port.intValue()))
        .flatMap(AttributesMirrorStartup::connect)
        .onSuccess(db -> start(provisioning, db))
        .onFailure(e -> ZimbraLog.misc.warn("Features mirror to Postgres not enabled", e));
  }

  private static void start(LdapProvisioning provisioning, DSLContext db) {
    PostgresAccountAttributes accounts = new PostgresAccountAttributes(db);
    PostgresCosAttributes coses = new PostgresCosAttributes(db);
    PostgresDomainAttributes domains = new PostgresDomainAttributes(db);
    AttributesMirror mirror = new AttributesMirror(accounts, coses, domains);
    provisioning.setAttributesMirror(mirror);
    provisioning.setPostgresEntries(new PostgresEntries(db));
    ZimbraLog.misc.info("Features mirror to Postgres enabled");

    AttributesReconcile reconcile =
        new AttributesReconcile(provisioning, db);
    ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor(
            task -> {
              Thread thread = new Thread(task, "features-reconcile");
              thread.setDaemon(true);
              return thread;
            });
    scheduler.scheduleWithFixedDelay(
        reconcile::runSafely,
        0,
        LC.mailbox_features_reconcile_interval_seconds.intValue(),
        TimeUnit.SECONDS);
  }

  public Try<DSLContext> connect() {
    return dataSource()
        .andThen(dataSource -> new MailboxSchemaMigration(dataSource).migrate())
        .map(dataSource -> DSL.using(dataSource, SQLDialect.POSTGRES));
  }

  private Try<HikariDataSource> dataSource() {
    return Try.of(
        () -> {
          HikariConfig pool = new HikariConfig();
          pool.setPoolName("mailbox-attributes");
          pool.setJdbcUrl("jdbc:postgresql://%s:%d/%s".formatted(host, port, config("db-name")));
          pool.setUsername(config("db-username"));
          pool.setPassword(config("db-password"));
          pool.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
          pool.setMinimumIdle(MINIMUM_IDLE);
          pool.setConnectionTimeout(TimeUnit.SECONDS.toMillis(CONNECT_TIMEOUT_SECONDS));
          return new HikariDataSource(pool);
        });
  }

  private String config(String key) {
    return serviceDiscover.getConfig(MAILBOX_SERVICE, key).get();
  }
}
