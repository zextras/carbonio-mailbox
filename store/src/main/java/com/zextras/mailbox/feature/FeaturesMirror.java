package com.zextras.mailbox.feature;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FeaturesMirror {

  public static final String ENTRY_CSN = "entryCSN";
  private static final String COS_ID = "zimbraCOSId";
  private static final String FEATURE_PREFIX = "zimbraFeature";

  private final PostgresAccountFeatures accountFeatures;
  private final PostgresCosFeatures cosFeatures;

  public FeaturesMirror(PostgresAccountFeatures accountFeatures, PostgresCosFeatures cosFeatures) {
    this.accountFeatures = accountFeatures;
    this.cosFeatures = cosFeatures;
  }

  public static boolean changesAccountFeatures(Map<String, ?> changes) {
    return changedAttributes(changes).anyMatch(name -> isFeature(name) || COS_ID.equals(name));
  }

  public static boolean changesCosFeatures(Map<String, ?> changes) {
    return changedAttributes(changes).anyMatch(FeaturesMirror::isFeature);
  }

  public void saveAccount(String accountId, Map<String, Object> ldapAttributes) {
    Object cosId = ldapAttributes.get(COS_ID);
    accountFeatures.save(
        new AccountFeatures(
            accountId,
            cosId == null ? null : cosId.toString(),
            entryCsn(accountId, ldapAttributes),
            features(ldapAttributes)));
  }

  public void saveCos(String cosId, Map<String, Object> ldapAttributes) {
    cosFeatures.save(
        new CosFeatures(cosId, entryCsn(cosId, ldapAttributes), features(ldapAttributes)));
  }

  private static String entryCsn(String id, Map<String, Object> ldapAttributes) {
    Object csn = ldapAttributes.get(ENTRY_CSN);
    if (csn == null) {
      throw new IllegalStateException("No " + ENTRY_CSN + " on entry " + id);
    }
    return csn.toString();
  }

  private static Map<String, List<String>> features(Map<String, Object> ldapAttributes) {
    return ldapAttributes.entrySet().stream()
        .filter(attribute -> isFeature(attribute.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, attribute -> values(attribute.getValue())));
  }

  private static List<String> values(Object value) {
    return value instanceof String[] many ? Arrays.asList(many) : List.of(value.toString());
  }

  private static Stream<String> changedAttributes(Map<String, ?> changes) {
    return changes.keySet().stream().map(FeaturesMirror::withoutOperator);
  }

  private static boolean isFeature(String attribute) {
    return attribute.startsWith(FEATURE_PREFIX);
  }

  private static String withoutOperator(String attribute) {
    return attribute.startsWith("+") || attribute.startsWith("-")
        ? attribute.substring(1)
        : attribute;
  }
}
