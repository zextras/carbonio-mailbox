package com.zextras.mailbox.feature;

import org.jooq.DSLContext;
import org.jooq.Record;

public class SeatCountQuery {

  public static final String SYNC = "zimbraFeatureMobileSyncEnabled";
  public static final String FILES = "carbonioFeatureFilesEnabled";
  public static final String MAILS_APP = "carbonioFeatureMailsAppEnabled";
  public static final String WSC = "carbonioFeatureWscEnabled";

  private static final String ZIMBRA_USER_ID = "e0fafd89-1360-11d9-8661-000a95d98ef2";
  private static final String DEFAULT_COS_NAME = "default";

  private static final String SEATS =
      """
      WITH seat AS (
        SELECT
          account.attributes AS own,
          CASE
            WHEN own_cos.cos_id IS NOT NULL THEN own_cos.attributes
            WHEN domain_cos.cos_id IS NOT NULL THEN domain_cos.attributes
            ELSE default_cos.attributes
          END AS cos
        FROM mailbox.account_attributes account
        LEFT JOIN mailbox.cos_attributes own_cos ON own_cos.cos_id = account.cos_id
        LEFT JOIN mailbox.domain_attributes domain ON domain.domain_name = account.domain_name
        LEFT JOIN mailbox.cos_attributes domain_cos ON domain_cos.cos_id = domain.default_cos_id
        LEFT JOIN mailbox.cos_attributes default_cos ON default_cos.name = {0}
        WHERE account.account_id <> {1}
          AND COALESCE(account.attributes -> 'zimbraIsSystemAccount' ->> 0, '') <> 'TRUE'
          AND COALESCE(account.attributes -> 'zimbraIsSystemResource' ->> 0, '') <> 'TRUE'
          AND COALESCE(account.attributes -> 'zimbraIsExternalVirtualAccount' ->> 0, '') <> 'TRUE'
          AND account.attributes -> 'zimbraCalResType' IS NULL
      )
      SELECT
        count(*) FILTER (
          WHERE lower(COALESCE(own -> 'zimbraAccountStatus' ->> 0, '')) <> 'closed'),
        count(*) FILTER (WHERE COALESCE(own -> {2} ->> 0, cos -> {2} ->> 0, {3}) = 'TRUE'),
        count(*) FILTER (WHERE COALESCE(own -> {4} ->> 0, cos -> {4} ->> 0, {5}) = 'TRUE'),
        count(*) FILTER (WHERE COALESCE(own -> {6} ->> 0, cos -> {6} ->> 0, {7}) = 'TRUE'),
        count(*) FILTER (WHERE COALESCE(own -> {8} ->> 0, cos -> {8} ->> 0, {9}) = 'TRUE')
      FROM seat
      """;

  private final DSLContext db;

  public SeatCountQuery(DSLContext db) {
    this.db = db;
  }

  public SeatCounts count(GlobalDefaults defaults) {
    Record row =
        db.fetchOne(
            SEATS,
            DEFAULT_COS_NAME,
            ZIMBRA_USER_ID,
            SYNC,
            text(defaults.sync()),
            FILES,
            text(defaults.files()),
            MAILS_APP,
            text(defaults.mailsApp()),
            WSC,
            text(defaults.wsc()));
    return new SeatCounts(
        row.get(0, Integer.class),
        row.get(1, Integer.class),
        row.get(2, Integer.class),
        row.get(3, Integer.class),
        row.get(4, Integer.class));
  }

  private static String text(boolean value) {
    return value ? "TRUE" : "FALSE";
  }

  public record GlobalDefaults(boolean sync, boolean files, boolean mailsApp, boolean wsc) {}
}
