package com.zextras.mailbox.feature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.Key;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.auth.AuthContext;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class PostgresReadsIT extends MailboxTestSuite {

  private static final String FROM_POSTGRES = "From Postgres";

  @Container
  private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  private static MirroredAttributes mirrored;

  @BeforeAll
  static void setUp() {
    mirrored = MirroredAttributes.enable(postgres);
    mirrored.reconcile().run();
  }

  @AfterAll
  static void tearDown() {
    MirroredAttributes.disable();
  }

  @BeforeEach
  void readFromPostgres() {
    mirrored.readFromPostgres();
  }

  @AfterEach
  void readFromLdap() {
    MirroredAttributes.readFromLdap();
  }

  @Test
  void getAccountById_ReadsPostgres() throws Exception {
    Account account = createAccount().create();
    overwrite("account_attributes", "account_id", account.getId(), "displayName");

    assertThat(provisioning().getAccountById(account.getId()).getDisplayName())
        .isEqualTo(FROM_POSTGRES);
  }

  @Test
  void getAccountByName_ReadsPostgres() throws Exception {
    Account account = createAccount().create();
    overwrite("account_attributes", "account_id", account.getId(), "displayName");

    assertThat(provisioning().getAccountByName(account.getName()).getDisplayName())
        .isEqualTo(FROM_POSTGRES);
  }

  @Test
  void getAccountByAlias_FindsAccount() throws Exception {
    Account account = createAccount().create();
    String alias = UUID.randomUUID() + "@" + account.getDomainName();
    provisioning().addAlias(account, alias);

    assertThat(provisioning().getAccountByName(alias).getId()).isEqualTo(account.getId());
  }

  @Test
  void getMissingAccount_IsNull() throws Exception {
    assertThat(provisioning().getAccountById(UUID.randomUUID().toString())).isNull();
    assertThat(provisioning().getAccountByName(UUID.randomUUID() + "@test.com")).isNull();
  }

  @Test
  void getCos_ReadsPostgres() throws Exception {
    Cos cos = provisioning().createCos(UUID.randomUUID().toString(), new HashMap<>());
    overwrite("cos_attributes", "cos_id", cos.getId(), "description");

    assertThat(provisioning().getCosById(cos.getId()).getDescription())
        .containsExactly(FROM_POSTGRES);
    assertThat(provisioning().getCosByName(cos.getName()).getDescription())
        .containsExactly(FROM_POSTGRES);
  }

  @Test
  void getDomain_ReadsPostgres() throws Exception {
    Domain domain =
        provisioning().createDomain(UUID.randomUUID() + ".com", new HashMap<>());
    overwrite("domain_attributes", "domain_name", domain.getName(), "description");

    assertThat(provisioning().get(Key.DomainBy.name, domain.getName()).getDescription())
        .containsExactly(FROM_POSTGRES);
    assertThat(provisioning().get(Key.DomainBy.id, domain.getId()).getDescription())
        .containsExactly(FROM_POSTGRES);
  }

  @Test
  void accountFromPostgres_CanBeModified() throws Exception {
    Account account = createAccount().create();

    provisioning()
        .modifyAttrs(
            provisioning().getAccountById(account.getId()),
            new HashMap<>(Map.of("displayName", "New")));

    MirroredAttributes.readFromLdap();
    provisioning().reload(account);
    assertThat(account.getDisplayName()).isEqualTo("New");
  }

  @Test
  void authAccount_UsesPasswordFromPostgres() throws Exception {
    Account account = createAccount().withPassword("secret-123").create();
    Account fromPostgres = provisioning().getAccountById(account.getId());

    provisioning().authAccount(fromPostgres, "secret-123", AuthContext.Protocol.test);
    assertThatThrownBy(
            () -> provisioning().authAccount(fromPostgres, "wrong", AuthContext.Protocol.test))
        .isNotNull();
  }

  @Test
  void flagOff_ReadsLdap() throws Exception {
    Account account = createAccount().create();
    overwrite("account_attributes", "account_id", account.getId(), "displayName");

    MirroredAttributes.readFromLdap();

    assertThat(provisioning().getAccountById(account.getId()).getDisplayName())
        .isNotEqualTo(FROM_POSTGRES);
  }

  private static void overwrite(String table, String keyColumn, String key, String attribute) {
    mirrored.db.execute(
        "UPDATE mailbox."
            + table
            + " SET attributes = jsonb_set(attributes, {0}::text[], jsonb_build_array({1}))"
            + " WHERE "
            + keyColumn
            + " = {2}",
        "{" + attribute + "}",
        FROM_POSTGRES,
        key);
  }

  private static Provisioning provisioning() {
    return Provisioning.getInstance();
  }
}
