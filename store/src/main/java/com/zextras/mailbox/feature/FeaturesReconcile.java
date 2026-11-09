package com.zextras.mailbox.feature;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.ldap.LdapProvisioning;
import com.zimbra.cs.ldap.IAttributes;
import com.zimbra.cs.ldap.SearchLdapOptions.SearchLdapVisitor;
import com.zimbra.cs.ldap.ZLdapFilter;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public class FeaturesReconcile {

  private static final long LOCK_KEY = 4706L;
  private static final String[] ATTRIBUTES = {"*", FeaturesMirror.ENTRY_CSN};
  private static final String DOMAIN_NAME = "zimbraDomainName";
  private static final String ZIMBRA_ID = "zimbraId";

  private final LdapProvisioning provisioning;
  private final FeaturesMirror mirror;
  private final DSLContext db;
  private final PostgresAccountFeatures accounts;
  private final PostgresCosFeatures coses;
  private final PostgresDomainDefaultCos domains;

  public FeaturesReconcile(
      LdapProvisioning provisioning,
      FeaturesMirror mirror,
      DSLContext db,
      PostgresAccountFeatures accounts,
      PostgresCosFeatures coses,
      PostgresDomainDefaultCos domains) {
    this.provisioning = provisioning;
    this.mirror = mirror;
    this.db = db;
    this.accounts = accounts;
    this.coses = coses;
    this.domains = domains;
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
          DSLContext locked = DSL.using(connection);
          if (!locked.fetchValue("SELECT pg_try_advisory_lock({0})", LOCK_KEY).equals(true)) {
            ZimbraLog.misc.debug("Features reconcile skipped: another node is running it");
            return;
          }
          try {
            reconcile();
          } finally {
            locked.fetchValue("SELECT pg_advisory_unlock({0})", LOCK_KEY);
          }
        });
  }

  private void reconcile() throws ServiceException {
    OffsetDateTime start = db.fetchValue(DSL.select(DSL.currentOffsetDateTime()));
    Set<String> seenDomains = new HashSet<>();
    Set<String> seenCoses = new HashSet<>();
    Set<String> seenAccounts = new HashSet<>();
    ZLdapFilterFactory filters = ZLdapFilterFactory.getInstance();

    scan(
        provisioning.getDIT().domainBaseDN(),
        filters.allDomains(),
        (dn, attrs) -> {
          String name = (String) attrs.get(DOMAIN_NAME);
          mirror.saveDomain(name, attrs);
          seenDomains.add(name);
        });
    scan(
        provisioning.getDIT().cosBaseDN(),
        filters.allCoses(),
        (dn, attrs) -> {
          String id = (String) attrs.get(ZIMBRA_ID);
          mirror.saveCos(id, attrs);
          seenCoses.add(id);
        });
    scan(
        provisioning.getDIT().mailBranchBaseDN(),
        filters.allAccounts(),
        (dn, attrs) -> {
          String id = (String) attrs.get(ZIMBRA_ID);
          mirror.saveAccount(id, domainOf(dn), attrs);
          seenAccounts.add(id);
        });

    int removed =
        removeUnseen(domains.namesSyncedBefore(start), seenDomains, mirror::deleteDomain)
            + removeUnseen(coses.idsSyncedBefore(start), seenCoses, mirror::deleteCos)
            + removeUnseen(accounts.idsSyncedBefore(start), seenAccounts, mirror::deleteAccount);
    ZimbraLog.misc.info(
        "Features reconcile done: %d domains, %d coses, %d accounts, %d removed",
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
            } catch (Exception e) {
              ZimbraLog.misc.warn("Features reconcile skipped %s", dn, e);
            }
          }
        });
  }

  private String domainOf(String dn) throws ServiceException {
    String email = provisioning.getDIT().dnToEmail(dn, (IAttributes) null);
    return email.substring(email.indexOf('@') + 1);
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
}
