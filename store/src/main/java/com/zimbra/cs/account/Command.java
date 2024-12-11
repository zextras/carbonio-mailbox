package com.zimbra.cs.account;

import com.zimbra.cs.account.accesscontrol.RightClass;
import com.zimbra.soap.admin.type.CacheEntryType;
import com.zimbra.soap.admin.type.CountObjectsType;

import java.util.HashMap;
import java.util.Map;

public enum Command {
  ADD_ACCOUNT_ALIAS(
      "addAccountAlias", "aaa", "{name@domain|id} {alias@domain}", Category.ACCOUNT, 2, 2),
  ADD_ACCOUNT_LOGGER(
      "addAccountLogger",
      "aal",
      "[-s/--server hostname] {name@domain|id} {logging-category}"
          + " {trace|debug|info|warn|error}",
      Category.LOG,
      3,
      5),
  ADD_DISTRIBUTION_LIST_ALIAS(
      "addDistributionListAlias", "adla", "{list@domain|id} {alias@domain}", Category.LIST, 2, 2),
  ADD_DISTRIBUTION_LIST_MEMBER(
      "addDistributionListMember",
      "adlm",
      "{list@domain|id} {member@domain}+",
      Category.LIST,
      2,
      Integer.MAX_VALUE),
  AUTO_COMPLETE_GAL("autoCompleteGal", "acg", "{domain} {name}", Category.SEARCH, 2, 2),
  AUTO_PROV_CONTROL("autoProvControl", "apc", "{start|status|stop}", Category.COMMANDS, 1, 1),
  CHECK_PASSWORD_STRENGTH(
      "checkPasswordStrength", "cps", "{name@domain|id} {password}", Category.ACCOUNT, 2, 2),
  CHECK_RIGHT(
      "checkRight",
      "ckr",
      "{target-type} [{target-id|target-name}] {grantee-id|grantee-name (note:can only"
          + " check internal user)} {right}",
      Category.RIGHT,
      3,
      4,
      null,
      new RightCommandHelp(false, false, true)),
  COPY_COS("copyCos", "cpc", "{src-cos-name|id} {dest-cos-name}", Category.COS, 2, 2),
  COUNT_ACCOUNT("countAccount", "cta", "{domain|id}", Category.DOMAIN, 1, 1),
  COUNT_OBJECTS(
      "countObjects",
      "cto",
      "{" + CountObjectsType.names("|") + "} [-d {domain|id}]",
      Category.MISC,
      1,
      4),
  CREATE_ACCOUNT(
      "createAccount",
      "ca",
      "{name@domain} {password} [attr1 value1 [attr2 value2...]]",
      Category.ACCOUNT,
      2,
      Integer.MAX_VALUE),
  CREATE_ALIAS_DOMAIN(
      "createAliasDomain",
      "cad",
      "{alias-domain-name} {local-domain-name|id} [attr1 value1 [attr2 value2...]]",
      Category.DOMAIN,
      2,
      Integer.MAX_VALUE),
  CREATE_BULK_ACCOUNTS(
      "createBulkAccounts",
      "cabulk",
      "{domain} {namemask} {number-of-accounts-to-create} {password}",
      Category.MISC,
      4,
      4),
  CREATE_CALENDAR_RESOURCE(
      "createCalendarResource",
      "ccr",
      "{name@domain} {password} [attr1 value1 [attr2 value2...]]",
      Category.CALENDAR,
      2,
      Integer.MAX_VALUE),
  CREATE_COS(
      "createCos",
      "cc",
      "{name} [attr1 value1 [attr2 value2...]]",
      Category.COS,
      1,
      Integer.MAX_VALUE),
  CREATE_DATA_SOURCE(
      "createDataSource",
      "cds",
      "{name@domain} {ds-type} {ds-name} zimbraDataSourceEnabled {TRUE|FALSE}"
          + " zimbraDataSourceFolderId {folder-id} [attr1 value1 [attr2 value2...]]",
      Category.ACCOUNT,
      3,
      Integer.MAX_VALUE),
  CREATE_DISTRIBUTION_LIST(
      "createDistributionList", "cdl", "{list@domain}", Category.LIST, 1, Integer.MAX_VALUE),
  CREATE_DYNAMIC_DISTRIBUTION_LIST(
      "createDynamicDistributionList",
      "cddl",
      "{list@domain}",
      Category.LIST,
      1,
      Integer.MAX_VALUE),
  CREATE_DISTRIBUTION_LISTS_BULK("createDistributionListsBulk", "cdlbulk"),
  CREATE_DOMAIN(
      "createDomain",
      "cd",
      "{domain} [attr1 value1 [attr2 value2...]]",
      Category.DOMAIN,
      1,
      Integer.MAX_VALUE),
  CREATE_SERVER(
      "createServer",
      "cs",
      "{name} [attr1 value1 [attr2 value2...]]",
      Category.SERVER,
      1,
      Integer.MAX_VALUE),
  CREATE_IDENTITY(
      "createIdentity",
      "cid",
      "{name@domain} {identity-name} [attr1 value1 [attr2 value2...]]",
      Category.ACCOUNT,
      2,
      Integer.MAX_VALUE),
  CREATE_SIGNATURE(
      "createSignature",
      "csig",
      "{name@domain} {signature-name} [attr1 value1 [attr2 value2...]]",
      Category.ACCOUNT,
      2,
      Integer.MAX_VALUE),
  DELETE_ACCOUNT("deleteAccount", "da", "{name@domain|id}", Category.ACCOUNT, 1, 1),
  DELETE_CALENDAR_RESOURCE(
      "deleteCalendarResource", "dcr", "{name@domain|id}", Category.CALENDAR, 1, 1),
  DELETE_COS("deleteCos", "dc", "{name|id}", Category.COS, 1, 1),
  DELETE_DATA_SOURCE(
      "deleteDataSource", "dds", "{name@domain|id} {ds-name|ds-id}", Category.ACCOUNT, 2, 2),
  DELETE_DISTRIBUTION_LIST(
      "deleteDistributionList", "ddl", "{list@domain|id} [true|false]", Category.LIST, 1, 2),
  DELETE_DOMAIN("deleteDomain", "dd", "{domain|id}", Category.DOMAIN, 1, 1),
  DELETE_IDENTITY(
      "deleteIdentity", "did", "{name@domain|id} {identity-name}", Category.ACCOUNT, 2, 2),
  DELETE_SIGNATURE(
      "deleteSignature", "dsig", "{name@domain|id} {signature-name}", Category.ACCOUNT, 2, 2),
  DELETE_SERVER("deleteServer", "ds", "{name|id}", Category.SERVER, 1, 1),
  DESCRIBE(
      "describe",
      "desc",
      "[[-v] [-ni] [{entry-type}]] | [-a {attribute-name}]",
      Category.MISC,
      0,
      Integer.MAX_VALUE,
      null,
      null,
      true),
  EXIT("exit", "quit", "", Category.MISC, 0, 0),
  FLUSH_CACHE(
      "flushCache",
      "fc",
      "[-a] {" + CacheEntryType.names() + "|extension-cache-type} [name1|id1 [name2|id2...]]",
      Category.MISC,
      1,
      Integer.MAX_VALUE),
  GENERATE_DOMAIN_PRE_AUTH(
      "generateDomainPreAuth",
      "gdpa",
      "{domain|id} {name|id|foreignPrincipal} {by} {timestamp|0} {expires|0}",
      Category.MISC,
      5,
      6),
  GENERATE_DOMAIN_PRE_AUTH_KEY(
      "generateDomainPreAuthKey", "gdpak", "[-f] {domain|id}", Category.MISC, 1, 2),
  GET_ACCOUNT(
      "getAccount",
      "ga",
      "[-e] {name@domain|id} [attr1 [attr2...]]",
      Category.ACCOUNT,
      1,
      Integer.MAX_VALUE),
  GET_DATA_SOURCES(
      "getDataSources",
      "gds",
      "{name@domain|id} [arg1 [arg2...]]",
      Category.ACCOUNT,
      1,
      Integer.MAX_VALUE),
  GET_IDENTITIES(
      "getIdentities",
      "gid",
      "{name@domain|id} [arg1 [arg...]]",
      Category.ACCOUNT,
      1,
      Integer.MAX_VALUE),
  GET_SIGNATURES(
      "getSignatures",
      "gsig",
      "{name@domain|id} [arg1 [arg...]]",
      Category.ACCOUNT,
      1,
      Integer.MAX_VALUE),
  GET_ACCOUNT_MEMBERSHIP(
      "getAccountMembership", "gam", "{name@domain|id}", Category.ACCOUNT, 1, 2),
  GET_ALL_ACCOUNTS(
      "getAllAccounts",
      "gaa",
      "[-v] [-e] [-s server] [{domain}]",
      Category.ACCOUNT,
      0,
      5,
      Via.ldap),
  GET_ACCOUNT_LOGGERS(
      "getAccountLoggers", "gal", "[-s/--server hostname] {name@domain|id}", Category.LOG, 1, 3),
  GET_ALL_ACCOUNT_LOGGERS(
      "getAllAccountLoggers", "gaal", "[-s/--server hostname]", Category.LOG, 0, 2),
  GET_ALL_ADMIN_ACCOUNTS(
      "getAllAdminAccounts",
      "gaaa",
      "[-v] [-e] [attr1 [attr2...]]",
      Category.ACCOUNT,
      0,
      Integer.MAX_VALUE),
  GET_ALL_CALENDAR_RESOURCES(
      "getAllCalendarResources",
      "gacr",
      "[-v] [-e] [-s server] [{domain}]",
      Category.CALENDAR,
      0,
      5),
  GET_ALL_CONFIG(
      "getAllConfig", "gacf", "[attr1 [attr2...]]", Category.CONFIG, 0, Integer.MAX_VALUE),
  GET_ALL_COS("getAllCos", "gac", "[-v]", Category.COS, 0, 1),
  GET_ALL_DISTRIBUTION_LISTS(
      "getAllDistributionLists", "gadl", "[-v] [{domain}]", Category.LIST, 0, 2),
  GET_ALL_DOMAINS(
      "getAllDomains",
      "gad",
      "[-v] [-e] [attr1 [attr2...]]",
      Category.DOMAIN,
      0,
      Integer.MAX_VALUE),
  GET_ALL_EFFECTIVE_RIGHTS(
      "getAllEffectiveRights",
      "gaer",
      "{grantee-type} {grantee-id|grantee-name} [expandSetAttrs] [expandGetAttrs]",
      Category.RIGHT,
      2,
      4),
  GET_ALL_FREEBUSY_PROVIDERS("getAllFbp", "gafbp", "[-v]", Category.FREEBUSY, 0, 1),
  GET_ALL_RIGHTS(
      "getAllRights",
      "gar",
      "[-v] [-t {target-type}] [-c " + RightClass.allValuesInString("|") + "]",
      Category.RIGHT,
      0,
      5),
  GET_ALL_SERVERS("getAllServers", "gas", "[-v] [-e] [service]", Category.SERVER, 0, 3),
  GET_AUTH_TOKEN_INFO("getAuthTokenInfo", "gati", "{auth-token}", Category.MISC, 1, 1),
  GET_CALENDAR_RESOURCE(
      "getCalendarResource",
      "gcr",
      "{name@domain|id} [attr1 [attr2...]]",
      Category.CALENDAR,
      1,
      Integer.MAX_VALUE),
  GET_CONFIG("getConfig", "gcf", "{name}", Category.CONFIG, 1, 1),
  GET_COS("getCos", "gc", "{name|id} [attr1 [attr2...]]", Category.COS, 1, Integer.MAX_VALUE),
  GET_DISTRIBUTION_LIST(
      "getDistributionList",
      "gdl",
      "{list@domain|id} [attr1 [attr2...]]",
      Category.LIST,
      1,
      Integer.MAX_VALUE),
  GET_DISTRIBUTION_LIST_MEMBERSHIP(
      "getDistributionListMembership", "gdlm", "{name@domain|id}", Category.LIST, 1, 1),
  GET_DOMAIN(
      "getDomain",
      "gd",
      "[-e] {domain|id} [attr1 [attr2...]]",
      Category.DOMAIN,
      1,
      Integer.MAX_VALUE),
  GET_DOMAIN_INFO(
      "getDomainInfo",
      "gdi",
      "name|id|virtualHostname {value} [attr1 [attr2...]]",
      Category.DOMAIN,
      2,
      Integer.MAX_VALUE),
  GET_EFFECTIVE_RIGHTS(
      "getEffectiveRights",
      "ger",
      "{target-type} [{target-id|target-name}] {grantee-id|grantee-name} [expandSetAttrs]"
          + " [expandGetAttrs]",
      Category.RIGHT,
      1,
      5,
      null,
      new RightCommandHelp(false, false, false)),
  // for testing the provisioning interface only, comment out after testing, the soap is only
  // used by admin console
  GET_CREATE_OBJECT_ATTRS(
      "getCreateObjectAttrs",
      "gcoa",
      "{target-type} {domain-id|domain-name} {cos-id|cos-name} {grantee-id|grantee-name}",
      Category.RIGHT,
      3,
      4),
  GET_FREEBUSY_QUEUE_INFO(
      "getFreebusyQueueInfo", "gfbqi", "[{provider-name}]", Category.FREEBUSY, 0, 1),
  GET_GRANTS(
      "getGrants",
      "gg",
      "[-t {target-type} [{target-id|target-name}]] [-g {grantee-type}"
          + " {grantee-id|grantee-name} [{0|1 (whether to include grants granted to"
          + " groups the grantee belongs)}]]",
      Category.RIGHT,
      2,
      7,
      null,
      new RightCommandHelp(false, false, false)),
  GET_MAILBOX_INFO("getMailboxInfo", "gmi", "{account}", Category.MAILBOX, 1, 1),
  GET_QUOTA_USAGE("getQuotaUsage", "gqu", "{server}", Category.MAILBOX, 1, 1),
  GET_RIGHT(
      "getRight",
      "gr",
      "{right} [-e] (whether to expand combo rights recursively)",
      Category.RIGHT,
      1,
      2),
  GET_RIGHTS_DOC("getRightsDoc", "grd", "[java-packages]", Category.RIGHT, 0, Integer.MAX_VALUE),
  GET_SERVER(
      "getServer",
      "gs",
      "[-e] {name|id} [attr1 [attr2...]]",
      Category.SERVER,
      1,
      Integer.MAX_VALUE),
  GET_SHARE_INFO("getShareInfo", "gsi", "{owner-name|owner-id}", Category.SHARE, 1, 1),
  GET_SPNEGO_DOMAIN("getSpnegoDomain", "gsd", "", Category.MISC, 0, 0),
  GRANT_RIGHT(
      "grantRight",
      "grr",
      "{target-type} [{target-id|target-name}] {grantee-type} [{grantee-id|grantee-name}"
          + " [secret]] {right}",
      Category.RIGHT,
      3,
      6,
      null,
      new RightCommandHelp(false, true, true)),
  HELP("help", "?", "commands", Category.MISC, 0, 1),
  LDAP(".ldap", ".l"),
  MODIFY_ACCOUNT(
      "modifyAccount",
      "ma",
      "{name@domain|id} [attr1 value1 [attr2 value2...]]",
      Category.ACCOUNT,
      3,
      Integer.MAX_VALUE),
  MODIFY_CALENDAR_RESOURCE(
      "modifyCalendarResource",
      "mcr",
      "{name@domain|id} [attr1 value1 [attr2 value2...]]",
      Category.CALENDAR,
      3,
      Integer.MAX_VALUE),
  MODIFY_CONFIG(
      "modifyConfig",
      "mcf",
      "attr1 value1 [attr2 value2...]",
      Category.CONFIG,
      2,
      Integer.MAX_VALUE),
  MODIFY_COS(
      "modifyCos",
      "mc",
      "{name|id} [attr1 value1 [attr2 value2...]]",
      Category.COS,
      3,
      Integer.MAX_VALUE),
  MODIFY_DATA_SOURCE(
      "modifyDataSource",
      "mds",
      "{name@domain|id} {ds-name|ds-id} [attr1 value1 [attr2 value2...]]",
      Category.ACCOUNT,
      4,
      Integer.MAX_VALUE),
  MODIFY_DISTRIBUTION_LIST(
      "modifyDistributionList",
      "mdl",
      "{list@domain|id} attr1 value1 [attr2 value2...]",
      Category.LIST,
      3,
      Integer.MAX_VALUE),
  MODIFY_DOMAIN(
      "modifyDomain",
      "md",
      "{domain|id} [attr1 value1 [attr2 value2...]]",
      Category.DOMAIN,
      3,
      Integer.MAX_VALUE),
  MODIFY_IDENTITY(
      "modifyIdentity",
      "mid",
      "{name@domain|id} {identity-name} [attr1 value1 [attr2 value2...]]",
      Category.ACCOUNT,
      4,
      Integer.MAX_VALUE),
  MODIFY_SIGNATURE(
      "modifySignature",
      "msig",
      "{name@domain|id} {signature-name|signature-id} [attr1 value1 [attr2 value2...]]",
      Category.ACCOUNT,
      4,
      Integer.MAX_VALUE),
  MODIFY_SERVER(
      "modifyServer",
      "ms",
      "{name|id} [attr1 value1 [attr2 value2...]]",
      Category.SERVER,
      3,
      Integer.MAX_VALUE),
  PUSH_FREEBUSY(
      "pushFreebusy", "pfb", "[account-id ...]", Category.FREEBUSY, 1, Integer.MAX_VALUE),
  PUSH_FREEBUSY_DOMAIN("pushFreebusyDomain", "pfbd", "{domain}", Category.FREEBUSY, 1, 1),
  PURGE_ACCOUNT_CALENDAR_CACHE(
      "purgeAccountCalendarCache",
      "pacc",
      "{name@domain|id} [...]",
      Category.CALENDAR,
      1,
      Integer.MAX_VALUE),
  PURGE_FREEBUSY_QUEUE(
      "purgeFreebusyQueue", "pfbq", "[{provider-name}]", Category.FREEBUSY, 0, 1),
  RECALCULATE_MAILBOX_COUNTS(
      "recalculateMailboxCounts", "rmc", "{name@domain|id}", Category.MAILBOX, 1, 1),
  REMOVE_ACCOUNT_ALIAS(
      "removeAccountAlias", "raa", "{name@domain|id} {alias@domain}", Category.ACCOUNT, 2, 2),
  REMOVE_ACCOUNT_LOGGER(
      "removeAccountLogger",
      "ral",
      "[-s/--server hostname] [{name@domain|id}] [{logging-category}]",
      Category.LOG,
      0,
      4),
  REMOVE_DISTRIBUTION_LIST_ALIAS(
      "removeDistributionListAlias",
      "rdla",
      "{list@domain|id} {alias@domain}",
      Category.LIST,
      2,
      2),
  REMOVE_DISTRIBUTION_LIST_MEMBER(
      "removeDistributionListMember",
      "rdlm",
      "{list@domain|id} {member@domain}",
      Category.LIST,
      2,
      Integer.MAX_VALUE),
  RENAME_ACCOUNT(
      "renameAccount", "ra", "{name@domain|id} {newName@domain}", Category.ACCOUNT, 2, 2),
  CHANGE_PRIMARY_EMAIL(
      "changePrimaryEmail", "cpe", "{name@domain|id} {newName@domain}", Category.ACCOUNT, 2, 2),
  RENAME_CALENDAR_RESOURCE(
      "renameCalendarResource",
      "rcr",
      "{name@domain|id} {newName@domain}",
      Category.CALENDAR,
      2,
      2),
  RENAME_COS("renameCos", "rc", "{name|id} {newName}", Category.COS, 2, 2),
  RENAME_DISTRIBUTION_LIST(
      "renameDistributionList", "rdl", "{list@domain|id} {newName@domain}", Category.LIST, 2, 2),
  RENAME_DOMAIN("renameDomain", "rd", "{domain|id} {newDomain}", Category.DOMAIN, 2, 2, Via.ldap),
  REINDEX_MAILBOX(
      "reIndexMailbox",
      "rim",
      "{name@domain|id} {start|status|cancel} [{types|ids} {type or id} [,type or" + " id...]]",
      Category.MAILBOX,
      2,
      Integer.MAX_VALUE,
      null,
      new ReindexCommandHelp()),
  COMPACT_INBOX_MAILBOX(
      "compactIndexMailbox",
      "cim",
      "{name@domain|id} {start|status}",
      Category.MAILBOX,
      2,
      Integer.MAX_VALUE),
  VERIFY_INDEX("verifyIndex", "vi", "{name@domain|id}", Category.MAILBOX, 1, 1),
  GET_INDEX_STATS("getIndexStats", "gis", "{name@domain|id}", Category.MAILBOX, 1, 1),
  REVOKE_RIGHT(
      "revokeRight",
      "rvr",
      "{target-type} [{target-id|target-name}] {grantee-type} [{grantee-id|grantee-name}]"
          + " {right}",
      Category.RIGHT,
      3,
      5,
      null,
      new RightCommandHelp(false, false, true)),
  SEARCH_ACCOUNTS(
      "searchAccounts",
      "sa",
      "[-v] {ldap-query} [limit {limit}] [offset {offset}] [sortBy {attr}] [sortAscending"
          + " 0|1*] [domain {domain}]",
      Category.SEARCH,
      1,
      Integer.MAX_VALUE),
  SEARCH_CALENDAR_RESOURCES(
      "searchCalendarResources",
      "scr",
      "[-v] domain attr op value [attr op value...]",
      Category.SEARCH,
      1,
      Integer.MAX_VALUE,
      Via.ldap),
  SEARCH_GAL(
      "searchGal",
      "sg",
      "{domain} {name} [limit {limit}] [offset {offset}] [sortBy {attr}]",
      Category.SEARCH,
      2,
      Integer.MAX_VALUE),
  SELECT_MAILBOX(
      "selectMailbox",
      "sm",
      "{account-name} [{zmmailbox commands}]",
      Category.MAILBOX,
      1,
      Integer.MAX_VALUE),
  SET_ACCOUNT_COS(
      "setAccountCos", "sac", "{name@domain|id} {cos-name|cos-id}", Category.ACCOUNT, 2, 2),
  SET_PASSWORD("setPassword", "sp", "{name@domain|id} {password}", Category.ACCOUNT, 2, 2),
  GET_ALL_MTA_AUTH_URLS("getAllMtaAuthURLs", "gamau", "", Category.SERVER, 0, 0),
  GET_ALL_REVERSE_PROXY_URLS("getAllReverseProxyURLs", "garpu", "", Category.REVERSEPROXY, 0, 0),
  GET_ALL_REVERSE_PROXY_BACKENDS(
      "getAllReverseProxyBackends", "garpb", "", Category.REVERSEPROXY, 0, 0),
  GET_ALL_REVERSE_PROXY_DOMAINS(
      "getAllReverseProxyDomains", "garpd", "", Category.REVERSEPROXY, 0, 0, Via.ldap),
  GET_ALL_MEMCACHED_SERVERS("getAllMemcachedServers", "gamcs", "", Category.SERVER, 0, 0),
  RELOAD_MEMCACHED_CLIENT_CONFIG(
      "reloadMemcachedClientConfig",
      "rmcc",
      "all | mailbox-server [...]",
      Category.MISC,
      1,
      Integer.MAX_VALUE,
      Via.soap),
  GET_MEMCACHED_CLIENT_CONFIG(
      "getMemcachedClientConfig",
      "gmcc",
      "all | mailbox-server [...]",
      Category.MISC,
      1,
      Integer.MAX_VALUE,
      Via.soap),
  SOAP(".soap", ".s"),
  SYNC_GAL("syncGal", "syg", "{domain} [{token}]", Category.MISC, 1, 2),
  RESET_ALL_LOGGERS("resetAllLoggers", "rlog", "[-s/--server hostname]", Category.LOG, 0, 2),
  UNLOCK_MAILBOX(
      "unlockMailbox",
      "ulm",
      "{name@domain|id} [hostname (When unlocking a mailbox after a failed move attempt"
          + " provide the hostname of the server that was the target for the failed move."
          + " Otherwise, do not include hostname parameter)]",
      Category.MAILBOX,
      1,
      2,
      Via.soap);

