package com.zextras.mailbox.feature;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttributesMirror {

  public static final String ENTRY_CSN = "entryCSN";
  private static final String COS_ID = "zimbraCOSId";
  private static final String COS_NAME = "cn";
  private static final String DOMAIN_DEFAULT_COS_ID = "zimbraDomainDefaultCOSId";

  private final PostgresAccountAttributes accounts;
  private final PostgresCosAttributes coses;
  private final PostgresDomainAttributes domains;

  public AttributesMirror(
      PostgresAccountAttributes accounts,
      PostgresCosAttributes coses,
      PostgresDomainAttributes domains) {
    this.accounts = accounts;
    this.coses = coses;
    this.domains = domains;
  }

  public void saveAccount(
      String accountId, String dn, String domainName, Map<String, Object> ldapAttributes) {
    accounts.save(accountRow(accountId, dn, domainName, ldapAttributes));
  }

  public void saveCos(String cosId, String dn, Map<String, Object> ldapAttributes) {
    coses.save(cosRow(cosId, dn, ldapAttributes));
  }

  public void saveDomain(String domainName, String dn, Map<String, Object> ldapAttributes) {
    domains.save(domainRow(domainName, dn, ldapAttributes));
  }

  public static AccountAttributes accountRow(
      String accountId, String dn, String domainName, Map<String, Object> ldapAttributes) {
    return new AccountAttributes(
        accountId,
        dn,
        domainName,
        single(ldapAttributes, COS_ID),
        entryCsn(accountId, ldapAttributes),
        mirrored(ldapAttributes));
  }

  public static CosAttributes cosRow(String cosId, String dn, Map<String, Object> ldapAttributes) {
    return new CosAttributes(
        cosId,
        dn,
        single(ldapAttributes, COS_NAME),
        entryCsn(cosId, ldapAttributes),
        mirrored(ldapAttributes));
  }

  public static DomainAttributes domainRow(
      String domainName, String dn, Map<String, Object> ldapAttributes) {
    return new DomainAttributes(
        domainName,
        dn,
        single(ldapAttributes, DOMAIN_DEFAULT_COS_ID),
        entryCsn(domainName, ldapAttributes),
        mirrored(ldapAttributes));
  }

  public void deleteAccount(String accountId) {
    accounts.delete(accountId);
  }

  public void deleteCos(String cosId) {
    coses.delete(cosId);
  }

  public void deleteDomain(String domainName) {
    domains.delete(domainName);
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
        .filter(attribute -> !ENTRY_CSN.equals(attribute.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, attribute -> values(attribute.getValue())));
  }

  private static List<String> values(Object value) {
    return value instanceof String[] many ? Arrays.asList(many) : List.of(value.toString());
  }
}
