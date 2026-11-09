package com.zextras.mailbox.feature;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccountFeaturesMirror {

  public static final String ENTRY_CSN = "entryCSN";
  private static final String FEATURE_PREFIX = "zimbraFeature";

  private final PostgresAccountFeatures accountFeatures;

  public AccountFeaturesMirror(PostgresAccountFeatures accountFeatures) {
    this.accountFeatures = accountFeatures;
  }

  public static boolean changesFeatures(Map<String, ?> changes) {
    return changes.keySet().stream()
        .map(AccountFeaturesMirror::withoutOperator)
        .anyMatch(AccountFeaturesMirror::isFeature);
  }

  public void save(String accountId, Map<String, Object> ldapAttributes) {
    Object csn = ldapAttributes.get(ENTRY_CSN);
    if (csn == null) {
      throw new IllegalStateException("No " + ENTRY_CSN + " on account " + accountId);
    }
    accountFeatures.save(new AccountFeatures(accountId, csn.toString(), features(ldapAttributes)));
  }

  private static Map<String, List<String>> features(Map<String, Object> ldapAttributes) {
    return ldapAttributes.entrySet().stream()
        .filter(attribute -> isFeature(attribute.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, attribute -> values(attribute.getValue())));
  }

  private static List<String> values(Object value) {
    return value instanceof String[] many ? Arrays.asList(many) : List.of(value.toString());
  }

  private static boolean isFeature(String attribute) {
    return attribute.startsWith(FEATURE_PREFIX);
  }

  private static String withoutOperator(String attribute) {
    return attribute.startsWith("+") || attribute.startsWith("-") ? attribute.substring(1) : attribute;
  }
}