  private final String mName;
  private final String mAlias;
  private String mHelp;
  private CommandHelp mExtraHelp;
  private Category mCat;
  private int mMinArgLength = 0;
  private int mMaxArgLength = Integer.MAX_VALUE;
  private Via mVia;
  private boolean mNeedsSchemaExtension = false;

  String getArgumentsCountDescription() {
    if (mMinArgLength == mMaxArgLength) {
      return String.valueOf(mMinArgLength);
    } else if (mMaxArgLength == Integer.MAX_VALUE) {
      return String.format("at least %s", mMinArgLength);
    } else if (mMinArgLength <= 0) {
      return String.format("at most %s", mMaxArgLength);
    } else {
      return String.format("%s to %s", mMinArgLength, mMaxArgLength);
    }
  }

  public int getMinArgLength() {
    return mMinArgLength;
  }

  public int getMaxArgLength() {
    return mMaxArgLength;
  }

  public enum Via {
    soap,
    ldap
  }

  static Map<String, Command> getCommandMap() {
    final Map<String, Command> commandMap = new HashMap<>();
    for (Command c : Command.values()) {
      String name = c.getName().toLowerCase();
      if (commandMap.get(name) != null) {
        throw new RuntimeException("duplicate command: " + name);
      }
      String alias = c.getAlias().toLowerCase();
      if (commandMap.get(alias) != null) {
        throw new RuntimeException("duplicate command: " + alias);
      }
      commandMap.put(name, c);
      commandMap.put(alias, c);
    }
    return commandMap;
  }

