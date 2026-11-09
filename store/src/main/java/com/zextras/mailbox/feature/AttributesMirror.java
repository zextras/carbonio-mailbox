package com.zextras.mailbox.feature;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttributesMirror {

  public static final String ENTRY_CSN = "entryCSN";
  private static final String COS_ID = "zimbraCOSId";
  private static final String COS_NAME = "cn";
  private static final String DOMAIN_DEFAULT_COS_ID = "zimbraDomainDefaultCOSId";
  private static final List<String> MIRRORED_PREFIXES = List.of("zimbraFeature", "carbonioFeature");
  private static final Set<String> MIRRORED_ACCOUNT_ATTRIBUTES =
      Set.of(
          "zimbraAccountStatus",
          "zimbraIsSystemAccount",
          "zimbraIsSystemResource",
          "zimbraCalResType",
          "zimbraIsExternalVirtualAccount");

  private final PostgresAccountAttributes accountFeatures;
  private final PostgresCosAttributes cosFeatures;
  private final PostgresDomainDefaultCos domainDefaultCos;

  public AttributesMirror(
      PostgresAccountAttributes accountFeatures,
      PostgresCosAttributes cosFeatures,
      PostgresDomainDefaultCos domainDefaultCos) {
    this.accountFeatures = accountFeatures;
    this.cosFeatures = cosFeatures;
    this.domainDefaultCos = domainDefaultCos;
  }

  public static boolean changesAccountAttributes(Map<String, ?> changes) {
    return changedAttributes(changes).anyMatch(name -> isMirrored(name) || COS_ID.equals(name));
  }

  public static boolean changesCosAttributes(Map<String, ?> changes) {
    return changedAttributes(changes).anyMatch(AttributesMirror::isMirrored);
  }

  public static boolean changesDomainDefaultCos(Map<String, ?> changes) {
    return changedAttributes(changes).anyMatch(DOMAIN_DEFAULT_COS_ID::equals);
  }

  public void saveAccount(String accountId, String domainName, Map<String, Object> ldapAttributes) {
    accountFeatures.save(accountRow(accountId, domainName, ldapAttributes));
  }

  public void saveCos(String cosId, Map<String, Object> ldapAttributes) {
    cosFeatures.save(cosRow(cosId, ldapAttributes));
  }

  public void saveDomain(String domainName, Map<String, Object> ldapAttributes) {
    domainDefaultCos.save(domainRow(domainName, ldapAttributes));
  }

  public static AccountAttributes accountRow(
      String accountId, String domainName, Map<String, Object> ldapAttributes) {
    return new AccountAttributes(
        accountId,
        domainName,
        single(ldapAttributes, COS_ID),
        entryCsn(accountId, ldapAttributes),
        mirrored(ldapAttributes));
  }

  public static CosAttributes cosRow(String cosId, Map<String, Object> ldapAttributes) {
    return new CosAttributes(
        cosId,
        single(ldapAttributes, COS_NAME),
        entryCsn(cosId, ldapAttributes),
        mirrored(ldapAttributes));
  }

  public static DomainDefaultCos domainRow(String domainName, Map<String, Object> ldapAttributes) {
    return new DomainDefaultCos(
        domainName,
        single(ldapAttributes, DOMAIN_DEFAULT_COS_ID),
        entryCsn(domainName, ldapAttributes));
  }

  public void deleteAccount(String accountId) {
    accountFeatures.delete(accountId);
  }

  public void deleteCos(String cosId) {
    cosFeatures.delete(cosId);
  }

  public void deleteDomain(String domainName) {
    domainDefaultCos.delete(domainName);
  }

  private static String entryCsn(String id, Map<String, Object> ldapAttributes) {
    String csn = single(ldapAttributes, ENTRY_CSN);
    if (csn == null) {
      throw new IllegalStateException("No " + ENTRY_CSN + " on entry " + id);
    }
    return csn;
  }

  private static String single(Map<String, Object> ldapAttributes, String name) {
    Object value = ldapAttributes.get(name);
    return value == null ? null : values(value).get(0);
  }

  private static Map<String, List<String>> mirrored(Map<String, Object> ldapAttributes) {
    return ldapAttributes.entrySet().stream()
        .filter(attribute -> isMirrored(attribute.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, attribute -> values(attribute.getValue())));
  }

  private static List<String> values(Object value) {
    return value instanceof String[] many ? Arrays.asList(many) : List.of(value.toString());
  }

  private static Stream<String> changedAttributes(Map<String, ?> changes) {
    return changes.keySet().stream().map(AttributesMirror::withoutOperator);
  }

  private static boolean isMirrored(String attribute) {
    return MIRRORED_ACCOUNT_ATTRIBUTES.contains(attribute)
        || MIRRORED_PREFIXES.stream().anyMatch(attribute::startsWith);
  }

  private static String withoutOperator(String attribute) {
    return attribute.startsWith("+") || attribute.startsWith("-")
        ? attribute.substring(1)
        : attribute;
  }
}
