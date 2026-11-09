package com.zextras.mailbox.feature;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.val;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;

public class AccountFeatureQueries {

  private static final String ENABLED = "TRUE";

  private final DSLContext db;

  public AccountFeatureQueries(DSLContext db) {
    this.db = db;
  }

  public int countEnabled(String feature) {
    return db.selectCount()
        .from(PostgresAccountFeatures.ACCOUNT_FEATURES)
        .leftJoin(PostgresCosFeatures.COS_FEATURES)
        .on(PostgresCosFeatures.COS_ID.eq(PostgresAccountFeatures.COS_ID))
        .where(
            coalesce(
                    firstValue(PostgresAccountFeatures.FEATURES, feature),
                    firstValue(PostgresCosFeatures.FEATURES, feature))
                .eq(ENABLED))
        .fetchOne(0, int.class);
  }

  private static Field<String> firstValue(Field<JSONB> features, String feature) {
    return field("{0} -> {1} ->> 0", String.class, features, val(feature));
  }
}