  public String getName() {
    return mName;
  }

  public String getAlias() {
    return mAlias;
  }

  public String getHelp() {
    return mHelp;
  }

  public CommandHelp getExtraHelp() {
    return mExtraHelp;
  }

  public Category getCategory() {
    return mCat;
  }

  public boolean hasHelp() {
    return mHelp != null;
  }

  public boolean checkArgsLength(String[] args) {
    int len = args == null ? 0 : args.length - 1;
    return len >= mMinArgLength && len <= mMaxArgLength;
  }

  public Via getVia() {
    return mVia;
  }

  public boolean needsSchemaExtension() {
    return mNeedsSchemaExtension || (mCat == Category.RIGHT);
  }

  public boolean isDeprecated() {
    return false; // Used to return true if mCat was Category.NOTEBOOK - which has now been
    // removed
  }

  Command(String name, String alias) {
    mName = name;
    mAlias = alias;
  }

  Command(
      String name, String alias, String help, Category cat, int minArgLength, int maxArgLength) {
    mName = name;
    mAlias = alias;
    mHelp = help;
    mCat = cat;
    mMinArgLength = minArgLength;
    mMaxArgLength = maxArgLength;
  }

  Command(
      String name,
      String alias,
      String help,
      Category cat,
      int minArgLength,
      int maxArgLength,
      Via via) {
    mName = name;
    mAlias = alias;
    mHelp = help;
    mCat = cat;
    mMinArgLength = minArgLength;
    mMaxArgLength = maxArgLength;
    mVia = via;
  }

  Command(
      String name,
      String alias,
      String help,
      Category cat,
      int minArgLength,
      int maxArgLength,
      Via via,
      CommandHelp extraHelp) {
    mName = name;
    mAlias = alias;
    mHelp = help;
    mCat = cat;
    mMinArgLength = minArgLength;
    mMaxArgLength = maxArgLength;
    mVia = via;
    mExtraHelp = extraHelp;
  }

  Command(
      String name,
      String alias,
      String help,
      Category cat,
      int minArgLength,
      int maxArgLength,
      Via via,
      CommandHelp extraHelp,
      boolean needsSchemaExtension) {
    this(name, alias, help, cat, minArgLength, maxArgLength, via, extraHelp);
    mNeedsSchemaExtension = needsSchemaExtension;
  }
}
