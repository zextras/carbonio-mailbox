package com.zextras.mailbox.feature;

import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeManager;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;

public class AccountFeatureQueries {

  private static final String DEFAULT_COS_NAME = "default";

  private static final String ACCOUNTS_WITH_EFFECTIVE_VALUE =
      """
      FROM mailbox.account_features account
      LEFT JOIN mailbox.cos_features own_cos ON own_cos.cos_id = account.cos_id
      LEFT JOIN mailbox.domain_default_cos domain ON domain.domain_name = account.domain_name
      LEFT JOIN mailbox.cos_features domain_cos ON domain_cos.cos_id = domain.default_cos_id
      LEFT JOIN mailbox.cos_features default_cos ON default_cos.name = {0}
      """;

  private static final String EFFECTIVE_VALUE =
      """
      COALESCE(
        account.features -> {1} ->> 0,
        CASE
          WHEN own_cos.cos_id IS NOT NULL THEN own_cos.features -> {1} ->> 0
          WHEN domain_cos.cos_id IS NOT NULL THEN domain_cos.features -> {1} ->> 0
          ELSE default_cos.features -> {1} ->> 0
        END,
        CAST({2} AS TEXT))
      """;

  private final DSLContext db;
  private final AttributeManager attributes;

  public AccountFeatureQueries(DSLContext db, AttributeManager attributes) {
    this.db = db;
    this.attributes = attributes;
  }

  public int countEnabled(String feature) {
    return db.fetchOne(
            "SELECT count(*) "
                + ACCOUNTS_WITH_EFFECTIVE_VALUE
                + " WHERE "
                + EFFECTIVE_VALUE
                + " = 'TRUE'",
            DEFAULT_COS_NAME,
            feature,
            defaultValue(feature))
        .get(0, Integer.class);
  }

  public Optional<String> effectiveValue(String accountId, String feature) {
    return db.fetchOptional(
            "SELECT "
                + EFFECTIVE_VALUE
                + " "
                + ACCOUNTS_WITH_EFFECTIVE_VALUE
                + " WHERE account.account_id = {3}",
            DEFAULT_COS_NAME,
            feature,
            defaultValue(feature),
            accountId)
        .map(row -> row.get(0, String.class));
  }

  private String defaultValue(String feature) {
    AttributeInfo info = attributes.getAttributeInfo(feature);
    List<String> defaults = info == null ? List.of() : info.getDefaultCosValues();
    return defaults == null || defaults.isEmpty() ? null : defaults.get(0);
  }
}
