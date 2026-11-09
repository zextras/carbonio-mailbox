package com.zextras.mailbox.feature;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.ldap.LdapProvisioning;
import com.zimbra.cs.ldap.IAttributes;
import com.zimbra.cs.ldap.SearchLdapOptions.SearchLdapVisitor;
import com.zimbra.cs.ldap.ZLdapFilter;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

public class AttributesReconcile {

  private static final long LOCK_KEY = 4706L;
  private static final int BATCH_SIZE = 500;
  private static final String[] ATTRIBUTES = {"*", AttributesMirror.ENTRY_CSN};
  private static final String DOMAIN_NAME = "zimbraDomainName";
  private static final String ZIMBRA_ID = "zimbraId";

  private final LdapProvisioning provisioning;
  private final DSLContext db;

  public AttributesReconcile(LdapProvisioning provisioning, DSLContext db) {
    this.provisioning = provisioning;
    this.db = db;
  }

  public void runSafely() {
    try {
      run();
    } catch (Exception e) {
      ZimbraLog.misc.warn("Features reconcile failed", e);
    }
  }

  public void run() {
    db.connection(
        connection -> {
          DSLContext held = DSL.using(connection, SQLDialect.POSTGRES);
          if (!held.fetchValue("SELECT pg_try_advisory_lock({0})", LOCK_KEY).equals(true)) {
            ZimbraLog.misc.debug("Features reconcile skipped: another node is running it");
            return;
          }
          try {
            reconcile(held);
          } finally {
            held.fetchValue("SELECT pg_advisory_unlock({0})", LOCK_KEY);
          }
        });
  }

  private void reconcile(DSLContext held) throws ServiceException {
    long started = System.currentTimeMillis();
    PostgresAccountAttributes accounts = new PostgresAccountAttributes(held);
    PostgresCosAttributes coses = new PostgresCosAttributes(held);
    PostgresDomainDefaultCos domains = new PostgresDomainDefaultCos(held);
    OffsetDateTime start = held.fetchValue(DSL.select(DSL.currentOffsetDateTime()));
    Set<String> seenDomains = new HashSet<>();
    Set<String> seenCoses = new HashSet<>();
    Set<String> seenAccounts = new HashSet<>();
    ZLdapFilterFactory filters = ZLdapFilterFactory.getInstance();

    Batch<DomainDefaultCos> domainBatch = new Batch<>(domains::saveAll);
    scan(
        provisioning.getDIT().domainBaseDN(),
        filters.allDomains(),
        (dn, attrs) -> {
          String name = (String) attrs.get(DOMAIN_NAME);
          domainBatch.add(AttributesMirror.domainRow(name, attrs));
          seenDomains.add(name);
        });
    domainBatch.flush();

    Batch<CosAttributes> cosBatch = new Batch<>(coses::saveAll);
    scan(
        provisioning.getDIT().cosBaseDN(),
        filters.allCoses(),
        (dn, attrs) -> {
          String id = (String) attrs.get(ZIMBRA_ID);
          cosBatch.add(AttributesMirror.cosRow(id, attrs));
          seenCoses.add(id);
        });
    cosBatch.flush();

    Batch<AccountAttributes> accountBatch = new Batch<>(accounts::saveAll);
    scan(
        provisioning.getDIT().mailBranchBaseDN(),
        filters.allAccounts(),
        (dn, attrs) -> {
          String email = provisioning.getDIT().dnToEmail(dn, (IAttributes) null);
          if (!email.contains("@")) {
            return;
          }
          String id = (String) attrs.get(ZIMBRA_ID);
          accountBatch.add(
              AttributesMirror.accountRow(id, email.substring(email.indexOf('@') + 1), attrs));
          seenAccounts.add(id);
        });
    accountBatch.flush();

    int removed =
        removeUnseen(domains.namesSyncedBefore(start), seenDomains, domains::delete)
            + removeUnseen(coses.idsSyncedBefore(start), seenCoses, coses::delete)
            + removeUnseen(accounts.idsSyncedBefore(start), seenAccounts, accounts::delete);
    ZimbraLog.misc.info(
        "Features reconcile done in %d ms: %d domains, %d coses, %d accounts, %d removed",
        System.currentTimeMillis() - started,
        seenDomains.size(),
        seenCoses.size(),
        seenAccounts.size(),
        removed);
  }

  private void scan(String base, ZLdapFilter filter, EntryVisitor visitor)
      throws ServiceException {
    provisioning.searchLdapOnReplica(
        base,
        filter,
        ATTRIBUTES,
        new SearchLdapVisitor() {
          @Override
          public void visit(String dn, Map<String, Object> attrs, IAttributes ldapAttrs) {
            try {
              visitor.visit(dn, attrs);
            } catch (DataAccessException e) {
              throw e;
            } catch (Exception e) {
              ZimbraLog.misc.warn("Features reconcile skipped %s", dn, e);
            }
          }
        });
  }

  private static int removeUnseen(List<String> stale, Set<String> seen, Consumer<String> delete) {
    int removed = 0;
    for (String id : stale) {
      if (!seen.contains(id)) {
        delete.accept(id);
        removed++;
      }
    }
    return removed;
  }

  @FunctionalInterface
  private interface EntryVisitor {
    void visit(String dn, Map<String, Object> attrs) throws Exception;
  }

  private static final class Batch<T> {

    private final Consumer<List<T>> sink;
    private final List<T> rows = new ArrayList<>(BATCH_SIZE);

    private Batch(Consumer<List<T>> sink) {
      this.sink = sink;
    }

    private void add(T row) {
      rows.add(row);
      if (rows.size() >= BATCH_SIZE) {
        flush();
      }
    }

    private void flush() {
      sink.accept(List.copyOf(rows));
      rows.clear();
    }
  }
}
