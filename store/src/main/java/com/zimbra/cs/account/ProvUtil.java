// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.ZAttrProvisioning.AccountStatus;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.net.SocketFactories;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.BackupConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.common.soap.SoapHttpTransport.HttpDebugListener;
import com.zimbra.common.soap.SoapTransport;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.CliUtil;
import com.zimbra.common.util.DateUtil;
import com.zimbra.common.util.FileUtil;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.zclient.ZClientException;
import com.zimbra.cs.account.Provisioning.MailMode;
import com.zimbra.cs.account.Provisioning.RightsDoc;
import com.zimbra.cs.account.Provisioning.SearchGalResult;
import com.zimbra.cs.account.Provisioning.SetPasswordResult;
import com.zimbra.cs.account.SearchAccountsOptions.IncludeType;
import com.zimbra.cs.account.SearchDirectoryOptions.MakeObjectOpt;
import com.zimbra.cs.account.SearchDirectoryOptions.ObjectType;
import com.zimbra.cs.account.SearchDirectoryOptions.SortOpt;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.AttrRight;
import com.zimbra.cs.account.accesscontrol.ComboRight;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Help;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.Right.RightType;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.cs.account.commands.AddAccountAliasCommandHandler;
import com.zimbra.cs.account.commands.AddAccountLoggerCommandHandler;
import com.zimbra.cs.account.commands.AutoCompleteGalCommandHandler;
import com.zimbra.cs.account.commands.AutoProvControlCommandHandler;
import com.zimbra.cs.account.commands.ChangePrimaryEmailCommandHandler;
import com.zimbra.cs.account.commands.CheckRightCommandHandler;
import com.zimbra.cs.account.commands.CopyCosCommandHandler;
import com.zimbra.cs.account.commands.CountAccountCommandHandler;
import com.zimbra.cs.account.commands.CountObjectsCommandHandler;
import com.zimbra.cs.account.commands.CreateAccountCommandHandler;
import com.zimbra.cs.account.commands.CreateAliasDomainCommandHandler;
import com.zimbra.cs.account.commands.CreateCosCommandHandler;
import com.zimbra.cs.account.commands.CreateDataSourceCommandHandler;
import com.zimbra.cs.account.commands.CreateDomainCommandHandler;
import com.zimbra.cs.account.commands.CreateIdentityCommandHandler;
import com.zimbra.cs.account.commands.CreateServerCommandHandler;
import com.zimbra.cs.account.commands.CreateSignatureCommandHandler;
import com.zimbra.cs.account.commands.CreateXMPPComponentCommandHandler;
import com.zimbra.cs.account.commands.DescribeCommandHandler;
import com.zimbra.cs.account.commands.ExitCommandHandler;
import com.zimbra.cs.account.commands.FlushCacheCommandHandler;
import com.zimbra.cs.account.commands.GenerateDomainPreAuthCommandHandler;
import com.zimbra.cs.account.commands.GenerateDomainPreAuthKeyCommandHandler;
import com.zimbra.cs.account.commands.GetAccountCommandHandler;
import com.zimbra.cs.account.commands.GetAccountLoggersCommandHandler;
import com.zimbra.cs.account.commands.GetAccountMembershipCommandHandler;
import com.zimbra.cs.account.commands.GetAllAccountLoggersCommandHandler;
import com.zimbra.cs.account.commands.GetAllAccountsCommandHandler;
import com.zimbra.cs.account.commands.GetAllAdminAccountsCommandHandler;
import com.zimbra.cs.account.commands.GetAllConfigCommandHandler;
import com.zimbra.cs.account.commands.GetAllCosCommandHandler;
import com.zimbra.cs.account.commands.GetAllDomainsCommandHandler;
import com.zimbra.cs.account.commands.GetAllEffectiveRightsCommandHandler;
import com.zimbra.cs.account.commands.GetAllFbpCommandHandler;
import com.zimbra.cs.account.commands.GetAllRightsCommandHandler;
import com.zimbra.cs.account.commands.GetAllServersCommandHandler;
import com.zimbra.cs.account.commands.GetConfigCommandHandler;
import com.zimbra.cs.account.commands.GetCosCommandHandler;
import com.zimbra.cs.account.commands.GetCreateObjectAttrsCommandHandler;
import com.zimbra.cs.account.commands.GetDataSourcesCommandHandler;
import com.zimbra.cs.account.commands.GetDistributionListMembershipCommandHandler;
import com.zimbra.cs.account.commands.GetDomainCommandHandler;
import com.zimbra.cs.account.commands.GetDomainInfoCommandHandler;
import com.zimbra.cs.account.commands.GetEffectiveRightsCommandHandler;
import com.zimbra.cs.account.commands.GetFreebusyQueueInfoCommandHandler;
import com.zimbra.cs.account.commands.GetGrantsCommandHandler;
import com.zimbra.cs.account.commands.GetIdentitiesCommandHandler;
import com.zimbra.cs.account.commands.GetRightCommandHandler;
import com.zimbra.cs.account.commands.GetRightsDocCommandHandler;
import com.zimbra.cs.account.commands.GetServerCommandHandler;
import com.zimbra.cs.account.commands.GetSignaturesCommandHandler;
import com.zimbra.cs.account.commands.GetXMPPComponentCommandHandler;
import com.zimbra.cs.account.commands.GrantRightCommandHandler;
import com.zimbra.cs.account.commands.HelpCommandHandler;
import com.zimbra.cs.account.commands.ModifyAccountCommandHandler;
import com.zimbra.cs.account.commands.ModifyDataSourceCommandHandler;
import com.zimbra.cs.account.commands.ModifyIdentityCommandHandler;
import com.zimbra.cs.account.commands.ModifySignatureCommandHandler;
import com.zimbra.cs.account.commands.RevokeRightCommandHandler;
import com.zimbra.cs.account.ldap.LdapEntrySearchFilter;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.account.soap.SoapProvisioning.IndexStatsInfo;
import com.zimbra.cs.account.soap.SoapProvisioning.MailboxInfo;
import com.zimbra.cs.account.soap.SoapProvisioning.MemcachedClientConfig;
import com.zimbra.cs.account.soap.SoapProvisioning.QuotaUsage;
import com.zimbra.cs.account.soap.SoapProvisioning.ReIndexBy;
import com.zimbra.cs.account.soap.SoapProvisioning.ReIndexInfo;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.fb.FbCli;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.ldap.ZLdapFilterFactory.FilterId;
import com.zimbra.cs.util.BuildInfo;
import com.zimbra.cs.util.SoapCLI;
import com.zimbra.cs.zclient.ZMailboxUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.admin.message.LockoutMailboxRequest;
import com.zimbra.soap.admin.message.UnregisterMailboxMoveOutRequest;
import com.zimbra.soap.admin.type.GranteeSelector.GranteeBy;
import com.zimbra.soap.admin.type.MailboxMoveSpec;
import com.zimbra.soap.type.AccountNameSelector;
import com.zimbra.soap.type.GalSearchType;
import com.zimbra.soap.type.TargetBy;
import net.spy.memcached.DefaultHashAlgorithm;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author schemers
 */
public class ProvUtil implements HttpDebugListener {

  private static final String ERR_VIA_SOAP_ONLY = "can only be used with SOAP";
  private static final String ERR_VIA_LDAP_ONLY = "can only be used with  \"zmprov -l/--ldap\"";
  public static final String ERR_INVALID_ARG_EV = "arg -e is invalid unless -v is also specified";
  private final Console console;
  enum SoapDebugLevel {
    none, // no SOAP debug
    normal, // SOAP request and response payload
    high // SOAP payload and http transport header
  }

  private boolean batchMode = false;
  private boolean interactiveMode = false;
  private boolean verboseMode = false;
  private SoapDebugLevel debugLevel = SoapDebugLevel.none;
  private boolean useLdap = LC.zimbra_zmprov_default_to_ldap.booleanValue();
  private boolean useLdapMaster = false;
  private String account = null;
  private String password = null;
  private ZAuthToken authToken = null;
  private String serverHostname = LC.zimbra_zmprov_default_soap_server.value();
  private int serverPort = LC.zimbra_admin_service_port.intValue();
  private Command command;
  private final Map<String, Command> commandIndex;
  private Provisioning prov;
  private BufferedReader cliReader;
  private boolean outputBinaryToFile;
  private boolean allowMultiValuedAttrReplacement;
  private long sendStart;
  private boolean forceDisplayAttrValue;
  private Map<Command, CommandHandler> handlersMap;

  private boolean errorOccursDuringInteraction = false; // bug 58554

  public void setDebug(SoapDebugLevel value) {
    debugLevel = value;
  }

  public void setVerbose(boolean value) {
    verboseMode = value;
  }

  public void setUseLdap(boolean ldap, boolean master) {
    useLdap = ldap;
    useLdapMaster = master;
  }

  public void setAccount(String value) {
    account = value;
    useLdap = false;
  }

  public void setPassword(String value) {
    password = value;
    useLdap = false;
  }

  public void setAuthToken(ZAuthToken value) {
    authToken = value;
    useLdap = false;
  }

  private void setOutputBinaryToFile(boolean value) {
    outputBinaryToFile = value;
  }

  private void setBatchMode(boolean value) {
    batchMode = value;
  }

  private void setAllowMultiValuedAttrReplacement(boolean value) {
    allowMultiValuedAttrReplacement = value;
  }

  private boolean outputBinaryToFile() {
    return outputBinaryToFile;
  }

  private void setForceDisplayAttrValue(boolean value) {
    this.forceDisplayAttrValue = value;
  }

  public void setServer(String value) {
    int i = value.indexOf(":");
    if (i == -1) {
      serverHostname = value;
    } else {
      serverHostname = value.substring(0, i);
      serverPort = Integer.parseInt(value.substring(i + 1));
    }
    useLdap = false;
  }

  public boolean useLdap() {
    return useLdap;
  }

  private void deprecated() {
    console.println("This command has been deprecated.");
    System.exit(1);
  }

  public void usage() {
    usage(null);
  }

  private void usage(Command.Via violatedVia) {
    boolean givenHelp = false;
    if (command != null) {
      if (violatedVia == null) {
        console.println( String.format(
            "usage:  %s(%s) %s\n", command.getName(), command.getAlias(), command.getHelp()));
        givenHelp = true;
        CommandHelp extraHelp = command.getExtraHelp();
        if (extraHelp != null) {
          console.println(extraHelp.getExtraHelp());
        }
      } else {
        if (violatedVia == Command.Via.ldap) {
          console.println(String.format("%s %s\n", command.getName(), ERR_VIA_LDAP_ONLY));
        } else {
          console.println(String.format("%s %s\n", command.getName(), ERR_VIA_SOAP_ONLY));
        }
      }
    }
    if (interactiveMode) {
      return;
    }
    if (givenHelp) {
      console.println("For general help, type : zmprov --help");
      System.exit(1);
    }
    console.println("");
    console.println("zmprov [args] [cmd] [cmd-args ...]");
    console.println("");
    console.println("  -h/--help                             display usage");
    console.println("  -f/--file                             use file as input stream");
    console.println("  -s/--server   {host}[:{port}]         server hostname and optional port");
    console.println("  -l/--ldap                             provision via LDAP instead of SOAP");
    console.println(
        "  -L/--logpropertyfile                  log4j property file, valid only with -l");
    console.println("  -a/--account  {name}                  account name to auth as");
    console.println("  -p/--password {pass}                  password for account");
    console.println("  -P/--passfile {file}                  read password from file");
    console.println(
        "  -z/--zadmin                           use zimbra admin name/password from"
            + " localconfig for admin/password");
    console.println(
        "  -y/--authtoken {authtoken}            " + SoapCLI.OPT_AUTHTOKEN.getDescription());
    console.println(
        "  -Y/--authtokenfile {authtoken file}   " + SoapCLI.OPT_AUTHTOKENFILE.getDescription());
    console.println(
        "  -v/--verbose                          verbose mode (dumps full exception stack"
            + " trace)");
    console.println("  -d/--debug                            debug mode (dumps SOAP messages)");
    console.println("  -m/--master                           use LDAP master (only valid with -l)");
    console.println(
        "  -r/--replace                          allow replacement of safe-guarded"
            + " multi-valued attributes configured in localconfig key"
            + " \"zmprov_safeguarded_attrs\"");
    console.println("");
    doHelp(null);
    System.exit(1);
  }

  private Command lookupCommand(String command) {
    return commandIndex.get(command.toLowerCase());
  }

  /**
   * Commands that should always use LdapProv, but for convenience don't require the -l option
   * specified.
   *
   * <p>Commands that must use -l (e.g. gaa) are indicated in the Via field of the command
   * definition
   */
  private boolean forceLdapButDontRequireUseLdapOption(Command command) {
    return (command == Command.DESCRIBE);
  }

  private boolean needProvisioningInstance(Command command) {
    return !(command == Command.HELP);
  }

  static ProvUtil createProvUtil(Console console) {
    final Map<String, Command> commandMap = Command.getCommandMap();
    return new ProvUtil(console, commandMap);
  }


  ProvUtil(Console console, Map<String, Command> commands) {
    this.console = console;
    this.commandIndex = commands;
    handlersMap = getCommandHandlersMap(this);
  }

  private Map<Command, CommandHandler> getCommandHandlersMap(ProvUtil provUtil) {
    var map = new HashMap<Command, CommandHandler>();
    map.put(Command.ADD_ACCOUNT_ALIAS, new AddAccountAliasCommandHandler(this));
    map.put(Command.ADD_ACCOUNT_LOGGER, new AddAccountLoggerCommandHandler(this));
//    map.put(Command.ADD_DISTRIBUTION_LIST_ALIAS, new AddDistributionListAliasCommandHandler(this));
//    map.put(Command.ADD_DISTRIBUTION_LIST_MEMBER, new AddDistributionListMemberCommandHandler(this));
    map.put(Command.AUTO_COMPLETE_GAL, new AutoCompleteGalCommandHandler(this));
    map.put(Command.AUTO_PROV_CONTROL, new AutoProvControlCommandHandler(this));
    map.put(Command.CHANGE_PRIMARY_EMAIL, new ChangePrimaryEmailCommandHandler(this));
//    map.put(Command.CHECK_PASSWORD_STRENGTH, new CheckPasswordStrengthCommandHandler(this));
    map.put(Command.CHECK_RIGHT, new CheckRightCommandHandler(this));
//    map.put(Command.COMPACT_INBOX_MAILBOX, new CompactIndexMailboxCommandHandler(this));
    map.put(Command.COPY_COS, new CopyCosCommandHandler(this));
    map.put(Command.COUNT_ACCOUNT, new CountAccountCommandHandler(this));
    map.put(Command.COUNT_OBJECTS, new CountObjectsCommandHandler(this));
    map.put(Command.CREATE_ACCOUNT, new CreateAccountCommandHandler(this));
    map.put(Command.CREATE_ALIAS_DOMAIN, new CreateAliasDomainCommandHandler(this));
//    map.put(Command.CREATE_BULK_ACCOUNTS, new CreateBulkAccountsCommandHandler(this));
//    map.put(Command.CREATE_CALENDAR_RESOURCE, new CreateCalendarResourceCommandHandler(this));
    map.put(Command.CREATE_COS, new CreateCosCommandHandler(this));
    map.put(Command.CREATE_DATA_SOURCE, new CreateDataSourceCommandHandler(this));
//    map.put(Command.CREATE_DISTRIBUTION_LIST, new CreateDistributionListCommandHandler(this));
//    map.put(Command.CREATE_DISTRIBUTION_LISTS_BULK, new CreateDistributionListsBulkCommandHandler(this));
    map.put(Command.CREATE_DOMAIN, new CreateDomainCommandHandler(this));
//    map.put(Command.CREATE_DYNAMIC_DISTRIBUTION_LIST, new CreateDynamicDistributionListCommandHandler(this));
    map.put(Command.CREATE_IDENTITY, new CreateIdentityCommandHandler(this));
    map.put(Command.CREATE_SERVER, new CreateServerCommandHandler(this));
    map.put(Command.CREATE_SIGNATURE, new CreateSignatureCommandHandler(this));
    map.put(Command.CREATE_XMPP_COMPONENT, new CreateXMPPComponentCommandHandler(this));
//    map.put(Command.DELETE_ACCOUNT, new DeleteAccountCommandHandler(this));
//    map.put(Command.DELETE_CALENDAR_RESOURCE, new DeleteCalendarResourceCommandHandler(this));
//    map.put(Command.DELETE_COS, new DeleteCosCommandHandler(this));
//    map.put(Command.DELETE_DATA_SOURCE, new DeleteDataSourceCommandHandler(this));
//    map.put(Command.DELETE_DISTRIBUTION_LIST, new DeleteDistributionListCommandHandler(this));
//    map.put(Command.DELETE_DOMAIN, new DeleteDomainCommandHandler(this));
//    map.put(Command.DELETE_IDENTITY, new DeleteIdentityCommandHandler(this));
//    map.put(Command.DELETE_SERVER, new DeleteServerCommandHandler(this));
//    map.put(Command.DELETE_SIGNATURE, new DeleteSignatureCommandHandler(this));
//    map.put(Command.DELETE_XMPP_COMPONENT, new DeleteXMPPComponentCommandHandler(this));
    map.put(Command.DESCRIBE, new DescribeCommandHandler(this));
    map.put(Command.EXIT, new ExitCommandHandler(this));
    map.put(Command.FLUSH_CACHE, new FlushCacheCommandHandler(this));
    map.put(Command.GENERATE_DOMAIN_PRE_AUTH, new GenerateDomainPreAuthCommandHandler(this));
    map.put(Command.GENERATE_DOMAIN_PRE_AUTH_KEY, new GenerateDomainPreAuthKeyCommandHandler(this));
    map.put(Command.GET_ACCOUNT, new GetAccountCommandHandler(this));
    map.put(Command.GET_ACCOUNT_LOGGERS, new GetAccountLoggersCommandHandler(this));
    map.put(Command.GET_ACCOUNT_MEMBERSHIP, new GetAccountMembershipCommandHandler(this));
    map.put(Command.GET_ALL_ACCOUNTS, new GetAllAccountsCommandHandler(this));
    map.put(Command.GET_ALL_ACCOUNT_LOGGERS, new GetAllAccountLoggersCommandHandler(this));
    map.put(Command.GET_ALL_ADMIN_ACCOUNTS, new GetAllAdminAccountsCommandHandler(this));
//    map.put(Command.GET_ALL_CALENDAR_RESOURCES, new GetAllCalendarResourcesCommandHandler(this));
    map.put(Command.GET_ALL_CONFIG, new GetAllConfigCommandHandler(this));
    map.put(Command.GET_ALL_COS, new GetAllCosCommandHandler(this));
//    map.put(Command.GET_ALL_DISTRIBUTION_LISTS, new GetAllDistributionListsCommandHandler(this));
    map.put(Command.GET_ALL_DOMAINS, new GetAllDomainsCommandHandler(this));
    map.put(Command.GET_ALL_EFFECTIVE_RIGHTS, new GetAllEffectiveRightsCommandHandler(this));
    map.put(Command.GET_ALL_FREEBUSY_PROVIDERS, new GetAllFbpCommandHandler(this));
//    map.put(Command.GET_ALL_MEMCACHED_SERVERS, new GetAllMemcachedServersCommandHandler(this));
//    map.put(Command.GET_ALL_MTA_AUTH_URLS, new GetAllMtaAuthURLsCommandHandler(this));
//    map.put(Command.GET_ALL_REVERSE_PROXY_BACKENDS, new GetAllReverseProxyBackendsCommandHandler(this));
//    map.put(Command.GET_ALL_REVERSE_PROXY_DOMAINS, new GetAllReverseProxyDomainsCommandHandler(this));
//    map.put(Command.GET_ALL_REVERSE_PROXY_URLS, new GetAllReverseProxyURLsCommandHandler(this));
    map.put(Command.GET_ALL_RIGHTS, new GetAllRightsCommandHandler(this));
    map.put(Command.GET_ALL_SERVERS, new GetAllServersCommandHandler(this));
//    map.put(Command.GET_ALL_XMPP_COMPONENTS, new GetAllXMPPComponentsCommandHandler(this));
//    map.put(Command.GET_AUTH_TOKEN_INFO, new GetAuthTokenInfoCommandHandler(this));
//    map.put(Command.GET_CALENDAR_RESOURCE, new GetCalendarResourceCommandHandler(this));
    map.put(Command.GET_CONFIG, new GetConfigCommandHandler(this));
    map.put(Command.GET_COS, new GetCosCommandHandler(this));
    map.put(Command.GET_CREATE_OBJECT_ATTRS, new GetCreateObjectAttrsCommandHandler(this));
    map.put(Command.GET_DATA_SOURCES, new GetDataSourcesCommandHandler(this));
//    map.put(Command.GET_DISTRIBUTION_LIST, new GetDistributionListCommandHandler(this));
    map.put(Command.GET_DISTRIBUTION_LIST_MEMBERSHIP, new GetDistributionListMembershipCommandHandler(this));
    map.put(Command.GET_DOMAIN, new GetDomainCommandHandler(this));
    map.put(Command.GET_DOMAIN_INFO, new GetDomainInfoCommandHandler(this));
    map.put(Command.GET_EFFECTIVE_RIGHTS, new GetEffectiveRightsCommandHandler(this));
    map.put(Command.GET_FREEBUSY_QUEUE_INFO, new GetFreebusyQueueInfoCommandHandler(this));
    map.put(Command.GET_GRANTS, new GetGrantsCommandHandler(this));
    map.put(Command.GET_IDENTITIES, new GetIdentitiesCommandHandler(this));
//    map.put(Command.GET_INDEX_STATS, new GetIndexStatsCommandHandler(this));
//    map.put(Command.GET_MAILBOX_INFO, new GetMailboxInfoCommandHandler(this));
//    map.put(Command.GET_MEMCACHED_CLIENT_CONFIG, new GetMemcachedClientConfigCommandHandler(this));
//    map.put(Command.GET_QUOTA_USAGE, new GetQuotaUsageCommandHandler(this));
    map.put(Command.GET_RIGHT, new GetRightCommandHandler(this));
    map.put(Command.GET_RIGHTS_DOC, new GetRightsDocCommandHandler(this));
    map.put(Command.GET_SERVER, new GetServerCommandHandler(this));
//    map.put(Command.GET_SHARE_INFO, new GetShareInfoCommandHandler(this));
    map.put(Command.GET_SIGNATURES, new GetSignaturesCommandHandler(this));
//    map.put(Command.GET_SPNEGO_DOMAIN, new GetSpnegoDomainCommandHandler(this));
    map.put(Command.GET_XMPP_COMPONENT, new GetXMPPComponentCommandHandler(this));
    map.put(Command.GRANT_RIGHT, new GrantRightCommandHandler(this));
    map.put(Command.HELP, new HelpCommandHandler(this));
//    map.put(Command.LDAP, new .ldapCommandHandler(this));
    map.put(Command.MODIFY_ACCOUNT, new ModifyAccountCommandHandler(this));
//    map.put(Command.MODIFY_CALENDAR_RESOURCE, new ModifyCalendarResourceCommandHandler(this));
//    map.put(Command.MODIFY_CONFIG, new ModifyConfigCommandHandler(this));
//    map.put(Command.MODIFY_COS, new ModifyCosCommandHandler(this));
    map.put(Command.MODIFY_DATA_SOURCE, new ModifyDataSourceCommandHandler(this));
//    map.put(Command.MODIFY_DISTRIBUTION_LIST, new ModifyDistributionListCommandHandler(this));
//    map.put(Command.MODIFY_DOMAIN, new ModifyDomainCommandHandler(this));
    map.put(Command.MODIFY_IDENTITY, new ModifyIdentityCommandHandler(this));
//    map.put(Command.MODIFY_SERVER, new ModifyServerCommandHandler(this));
    map.put(Command.MODIFY_SIGNATURE, new ModifySignatureCommandHandler(this));
//    map.put(Command.MODIFY_XMPP_COMPONENT, new ModifyXMPPComponentCommandHandler(this));
//    map.put(Command.PURGE_ACCOUNT_CALENDAR_CACHE, new PurgeAccountCalendarCacheCommandHandler(this));
//    map.put(Command.PURGE_FREEBUSY_QUEUE, new PurgeFreebusyQueueCommandHandler(this));
//    map.put(Command.PUSH_FREEBUSY, new PushFreebusyCommandHandler(this));
//    map.put(Command.PUSH_FREEBUSY_DOMAIN, new PushFreebusyDomainCommandHandler(this));
//    map.put(Command.RECALCULATE_MAILBOX_COUNTS, new RecalculateMailboxCountsCommandHandler(this));
//    map.put(Command.REINDEX_MAILBOX, new ReIndexMailboxCommandHandler(this));
//    map.put(Command.RELOAD_MEMCACHED_CLIENT_CONFIG, new ReloadMemcachedClientConfigCommandHandler(this));
//    map.put(Command.REMOVE_ACCOUNT_ALIAS, new RemoveAccountAliasCommandHandler(this));
//    map.put(Command.REMOVE_ACCOUNT_LOGGER, new RemoveAccountLoggerCommandHandler(this));
//    map.put(Command.REMOVE_DISTRIBUTION_LIST_ALIAS, new RemoveDistributionListAliasCommandHandler(this));
//    map.put(Command.REMOVE_DISTRIBUTION_LIST_MEMBER, new RemoveDistributionListMemberCommandHandler(this));
//    map.put(Command.RENAME_ACCOUNT, new RenameAccountCommandHandler(this));
//    map.put(Command.RENAME_CALENDAR_RESOURCE, new RenameCalendarResourceCommandHandler(this));
//    map.put(Command.RENAME_COS, new RenameCosCommandHandler(this));
//    map.put(Command.RENAME_DISTRIBUTION_LIST, new RenameDistributionListCommandHandler(this));
//    map.put(Command.RENAME_DOMAIN, new RenameDomainCommandHandler(this));
//    map.put(Command.RESET_ALL_LOGGERS, new ResetAllLoggersCommandHandler(this));
    map.put(Command.REVOKE_RIGHT, new RevokeRightCommandHandler(this));
//    map.put(Command.SEARCH_ACCOUNTS, new SearchAccountsCommandHandler(this));
//    map.put(Command.SEARCH_CALENDAR_RESOURCES, new SearchCalendarResourcesCommandHandler(this));
//    map.put(Command.SEARCH_GAL, new SearchGalCommandHandler(this));
//    map.put(Command.SELECT_MAILBOX, new SelectMailboxCommandHandler(this));
//    map.put(Command.SET_ACCOUNT_COS, new SetAccountCosCommandHandler(this));
//    map.put(Command.SET_PASSWORD, new SetPasswordCommandHandler(this));
//    map.put(Command.SOAP, new .soapCommandHandler(this));
//    map.put(Command.SYNC_GAL, new SyncGalCommandHandler(this));
//    map.put(Command.UNLOCK_MAILBOX, new UnlockMailboxCommandHandler(this));
//    map.put(Command.VERIFY_INDEX, new VerifyIndexCommandHandler(this));
    return map;
  }

  public void initProvisioning() throws ServiceException {
    if (useLdap) {
      if (useLdapMaster) {
        LdapClient.masterOnly();
      }
      prov = Provisioning.getInstance();
    } else {
      SoapProvisioning sp = new SoapProvisioning();
      sp.soapSetURI(
          LC.zimbra_admin_service_scheme.value()
              + serverHostname
              + ":"
              + serverPort
              + AdminConstants.ADMIN_SERVICE_URI);
      if (debugLevel != SoapDebugLevel.none) {
        sp.soapSetHttpTransportDebugListener(this);
      }
      if (account != null && password != null) {
        sp.soapAdminAuthenticate(account, password);
      } else if (authToken != null) {
        sp.soapAdminAuthenticate(authToken);
      } else {
        sp.soapZimbraAdminAuthenticate();
      }
      prov = sp;
    }
  }

  private Command.Via violateVia(Command cmd) {
    Command.Via via = cmd.getVia();
    if (via == null) {
      return null;
    }
    if (via == Command.Via.ldap && !(prov instanceof LdapProv)) {
      return Command.Via.ldap;
    }
    if (via == Command.Via.soap && !(prov instanceof SoapProvisioning)) {
      return Command.Via.soap;
    }
    return null;
  }

  boolean execute(String[] args)
      throws ServiceException, ArgException, IOException, HttpException {
    String[] members;
    Account account;
    AccountLoggerOptions alo;
    command = lookupCommand(args[0]);
    if (command == null) {
      return false;
    }
    Command.Via violatedVia = violateVia(command);
    if (violatedVia != null) {
      usage(violatedVia);
      return true;
    }
    if (!command.checkArgsLength(args)) {
      int length = args.length - 1;
      console.printError(String.format(
              "%s is expecting %s arguments but %s %s %s been provided",
              command.getName(),
              command.getArgumentsCountDescription(),
              length,
              length == 1 ? "argument" : "arguments",
              length == 1 ? "has" : "have"
      ));
      usage();
      return true;
    }
    if (command.needsSchemaExtension()) {
      loadLdapSchemaExtensionAttrs();
    }
    switch (command) {
      case ADD_ACCOUNT_ALIAS:
      case ADD_ACCOUNT_LOGGER:
      case AUTO_COMPLETE_GAL:
      case AUTO_PROV_CONTROL:
      case CHANGE_PRIMARY_EMAIL:
      case COPY_COS:
      case COUNT_ACCOUNT:
      case CREATE_ACCOUNT:
      case CREATE_ALIAS_DOMAIN:
      case CREATE_COS:
      case CREATE_DOMAIN:
      case CREATE_IDENTITY:
      case CREATE_SIGNATURE:
      case CREATE_DATA_SOURCE:
      case CREATE_SERVER:
      case COUNT_OBJECTS:
      case CREATE_XMPP_COMPONENT:
      case DESCRIBE:
      case EXIT:
      case FLUSH_CACHE:
      case GENERATE_DOMAIN_PRE_AUTH_KEY:
      case GENERATE_DOMAIN_PRE_AUTH:
      case GET_ACCOUNT:
      case GET_ACCOUNT_MEMBERSHIP:
      case GET_IDENTITIES:
      case GET_SIGNATURES:
      case GET_DATA_SOURCES:
      case GET_ACCOUNT_LOGGERS:
      case GET_ALL_ACCOUNT_LOGGERS:
      case GET_ALL_ACCOUNTS:
      case GET_ALL_ADMIN_ACCOUNTS:
      case GET_ALL_CONFIG:
      case GET_ALL_COS:
      case GET_ALL_DOMAINS:
      case GET_ALL_FREEBUSY_PROVIDERS:
      case GET_ALL_RIGHTS:
      case GET_ALL_SERVERS:
      case GET_CONFIG:
      case GET_COS:
      case GET_DISTRIBUTION_LIST_MEMBERSHIP:
      case GET_DOMAIN:
      case GET_DOMAIN_INFO:
      case GET_FREEBUSY_QUEUE_INFO:
      case GET_RIGHT:
      case GET_RIGHTS_DOC:
      case GET_SERVER:
      case GET_XMPP_COMPONENT:
      case CHECK_RIGHT:
      case GET_ALL_EFFECTIVE_RIGHTS:
      case GET_EFFECTIVE_RIGHTS:
      case GET_CREATE_OBJECT_ATTRS:
      case GET_GRANTS:
      case GRANT_RIGHT:
      case REVOKE_RIGHT:
      case HELP:
      case MODIFY_ACCOUNT:
      case MODIFY_DATA_SOURCE:
      case MODIFY_IDENTITY:
      case MODIFY_SIGNATURE:
        handlersMap.get(command).handle(args);
        break;
      case MODIFY_COS:
        prov.modifyAttrs(lookupCos(args[1]), getMapAndCheck(args, 2, false), true);
        break;
      case MODIFY_CONFIG:
        prov.modifyAttrs(prov.getConfig(), getMapAndCheck(args, 1, false), true);
        break;
      case MODIFY_DOMAIN:
        prov.modifyAttrs(lookupDomain(args[1]), getMapAndCheck(args, 2, false), true);
        break;
      case MODIFY_SERVER:
        prov.modifyAttrs(lookupServer(args[1]), getMapAndCheck(args, 2, false), true);
        break;
      case DELETE_ACCOUNT:
        doDeleteAccount(args);
        break;
      case DELETE_COS:
        prov.deleteCos(lookupCos(args[1]).getId());
        break;
      case DELETE_DOMAIN:
        prov.deleteDomain(lookupDomain(args[1]).getId());
        break;
      case DELETE_IDENTITY:
        prov.deleteIdentity(lookupAccount(args[1]), args[2]);
        break;
      case DELETE_SIGNATURE:
        account = lookupAccount(args[1]);
        prov.deleteSignature(account, lookupSignatureId(account, args[2]));
        break;
      case DELETE_DATA_SOURCE:
        account = lookupAccount(args[1]);
        prov.deleteDataSource(account, lookupDataSourceId(account, args[2]));
        break;
      case DELETE_SERVER:
        prov.deleteServer(lookupServer(args[1]).getId());
        break;
      case DELETE_XMPP_COMPONENT:
        prov.deleteXMPPComponent(lookupXMPPComponent(args[1]));
        break;
      case PUSH_FREEBUSY:
        doPushFreeBusy(args);
        break;
      case PUSH_FREEBUSY_DOMAIN:
        doPushFreeBusyForDomain(args);
        break;
      case PURGE_FREEBUSY_QUEUE:
        doPurgeFreeBusyQueue(args);
        break;
      case PURGE_ACCOUNT_CALENDAR_CACHE:
        doPurgeAccountCalendarCache(args);
        break;
      case REMOVE_ACCOUNT_ALIAS:
        Account acct = lookupAccount(args[1], false);
        prov.removeAlias(acct, args[2]);
        // even if acct is null, we still invoke removeAlias and throw an exception
        // afterwards.
        // this is so dangling aliases can be cleaned up as much as possible
        if (acct == null) {
          throw AccountServiceException.NO_SUCH_ACCOUNT(args[1]);
        }
        break;
      case REMOVE_ACCOUNT_LOGGER:
        alo = AccountLoggerOptions.parseAccountLoggerOptions(args);
        if (!command.checkArgsLength(alo.args)) {
          usage();
          return true;
        }
        doRemoveAccountLogger(alo);
        break;
      case RENAME_ACCOUNT:
        doRenameAccount(args);
        break;
      case RENAME_COS:
        prov.renameCos(lookupCos(args[1]).getId(), args[2]);
        break;
      case RENAME_DOMAIN:
        doRenameDomain(args);
        break;
      case SET_ACCOUNT_COS:
        prov.setCOS(lookupAccount(args[1]), lookupCos(args[2]));
        break;
      case SEARCH_ACCOUNTS:
        doSearchAccounts(args);
        break;
      case SEARCH_GAL:
        doSearchGal(args);
        break;
      case SYNC_GAL:
        doSyncGal(args);
        break;
      case SET_PASSWORD:
        SetPasswordResult result = prov.setPassword(lookupAccount(args[1]), args[2]);
        if (result.hasMessage()) {
          console.println(result.getMessage());
        }
        break;
      case CHECK_PASSWORD_STRENGTH:
        prov.checkPasswordStrength(lookupAccount(args[1]), args[2]);
        console.println("Password passed strength check.");
        break;
      case CREATE_DISTRIBUTION_LIST:
        console.println(prov.createGroup(args[1], getMapAndCheck(args, 2, true), false).getId());
        break;
      case CREATE_DYNAMIC_DISTRIBUTION_LIST:
        console.println(prov.createGroup(args[1], getMapAndCheck(args, 2, true), true).getId());
        break;
      case CREATE_DISTRIBUTION_LISTS_BULK:
        doCreateDistributionListsBulk(args);
        break;
      case GET_ALL_DISTRIBUTION_LISTS:
        doGetAllDistributionLists(args);
        break;
      case GET_DISTRIBUTION_LIST:
        dumpGroup(lookupGroup(args[1]), getArgNameSet(args, 2));
        break;
      case GET_ALL_XMPP_COMPONENTS:
        doGetAllXMPPComponents();
        break;
      case MODIFY_DISTRIBUTION_LIST:
        prov.modifyAttrs(lookupGroup(args[1]), getMapAndCheck(args, 2, false), true);
        break;
      case DELETE_DISTRIBUTION_LIST:
        doDeleteDistributionList(args);
        break;
      case ADD_DISTRIBUTION_LIST_MEMBER:
        doAddMember(args);
        break;
      case REMOVE_DISTRIBUTION_LIST_MEMBER:
        doRemoveMember(args);
        break;
      case CREATE_BULK_ACCOUNTS:
        doCreateAccountsBulk(args);
        break;
      case ADD_DISTRIBUTION_LIST_ALIAS:
        prov.addGroupAlias(lookupGroup(args[1]), args[2]);
        break;
      case REMOVE_DISTRIBUTION_LIST_ALIAS:
        Group dl = lookupGroup(args[1], false);
        // Even if dl is null, we still invoke removeAlias.
        // This is so dangling aliases can be cleaned up as much as possible.
        // If dl is null, the NO_SUCH_DISTRIBUTION_LIST thrown by SOAP will contain
        // null as the dl identity, because SoapProvisioning sends no id to the server.
        // In this case, we catch the NO_SUCH_DISTRIBUTION_LIST and throw another one
        // with the named/id entered on the comand line.
        try {
          prov.removeGroupAlias(dl, args[2]);
        } catch (ServiceException e) {
          if (!(dl == null
              && AccountServiceException.NO_SUCH_DISTRIBUTION_LIST.equals(e.getCode()))) {
            throw e;
          }
          // else eat the exception, we will throw below
        }
        if (dl == null) {
          throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(args[1]);
        }
        break;
      case RENAME_DISTRIBUTION_LIST:
        prov.renameGroup(lookupGroup(args[1]).getId(), args[2]);
        break;
      case CREATE_CALENDAR_RESOURCE:
        console.println(
            prov.createCalendarResource(
                    args[1], args[2].isEmpty() ? null : args[2], getMapAndCheck(args, 3, true))
                .getId());
        break;
      case DELETE_CALENDAR_RESOURCE:
        prov.deleteCalendarResource(lookupCalendarResource(args[1]).getId());
        break;
      case MODIFY_CALENDAR_RESOURCE:
        prov.modifyAttrs(lookupCalendarResource(args[1]), getMapAndCheck(args, 2, false), true);
        break;
      case RENAME_CALENDAR_RESOURCE:
        prov.renameCalendarResource(lookupCalendarResource(args[1]).getId(), args[2]);
        break;
      case GET_CALENDAR_RESOURCE:
        dumpCalendarResource(lookupCalendarResource(args[1]), true, getArgNameSet(args, 2));
        break;
      case GET_ALL_CALENDAR_RESOURCES:
        doGetAllCalendarResources(args);
        break;
      case SEARCH_CALENDAR_RESOURCES:
        doSearchCalendarResources(args);
        break;
      case GET_SHARE_INFO:
        doGetShareInfo(args);
        break;
      case GET_SPNEGO_DOMAIN:
        doGetSpnegoDomain();
        break;
      case GET_QUOTA_USAGE:
        doGetQuotaUsage(args);
        break;
      case GET_MAILBOX_INFO:
        doGetMailboxInfo(args);
        break;
      case REINDEX_MAILBOX:
        doReIndexMailbox(args);
        break;
      case COMPACT_INBOX_MAILBOX:
        doCompactIndexMailbox(args);
        break;
      case VERIFY_INDEX:
        doVerifyIndex(args);
        break;
      case GET_INDEX_STATS:
        doGetIndexStats(args);
        break;
      case RECALCULATE_MAILBOX_COUNTS:
        doRecalculateMailboxCounts(args);
        break;
      case SELECT_MAILBOX:
        if (!(prov instanceof SoapProvisioning)) {
          throwSoapOnly();
        }
        ZMailboxUtil util = new ZMailboxUtil();
        util.setVerbose(verboseMode);
        util.setDebug(debugLevel != SoapDebugLevel.none);
        boolean smInteractive = interactiveMode && args.length < 3;
        util.setInteractive(smInteractive);
        util.selectMailbox(args[1], (SoapProvisioning) prov);
        if (smInteractive) {
          util.interactive(cliReader);
        } else if (args.length > 2) {
          String[] newArgs = new String[args.length - 2];
          System.arraycopy(args, 2, newArgs, 0, newArgs.length);
          util.execute(newArgs);
        } else {
          throw ZClientException.CLIENT_ERROR(
              "command only valid in interactive mode or with arguments", null);
        }
        break;
      case GET_ALL_MTA_AUTH_URLS:
        doGetAllMtaAuthURLs();
        break;
      case GET_ALL_REVERSE_PROXY_URLS:
        doGetAllReverseProxyURLs();
        break;
      case GET_ALL_REVERSE_PROXY_BACKENDS:
        doGetAllReverseProxyBackends();
        break;
      case GET_ALL_REVERSE_PROXY_DOMAINS:
        doGetAllReverseProxyDomains();
        break;
      case GET_ALL_MEMCACHED_SERVERS:
        doGetAllMemcachedServers();
        break;
      case RELOAD_MEMCACHED_CLIENT_CONFIG:
        doReloadMemcachedClientConfig(args);
        break;
      case GET_MEMCACHED_CLIENT_CONFIG:
        doGetMemcachedClientConfig(args);
        break;
      case GET_AUTH_TOKEN_INFO:
        doGetAuthTokenInfo(args);
        break;
      case SOAP:
        // HACK FOR NOW
        SoapProvisioning sp = new SoapProvisioning();
        sp.soapSetURI("https://localhost:" + serverPort + AdminConstants.ADMIN_SERVICE_URI);
        sp.soapZimbraAdminAuthenticate();
        prov = sp;
        break;
      case LDAP:
        // HACK FOR NOW
        prov = Provisioning.getInstance();
        break;
      case RESET_ALL_LOGGERS:
        doResetAllLoggers(args);
        break;
      case UNLOCK_MAILBOX:
        doUnlockMailbox(args);
        break;
      default:
        return false;
    }
    return true;
  }

  private void doAddMember(String[] args) throws ServiceException {
    String[] members = new String[args.length - 2];
    System.arraycopy(args, 2, members, 0, args.length - 2);
    prov.addGroupMembers(lookupGroup(args[1]), members);
  }

  private void doRemoveMember(String[] args) throws ServiceException {
    String[] members = new String[args.length - 2];
    System.arraycopy(args, 2, members, 0, args.length - 2);
    prov.removeGroupMembers(lookupGroup(args[1]), members);
  }

  private void sendMailboxLockoutRequest(String acctName, String server, String operation)
      throws ServiceException, IOException, HttpException {
    LockoutMailboxRequest req =
        LockoutMailboxRequest.create(AccountNameSelector.fromName(acctName));
    req.setOperation(operation);
    String url = URLUtil.getAdminURL(server);
    ZAuthToken token = ((SoapProvisioning) prov).getAuthToken();
    SoapHttpTransport transport = new SoapHttpTransport(url);
    transport.setAuthToken(token);
    transport.invokeWithoutSession(JaxbUtil.jaxbToElement(req));
  }

  private void doUnlockMailbox(String[] args) throws ServiceException {
    String accountVal = null;
    if (args.length > 1) {
      accountVal = args[1];
    } else {
      usage();
      return;
    }

    if (accountVal != null) {
      Account acct = lookupAccount(accountVal); // will throw NO_SUCH_ACCOUNT if not found
      if (!acct.getAccountStatus().isActive()) {
        final String error = String.format(
            "Cannot unlock mailbox for account %s. Account status must be %s."
                + " Current account status is %s. You must change the value of"
                + " zimbraAccountStatus to '%s' first",
            accountVal, AccountStatus.active, acct.getAccountStatus(), AccountStatus.active);
        console.printError(error);
        System.exit(1);
      }
      String accName = acct.getName();
      String server = acct.getMailHost();
      try {
        sendMailboxLockoutRequest(accName, server, AdminConstants.A_END);
      } catch (ServiceException e) {
        if (ServiceException.UNKNOWN_DOCUMENT.equals(e.getCode())) {
          throw ServiceException.FAILURE(
              "source server version does not support " + AdminConstants.E_LOCKOUT_MAILBOX_REQUEST,
              e);
        } else if (ServiceException.NOT_FOUND.equals(
            e.getCode())) { // if mailbox is not locked, move on
          console.printOutput("Warning: " + e.getMessage());
        } else {
          throw e;
        }
      } catch (IOException | HttpException e) {
        throw ServiceException.FAILURE(
            String.format(
                "Error sending %s (operation = %s) request for %s to %s",
                AdminConstants.E_LOCKOUT_MAILBOX_REQUEST, AdminConstants.A_END, accountVal, server),
            e);
      }

      // unregister moveout if hostname is provided
      if (args.length > 2) {
        // set account status to maintenance and lock the mailbox to avoid race conditions
        acct.setAccountStatus(AccountStatus.maintenance);
        try {
          sendMailboxLockoutRequest(accName, server, AdminConstants.A_START);
        } catch (IOException | HttpException e) {
          throw ServiceException.FAILURE(
              String.format(
                  "Error sending %s (opertion = %s) request for %s to %s.\n"
                      + " Warning: Account is left in maintenance state!",
                  AdminConstants.E_LOCKOUT_MAILBOX_REQUEST,
                  AdminConstants.A_START,
                  accountVal,
                  server),
              e);
        }

        // unregister moveout via SOAP
        String targetServer = args[2];
        try {
          UnregisterMailboxMoveOutRequest unregisterReq =
              UnregisterMailboxMoveOutRequest.create(
                  MailboxMoveSpec.createForNameAndTarget(accName, targetServer));
          String url = URLUtil.getAdminURL(server);
          ZAuthToken token = ((SoapProvisioning) prov).getAuthToken();
          SoapHttpTransport transport = new SoapHttpTransport(url);
          transport.setAuthToken(token);
          transport.invokeWithoutSession(JaxbUtil.jaxbToElement(unregisterReq));
        } catch (ServiceException e) {
          if (ServiceException.UNKNOWN_DOCUMENT.equals(e.getCode())) {
            throw ServiceException.FAILURE(
                String.format(
                    "target server version does not support %s.",
                    BackupConstants.E_UNREGISTER_MAILBOX_MOVE_OUT_REQUEST),
                e);
          } else {
            throw ServiceException.FAILURE("Failed to unregister mailbox moveout", e);
          }
        } catch (IOException e) {
          throw ServiceException.FAILURE(
              String.format(
                  "Error sending %s request for %s to %s.",
                  BackupConstants.E_UNREGISTER_MAILBOX_MOVE_OUT_REQUEST, accountVal, server),
              e);
        } finally {
          // unlock mailbox object and end account maintenance even if failed to
          // unregister moveout
          try {
            sendMailboxLockoutRequest(accName, server, AdminConstants.A_END);
          } catch (ServiceException e) {
            // print error messages, but don't throw any more exceptions, because we
            // have to set account status back to 'active'
            if (ServiceException.UNKNOWN_DOCUMENT.equals(e.getCode())) {
              console.printError(
                  "source server version does not support "
                      + AdminConstants.E_LOCKOUT_MAILBOX_REQUEST);
            } else {
              console.printError(
                  String.format(
                      "Error: failed to unregister mailbox moveout.\n" + " Exception: %s.",
                      e.getMessage()));
            }
          } catch (IOException | HttpException e) {
            console.printError(
                String.format(
                    "Error sending %s (operation = %s) request for %s to %s"
                        + " after unregistering moveout. Exception: %s",
                    AdminConstants.E_LOCKOUT_MAILBOX_REQUEST,
                    AdminConstants.A_END,
                    accountVal,
                    server,
                    e.getMessage()));
          }
          // end account maintenance
          acct.setAccountStatus(AccountStatus.active);
        }
      }
    }
  }

  private void doRenameDomain(String[] args) throws ServiceException {

    // bug 56768
    // if we are not already using master only, force it to use master.
    // Note: after rename domain, the zmprov instance will stay in "master only" mode.
    if (!useLdapMaster) {
      ((LdapProv) prov).alwaysUseMaster();
    }

    LdapProv lp = (LdapProv) prov;
    Domain domain = lookupDomain(args[1]);
    lp.renameDomain(domain.getId(), args[2]);
    console.printOutput("domain " + args[1] + " renamed to " + args[2]);
    console.printOutput(
        "Note: use zmlocalconfig to check and update any localconfig settings referencing"
            + " domain '"
            + args[1]
            + "' on all servers.");
    console.printOutput(
        "Use /opt/zextras/libexec/zmdkimkeyutil to recreate the DKIM entries for new domain"
            + " name if required.");
  }

  private void doGetQuotaUsage(String[] args) throws ServiceException {
    if (!(prov instanceof SoapProvisioning)) {
      throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    List<QuotaUsage> result = sp.getQuotaUsage(args[1]);
    for (QuotaUsage u : result) {
      console.println(String.format("%s %d %d", u.getName(), u.getLimit(), u.getUsed()));
    }
  }

  private void doGetMailboxInfo(String[] args) throws ServiceException {
    if (!(prov instanceof SoapProvisioning)) {
      throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account acct = lookupAccount(args[1]);
    MailboxInfo info = sp.getMailbox(acct);
    console.println(String.format("mailboxId: %s\nquotaUsed: %d", info.getMailboxId(), info.getUsed()));
  }

  private void doReIndexMailbox(String[] args) throws ServiceException {
    if (!(prov instanceof SoapProvisioning)) {
      throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account acct = lookupAccount(args[1]);
    ReIndexBy by = null;
    String[] values = null;
    if (args.length > 3) {
      try {
        by = ReIndexBy.valueOf(args[3]);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("invalid reindex-by", null);
      }
      if (args.length > 4) {
        values = new String[args.length - 4];
        System.arraycopy(args, 4, values, 0, args.length - 4);
      } else {
        throw ServiceException.INVALID_REQUEST("missing reindex-by values", null);
      }
    }
    ReIndexInfo info = sp.reIndex(acct, args[2], by, values);
    ReIndexInfo.Progress progress = info.getProgress();
    console.println(String.format("status: %s\n", info.getStatus()));
    if (progress != null) {
      console.println(String.format(
          "progress: numSucceeded=%d, numFailed=%d, numRemaining=%d\n",
          progress.getNumSucceeded(), progress.getNumFailed(), progress.getNumRemaining()));
    }
  }

  private void doCompactIndexMailbox(String[] args) throws ServiceException {
    if (!(prov instanceof SoapProvisioning)) {
      throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account acct = lookupAccount(args[1]);
    String status = sp.compactIndex(acct, args[2]);
    console.println(String.format("status: %s", status));
  }

  private void doVerifyIndex(String[] args) throws ServiceException {
    if (!(prov instanceof SoapProvisioning)) {
      throwSoapOnly();
    }
    console.println("Verifying, on a large index it can take quite a long time...");
    SoapProvisioning soap = (SoapProvisioning) prov;
    SoapProvisioning.VerifyIndexResult result = soap.verifyIndex(lookupAccount(args[1]));
    console.println();
    console.print(result.message);
    if (!result.status) {
      throw ServiceException.FAILURE(
          "The index may be corrupted. Run reIndexMailbox(rim) to repair.", null);
    }
  }

  private void doGetIndexStats(String[] args) throws ServiceException {
    if (!(prov instanceof SoapProvisioning)) {
      throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account acct = lookupAccount(args[1]);
    IndexStatsInfo stats = sp.getIndexStats(acct);
    console.println(String.format(
        "stats: maxDocs:%d numDeletedDocs:%d", stats.getMaxDocs(), stats.getNumDeletedDocs()));
  }

  private void doRecalculateMailboxCounts(String[] args) throws ServiceException {
    if (!(prov instanceof SoapProvisioning)) {
      throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account account = lookupAccount(args[1]);
    long quotaUsed = sp.recalculateMailboxCounts(account);
    console.print("account: " + account.getName() + "\nquotaUsed: " + quotaUsed + "\n");
  }

  private void doRemoveAccountLogger(AccountLoggerOptions alo) throws ServiceException {
    if (!(prov instanceof SoapProvisioning)) {
      throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account acct = null;
    String category = null;
    if (alo.args.length == 2) {
      // Hack: determine if it's an account or category, based on the name.
      String arg = alo.args[1];
      if (arg.startsWith("zimbra.") || arg.startsWith("com.zimbra")) {
        category = arg;
      } else {
        acct = lookupAccount(alo.args[1]);
      }
    }
    if (alo.args.length == 3) {
      acct = lookupAccount(alo.args[1]);
      category = alo.args[2];
    }
    sp.removeAccountLoggers(acct, category, alo.server);
  }

  private void doResetAllLoggers(String[] args) throws ServiceException {
    if (!(prov instanceof SoapProvisioning)) {
      throwSoapOnly();
    }
    SoapProvisioning sprov = (SoapProvisioning) prov;
    String server = null;
    if (args.length > 1 && ("-s".equals(args[1]) || "--server".equals(args[1]))) {
      server = args.length > 0 ? args[2] : null;
    }
    sprov.resetAllLoggers(server);
  }

  private void doCreateAccountsBulk(String[] args) throws ServiceException {
      String domain = args[1];
      String userPassword =  args[4];
      String nameMask = args[2];
      int numAccounts = Integer.parseInt(args[3]);
      for (int ix = 0; ix < numAccounts; ix++) {
        String name = nameMask + ix + "@" + domain;
        Map<String, Object> attrs = new HashMap<>();
        String displayName = nameMask + " N. " + ix;
        StringUtil.addToMultiMap(attrs, "displayName", displayName);
        Account createdAccount = prov.createAccount(name, userPassword, attrs);
        console.println(createdAccount.getId());
      }
  }

  private void doGetShareInfo(String[] args) throws ServiceException {
    if (!(prov instanceof SoapProvisioning)) {
      throwSoapOnly();
    }
    Account owner = lookupAccount(args[1]);

    console.println(ShareInfoVisitor.getPrintHeadings());
    prov.getShareInfo(owner, new ShareInfoVisitor(console));
  }

  private void doGetSpnegoDomain() throws ServiceException {
    Config config = prov.getConfig();
    String spnegoAuthRealm = config.getSpnegoAuthRealm();
    if (spnegoAuthRealm != null) {
      Domain domain = prov.get(Key.DomainBy.krb5Realm, spnegoAuthRealm);
      if (domain != null) {
        console.println(domain.getName());
      }
    }
  }

  private boolean confirm(String msg) {
    if (batchMode) {
      return true;
    }

    console.println(msg);
    console.print("Continue? [Y]es, [N]o: ");

    BufferedReader in;
    try {
      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
      String line = StringUtil.readLine(in);
      if ("y".equalsIgnoreCase(line) || "yes".equalsIgnoreCase(line)) {
        return true;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  private void doDeleteAccount(String[] args) throws ServiceException {
    if (prov instanceof LdapProv) {
      boolean confirmed =
          confirm(
              "-l option is specified.  Only the LDAP entry of the account will be"
                  + " deleted.\n"
                  + "DB data of the account and associated blobs will not be"
                  + " deleted.\n");

      if (!confirmed) {
        console.println("aborted");
        return;
      }
    }

    String key = args[1];
    Account acct = lookupAccount(key);
    if (key.equalsIgnoreCase(acct.getId())
        || key.equalsIgnoreCase(acct.getName())
        || acct.getName().equalsIgnoreCase(key + "@" + acct.getDomainName())) {
      prov.deleteAccount(acct.getId());
    } else {
      throw ServiceException.INVALID_REQUEST(
          "argument to deleteAccount must be an account id or the account's primary name", null);
    }
  }

  private void doRenameAccount(String[] args) throws ServiceException {
    if (prov instanceof LdapProv) {
      boolean confirmed =
          confirm(
              "-l option is specified.  "
                  + "Only the LDAP portion of the account will be deleted.\n"
                  + "DB data of the account will not be renamed.\n");

      if (!confirmed) {
        console.println("aborted");
        return;
      }
    }

    prov.renameAccount(lookupAccount(args[1]).getId(), args[2]);
  }

  private void doSearchAccounts(String[] args) throws ServiceException, ArgException {
    boolean verbose = false;
    int i = 1;

    if (args[i].equals("-v")) {
      verbose = true;
      i++;
      if (args.length < i - 1) {
        usage();
        return;
      }
    }

    if (args.length < i + 1) {
      usage();
      return;
    }

    String query = args[i];

    Map<String, Object> attrs = getMap(args, i + 1);
    String limitStr = (String) attrs.get("limit");
    int limit = limitStr == null ? Integer.MAX_VALUE : Integer.parseInt(limitStr);

    String offsetStr = (String) attrs.get("offset");
    int offset = offsetStr == null ? 0 : Integer.parseInt(offsetStr);

    String sortBy = (String) attrs.get("sortBy");
    String sortAscending = (String) attrs.get("sortAscending");
    boolean isSortAscending = sortAscending == null || "1".equalsIgnoreCase(sortAscending);

    String[] attrsToGet = null;

    String typesStr = (String) attrs.get("types");
    if (typesStr == null) {
      typesStr =
          SearchDirectoryOptions.ObjectType.accounts.name()
              + ","
              + SearchDirectoryOptions.ObjectType.aliases.name()
              + ","
              + SearchDirectoryOptions.ObjectType.distributionlists.name()
              + ","
              + SearchDirectoryOptions.ObjectType.dynamicgroups.name()
              + ","
              + SearchDirectoryOptions.ObjectType.resources.name();
    }

    String domainStr = (String) attrs.get("domain");

    SearchDirectoryOptions searchOpts = new SearchDirectoryOptions(attrsToGet);
    if (domainStr != null) {
      Domain d = lookupDomain(domainStr, prov);
      searchOpts.setDomain(d);
    }
    searchOpts.setTypes(typesStr);
    searchOpts.setSortOpt(isSortAscending ? SortOpt.SORT_ASCENDING : SortOpt.SORT_DESCENDING);
    searchOpts.setSortAttr(sortBy);

    // if LdapClient is not initialized(the case for SoapProvisioning), FilterId
    // is not initialized. Use null for SoapProvisioning, it will be set to
    // FilterId.ADMIN_SEARCH in SearchDirectory soap handler.
    FilterId filterId = (prov instanceof LdapProv) ? FilterId.ADMIN_SEARCH : null;
    searchOpts.setFilterString(filterId, query);
    searchOpts.setConvertIDNToAscii(true); // query must be already RFC 2254 escaped

    List<NamedEntry> accounts = prov.searchDirectory(searchOpts);

    for (int j = offset; j < offset + limit && j < accounts.size(); j++) {
      NamedEntry account = accounts.get(j);
      if (verbose) {
        if (account instanceof Account) {
          dumpAccount((Account) account, true, null);
        } else if (account instanceof Alias) {
          dumpAlias((Alias) account);
        } else if (account instanceof DistributionList) {
          dumpGroup((DistributionList) account, null);
        } else if (account instanceof Domain) {
          dumpDomain((Domain) account, null);
        }
      } else {
        console.println(account.getName());
      }
    }
  }

  private void doSyncGal(String[] args) throws ServiceException {
    String domain = args[1];
    String token = args.length == 3 ? args[2] : "";

    Domain d = lookupDomain(domain);

    SearchGalResult result = null;
    if (prov instanceof LdapProv) {
      GalContact.Visitor visitor =
          gc -> dumpContact(gc);
      result = prov.syncGal(d, token, visitor);
    } else {
      result = ((SoapProvisioning) prov).searchGal(d, "", GalSearchType.all, token, 0, 0, null);
      for (GalContact contact : result.getMatches()) {
        dumpContact(contact);
      }
    }

    if (result.getToken() != null) {
      console.println("\n# token = " + result.getToken() + "\n");
    }
  }

  private void doSearchGal(String[] args) throws ServiceException, ArgException {
    if (args.length < 3) {
      usage();
      return;
    }
    String domain = args[1];
    String query = args[2];
    Map<String, Object> attrs = getMap(args, 3);
    String limitStr = (String) attrs.get("limit");
    int limit = limitStr == null ? 0 : Integer.parseInt(limitStr);
    String offsetStr = (String) attrs.get("offset");
    int offset = offsetStr == null ? 0 : Integer.parseInt(offsetStr);
    String sortBy = (String) attrs.get("sortBy");
    Domain d = lookupDomain(domain);

    SearchGalResult result;

    if (prov instanceof LdapProv) {
      if (offsetStr != null) {
        throw ServiceException.INVALID_REQUEST("offset is not supported with -l", null);
      }

      if (sortBy != null) {
        throw ServiceException.INVALID_REQUEST("sortBy is not supported with -l", null);
      }

      GalContact.Visitor visitor =
          gc -> dumpContact(gc);
      result = prov.searchGal(d, query, GalSearchType.all, limit, visitor);

    } else {
      result =
          ((SoapProvisioning) prov)
              .searchGal(d, query, GalSearchType.all, null, limit, offset, sortBy);
      for (GalContact contact : result.getMatches()) {
        dumpContact(contact);
      }
    }
  }

  public void dumpCos(Cos cos, Set<String> attrNames) throws ServiceException {
    console.println("# name " + cos.getName());
    Map<String, Object> attrs = cos.getAttrs();
    dumpAttrs(attrs, attrNames);
    console.println();
  }

  public void dumpDomain(Domain domain, Set<String> attrNames) throws ServiceException {
    dumpDomain(domain, true, attrNames);
  }

  public void dumpDomain(Domain domain, boolean expandConfig, Set<String> attrNames)
      throws ServiceException {
    console.println("# name " + domain.getName());
    Map<String, Object> attrs = domain.getAttrs(expandConfig);
    dumpAttrs(attrs, attrNames);
    console.println();
  }

  private void dumpGroup(Group group, Set<String> attrNames) throws ServiceException {

    String[] members;
    if (group instanceof DynamicGroup) {
      members = ((DynamicGroup) group).getAllMembers(true);
    } else {
      members = group.getAllMembers();
    }

    int count = members == null ? 0 : members.length;
    console.println("# distributionList " + group.getName() + " memberCount=" + count);
    Map<String, Object> attrs = group.getAttrs();
    dumpAttrs(attrs, attrNames);
    console.println();
    console.println("members");
    for (String member : members) {
      console.println(member);
    }
  }

  private void dumpAlias(Alias alias) throws ServiceException {
    console.println("# alias " + alias.getName());
    Map<String, Object> attrs = alias.getAttrs();
    dumpAttrs(attrs, null);
  }

  public void dumpRight(Right right, boolean expandComboRight) {
    String tab = "    ";
    String indent = tab;
    String indent2 = indent + indent;

    console.println();
    console.println("------------------------------");

    console.println(right.getName());
    console.println(indent + "      description: " + right.getDesc());
    console.println(indent + "       right type: " + right.getRightType().name());

    String targetType = right.getTargetTypeStr();
    console.println(indent + "   target type(s): " + (targetType == null ? "" : targetType));

    String grantTargetType = right.getGrantTargetTypeStr();
    console.println(
        indent + "grant target type: " + (grantTargetType == null ? "(default)" : grantTargetType));

    console.println(indent + "      right class: " + right.getRightClass().name());

    if (right.isAttrRight()) {
      AttrRight attrRight = (AttrRight) right;
      console.println();
      console.println(indent + "attributes:");
      if (attrRight.allAttrs()) {
        console.println(indent2 + "all attributes");
      } else {
        for (String attrName : attrRight.getAttrs()) {
          console.println(indent2 + attrName);
        }
      }
    } else if (right.isComboRight()) {
      ComboRight comboRight = (ComboRight) right;
      console.println();
      console.println(indent + "rights:");
      dumpComboRight(comboRight, expandComboRight, indent, new HashSet<>());
    }
    console.println();

    Help help = right.getHelp();
    if (help != null) {
      console.println(help.getDesc());
      List<String> helpItems = help.getItems();
      for (String helpItem : helpItems) {
        // console.println(FileGenUtil.wrapComments(helpItem, 70, prefix) + "\n");
        console.println("- " + helpItem.trim());
        console.println();
      }
    }
    console.println();
  }

  private void dumpComboRight(
      ComboRight comboRight, boolean expandComboRight, String indent, Set<String> seen) {
    // safety check, should not happen,
    // detect circular combo rights
    if (seen.contains(comboRight.getName())) {
      console.println("Circular combo right: " + comboRight.getName() + " !!");
      return;
    }

    String indent2 = indent + indent;

    for (Right r : comboRight.getRights()) {
      String tt = r.getTargetTypeStr();
      tt = tt == null ? "" : " (" + tt + ")";
      // console.format("%s%10.10s: %s %s\n", indent2, r.getRightType().name(), r.getName(),
      // tt);
      console.print(String.format("%s %s: %s %s\n", indent2, r.getRightType().name(), r.getName(), tt));

      seen.add(comboRight.getName());

      if (r.isComboRight() && expandComboRight) {
        dumpComboRight((ComboRight) r, expandComboRight, indent2, seen);
      }

      seen.clear();
    }
  }

  public void dumpServer(Server server, boolean expandConfig, Set<String> attrNames)
      throws ServiceException {
    console.println("# name " + server.getName());
    Map<String, Object> attrs = server.getAttrs(expandConfig);
    dumpAttrs(attrs, attrNames);
    console.println();
  }

  public void dumpXMPPComponent(XMPPComponent comp, Set<String> attrNames)
      throws ServiceException {
    console.println("# name " + comp.getName());
    Map<String, Object> attrs = comp.getAttrs();
    dumpAttrs(attrs, attrNames);
    console.println();
  }

  private void doGetAllXMPPComponents() throws ServiceException {
    List<XMPPComponent> components = prov.getAllXMPPComponents();
    for (XMPPComponent comp : components) {
      dumpXMPPComponent(comp, null);
    }
  }

  public void dumpAccount(Account account, boolean expandCos, Set<String> attrNames)
      throws ServiceException {
    console.println("# name " + account.getName());
    Map<String, Object> attrs = account.getAttrs(expandCos);
    dumpAttrs(attrs, attrNames);
    console.println();
  }

  private void dumpCalendarResource(
      CalendarResource resource, boolean expandCos, Set<String> attrNames) throws ServiceException {
    console.println("# name " + resource.getName());
    Map<String, Object> attrs = resource.getAttrs(expandCos);
    dumpAttrs(attrs, attrNames);
    console.println();
  }

  public void dumpContact(GalContact contact) throws ServiceException {
    console.println("# name " + contact.getId());
    Map<String, Object> attrs = contact.getAttrs();
    dumpAttrs(attrs, null);
    console.println();
  }

  public void dumpIdentity(Identity identity, Set<String> attrNameSet) throws ServiceException {
    console.println("# name " + identity.getName());
    Map<String, Object> attrs = identity.getAttrs();
    dumpAttrs(attrs, attrNameSet);
    console.println();
  }

  public void dumpAttrs(Map<String, Object> attrsIn, Set<String> specificAttrs)
      throws ServiceException {
    TreeMap<String, Object> attrs = new TreeMap<>(attrsIn);

    Map<String, Set<String>> specificAttrValues = null;

    if (specificAttrs != null) {
      specificAttrValues = new HashMap<>();
      for (String specificAttr : specificAttrs) {
        int colonAt = specificAttr.indexOf("=");
        String attrName = null;
        String attrValue = null;
        if (colonAt == -1) {
          attrName = specificAttr;
        } else {
          attrName = specificAttr.substring(0, colonAt);
          attrValue = specificAttr.substring(colonAt + 1);
          if (attrValue.length() < 1) {
            throw ServiceException.INVALID_REQUEST("missing value for " + specificAttr, null);
          }
        }

        attrName = attrName.toLowerCase();
        Set<String> values = specificAttrValues.get(attrName);
        if (values == null) { // haven't seen the attr yet
          values = new HashSet<>();
        }
        if (attrValue != null) {
          values.add(attrValue);
        }
        specificAttrValues.put(attrName, values);
      }
    }

    AttributeManager attrMgr = AttributeManager.getInstance();

    SimpleDateFormat dateFmt = new SimpleDateFormat("yyyyMMddHHmmss");
    String timestamp = dateFmt.format(new Date());

    for (Map.Entry<String, Object> entry : attrs.entrySet()) {
      String name = entry.getKey();

      boolean isBinary = needsBinaryIO(attrMgr, name);

      Set<String> specificValues = null;
      if (specificAttrValues != null) {
        specificValues = specificAttrValues.get(name.toLowerCase());
      }
      if (specificAttrValues == null || specificAttrValues.containsKey(name.toLowerCase())) {

        Object value = entry.getValue();

        if (value instanceof String[]) {
          String[] sv = (String[]) value;
          for (int i = 0; i < sv.length; i++) {
            String aSv = sv[i];
            // don't print permission denied attr
            if (this.forceDisplayAttrValue
                || aSv.length() > 0
                    && (specificValues == null
                        || specificValues.isEmpty()
                        || specificValues.contains(aSv))) {
              printAttr(name, aSv, i, isBinary, timestamp);
            }
          }
        } else if (value instanceof String) {
          // don't print permission denied attr
          if (this.forceDisplayAttrValue
              || ((String) value).length() > 0
                  && (specificValues == null
                      || specificValues.isEmpty()
                      || specificValues.contains(value))) {
            printAttr(name, (String) value, null, isBinary, timestamp);
          }
        }
      }
    }

    // force display empty value attribute
    if (this.forceDisplayAttrValue) {
      for (String attr : specificAttrs) {
        if (!attrs.containsKey(attr)) {
          AttributeInfo ai = attrMgr.getAttributeInfo(attr);
          if (ai != null) {
            printAttr(attr, "", null, false, timestamp);
          }
        }
      }
    }
  }

  private void doCreateDistributionListsBulk(String[] args) throws ServiceException {
    if (args.length < 3) {
      usage();
    } else {
      String domain = args[1];
      String nameMask = args[2];
      int numAccounts = Integer.parseInt(args[3]);
      for (int i = 0; i < numAccounts; i++) {
        String name = nameMask + i + "@" + domain;
        Map<String, Object> attrs = new HashMap<>();
        String displayName = nameMask + " N. " + i;
        StringUtil.addToMultiMap(attrs, "displayName", displayName);
        DistributionList dl = prov.createDistributionList(name, attrs);
        console.println(dl.getId());
      }
    }
  }

  private void doGetAllDistributionLists(String[] args) throws ServiceException {
    String d = null;
    boolean verbose = false;
    int i = 1;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-v")) {
        verbose = true;
      } else {
        if (d == null) {
          d = arg;
        } else {
          console.println("invalid arg: " + arg + ", already specified domain: " + d);
          usage();
          return;
        }
      }
      i++;
    }

    if (d == null) {
      List<Domain> domains = prov.getAllDomains();
      for (Domain domain : domains) {
        Collection<?> dls = prov.getAllGroups(domain);
        for (Object obj : dls) {
          Group dl = (Group) obj;
          if (verbose) {
            dumpGroup(dl, null);
          } else {
            console.println(dl.getName());
          }
        }
      }
    } else {
      Domain domain = lookupDomain(d);
      Collection<?> dls = prov.getAllGroups(domain);
      for (Object obj : dls) {
        Group dl = (Group) obj;
        if (verbose) {
          dumpGroup(dl, null);
        } else {
          console.println(dl.getName());
        }
      }
    }
  }

  private void doGetAllCalendarResources(String[] args) throws ServiceException {
    boolean verbose = false;
    boolean applyDefault = true;
    String d = null;
    String s = null;

    int i = 1;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-v")) {
        verbose = true;
      } else if (arg.equals("-e")) {
        applyDefault = false;
      } else if (arg.equals("-s")) {
        i++;
        if (i < args.length) {
          if (s == null) {
            s = args[i];
          } else {
            console.println("invalid arg: " + args[i] + ", already specified -s with " + s);
            usage();
            return;
          }
        } else {
          usage();
          return;
        }
      } else {
        if (d == null) {
          d = arg;
        } else {
          console.println("invalid arg: " + arg + ", already specified domain: " + d);
          usage();
          return;
        }
      }
      i++;
    }

    if (!applyDefault && !verbose) {
      console.println(ERR_INVALID_ARG_EV);
      usage();
      return;
    }

    // always use LDAP
    Provisioning prov = Provisioning.getInstance();

    Server server = null;
    if (s != null) {
      server = lookupServer(s);
    }
    if (d == null) {
      List<Domain> domains = prov.getAllDomains();
      for (Domain domain : domains) {
        doGetAllCalendarResources(prov, domain, server, verbose, applyDefault);
      }
    } else {
      Domain domain = lookupDomain(d, prov);
      doGetAllCalendarResources(prov, domain, server, verbose, applyDefault);
    }
  }

  private void doGetAllCalendarResources(
      Provisioning prov,
      Domain domain,
      Server server,
      final boolean verbose,
      final boolean applyDefault)
      throws ServiceException {
    NamedEntry.Visitor visitor =
        entry -> {
          if (verbose) {
            dumpCalendarResource((CalendarResource) entry, applyDefault, null);
          } else {
            console.println(entry.getName());
          }
        };
    prov.getAllCalendarResources(domain, server, visitor);
  }

  private void doSearchCalendarResources(String[] args) throws ServiceException {

    boolean verbose = false;
    int i = 1;

    if (args.length < i + 1) {
      usage();
      return;
    }
    if (args[i].equals("-v")) {
      verbose = true;
      i++;
    }
    if (args.length < i + 1) {
      usage();
      return;
    }
    Domain d = lookupDomain(args[i++]);

    if ((args.length - i) % 3 != 0) {
      usage();
      return;
    }

    EntrySearchFilter.Multi multi = new EntrySearchFilter.Multi(false, EntrySearchFilter.AndOr.and);
    for (; i < args.length; ) {
      String attr = args[i++];
      String op = args[i++];
      String value = args[i++];
      try {
        EntrySearchFilter.Single single = new EntrySearchFilter.Single(false, attr, op, value);
        multi.add(single);
      } catch (IllegalArgumentException e) {
        console.printError("Bad search op in: " + attr + " " + op + " '" + value + "'");
        e.printStackTrace();
        usage();
        return;
      }
    }
    EntrySearchFilter filter = new EntrySearchFilter(multi);
    String filterStr = LdapEntrySearchFilter.toLdapCalendarResourcesFilter(filter);

    SearchDirectoryOptions searchOpts = new SearchDirectoryOptions();
    searchOpts.setDomain(d);
    searchOpts.setTypes(ObjectType.resources);
    searchOpts.setSortOpt(SortOpt.SORT_ASCENDING);
    searchOpts.setFilterString(FilterId.ADMIN_SEARCH, filterStr);

    List<NamedEntry> resources = prov.searchDirectory(searchOpts);

    // List<NamedEntry> resources = prov.searchCalendarResources(d, filter, null, null, true);
    for (NamedEntry entry : resources) {
      CalendarResource resource = (CalendarResource) entry;
      if (verbose) {
        dumpCalendarResource(resource, true, null);
      } else {
        console.println(resource.getName());
      }
    }
  }

  public Account lookupAccount(String key, boolean mustFind, boolean applyDefault)
      throws ServiceException {
    Account account;
    if (applyDefault == true || (prov instanceof LdapProv)) {
      account = prov.getAccount(key);
    } else {
      /*
       * oops, do not apply default, and we are SoapProvisioning
       *
       * This a bit awkward because the applyDefault is controlled at the Entry.getAttrs, not at the provisioning
       * interface. But for SOAP, this needs to be passed to the get(AccountBy) method so it can set the flag in
       * SOAP. We do not want to add a provisioning method for this. Instead, we make it a SOAPProvisioning only
       * method.
       */
      SoapProvisioning soapProv = (SoapProvisioning) prov;
      account = soapProv.getAccount(key, applyDefault);
    }

    if (mustFind && account == null) {
      throw AccountServiceException.NO_SUCH_ACCOUNT(key);
    } else {
      return account;
    }
  }

  public Account lookupAccount(String key) throws ServiceException {
    return lookupAccount(key, true, true);
  }

  private Account lookupAccount(String key, boolean mustFind) throws ServiceException {
    return lookupAccount(key, mustFind, true);
  }

  private CalendarResource lookupCalendarResource(String key) throws ServiceException {
    CalendarResource res = prov.get(guessCalendarResourceBy(key), key);
    if (res == null) {
      throw AccountServiceException.NO_SUCH_CALENDAR_RESOURCE(key);
    } else {
      return res;
    }
  }

  public Domain lookupDomain(String key) throws ServiceException {
    return lookupDomain(key, prov);
  }

  public Domain lookupDomain(String key, Provisioning prov) throws ServiceException {
    return lookupDomain(key, prov, true);
  }

  public Domain lookupDomain(String key, Provisioning prov, boolean applyDefault)
      throws ServiceException {
    Domain domain;
    if (prov instanceof SoapProvisioning) {
      SoapProvisioning soapProv = (SoapProvisioning) prov;
      domain = soapProv.get(guessDomainBy(key), key, applyDefault);
    } else {
      domain = prov.get(guessDomainBy(key), key);
    }
    if (domain == null) {
      throw AccountServiceException.NO_SUCH_DOMAIN(key);
    } else {
      return domain;
    }
  }

  public Cos lookupCos(String key) throws ServiceException {
    Cos cos = prov.get(guessCosBy(key), key);
    if (cos == null) {
      throw AccountServiceException.NO_SUCH_COS(key);
    } else {
      return cos;
    }
  }

  public Server lookupServer(String key) throws ServiceException {
    return lookupServer(key, true);
  }

  public Server lookupServer(String key, boolean applyDefault) throws ServiceException {
    Server server;
    if (prov instanceof SoapProvisioning) {
      SoapProvisioning soapProv = (SoapProvisioning) prov;
      server = soapProv.get(guessServerBy(key), key, applyDefault);
    } else {
      server = prov.get(guessServerBy(key), key);
    }
    if (server == null) {
      throw AccountServiceException.NO_SUCH_SERVER(key);
    } else {
      return server;
    }
  }

  public String lookupDataSourceId(Account account, String key) throws ServiceException {
    if (Provisioning.isUUID(key)) {
      return key;
    }
    DataSource ds = prov.get(account, Key.DataSourceBy.name, key);
    if (ds == null) {
      throw AccountServiceException.NO_SUCH_DATA_SOURCE(key);
    } else {
      return ds.getId();
    }
  }

  public String lookupSignatureId(Account account, String key) throws ServiceException {
    Signature sig = prov.get(account, guessSignatureBy(key), key);
    if (sig == null) {
      throw AccountServiceException.NO_SUCH_SIGNATURE(key);
    } else {
      return sig.getId();
    }
  }

  private DistributionList lookupDistributionList(String key, boolean mustFind)
      throws ServiceException {
    DistributionList dl = prov.get(guessDistributionListBy(key), key);
    if (mustFind && dl == null) {
      throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(key);
    } else {
      return dl;
    }
  }

  private DistributionList lookupDistributionList(String key) throws ServiceException {
    return lookupDistributionList(key, true);
  }

  private Group lookupGroup(String key, boolean mustFind) throws ServiceException {
    Group dl = prov.getGroup(guessDistributionListBy(key), key);
    if (mustFind && dl == null) {
      throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(key);
    } else {
      return dl;
    }
  }

  public Group lookupGroup(String key) throws ServiceException {
    return lookupGroup(key, true);
  }

  public XMPPComponent lookupXMPPComponent(String value) throws ServiceException {
    if (Provisioning.isUUID(value)) {
      return prov.get(Key.XMPPComponentBy.id, value);
    } else {
      return prov.get(Key.XMPPComponentBy.name, value);
    }
  }

  public static AccountBy guessAccountBy(String value) {
    if (Provisioning.isUUID(value)) {
      return AccountBy.id;
    }
    return AccountBy.name;
  }

  public static Key.CosBy guessCosBy(String value) {
    if (Provisioning.isUUID(value)) {
      return Key.CosBy.id;
    }
    return Key.CosBy.name;
  }

  public static Key.DomainBy guessDomainBy(String value) {
    if (Provisioning.isUUID(value)) {
      return Key.DomainBy.id;
    }
    return Key.DomainBy.name;
  }

  public static Key.ServerBy guessServerBy(String value) {
    if (Provisioning.isUUID(value)) {
      return Key.ServerBy.id;
    }
    return Key.ServerBy.name;
  }

  public static Key.CalendarResourceBy guessCalendarResourceBy(String value) {
    if (Provisioning.isUUID(value)) {
      return Key.CalendarResourceBy.id;
    }
    return Key.CalendarResourceBy.name;
  }

  public static Key.DistributionListBy guessDistributionListBy(String value) {
    if (Provisioning.isUUID(value)) {
      return Key.DistributionListBy.id;
    }
    return Key.DistributionListBy.name;
  }

  public static Key.SignatureBy guessSignatureBy(String value) {
    if (Provisioning.isUUID(value)) {
      return Key.SignatureBy.id;
    }
    return Key.SignatureBy.name;
  }

  public static TargetBy guessTargetBy(String value) {
    if (Provisioning.isUUID(value)) {
      return TargetBy.id;
    }
    return TargetBy.name;
  }

  public static GranteeBy guessGranteeBy(String value) {
    if (Provisioning.isUUID(value)) {
      return GranteeBy.id;
    }
    return GranteeBy.name;
  }

  private void checkDeprecatedAttrs(Map<String, ? extends Object> attrs) throws ServiceException {
    AttributeManager am = AttributeManager.getInstance();
    boolean hadWarnings = false;
    for (String attr : attrs.keySet()) {
      AttributeInfo ai = am.getAttributeInfo(attr);
      if (ai == null) {
        continue;
      }

      if (ai.isDeprecated()) {
        hadWarnings = true;
        console.println(
            "Warn: attribute " + attr + " has been deprecated since " + ai.getDeprecatedSince());
      }
    }

    if (hadWarnings) {
      console.println();
    }
  }

  private static boolean needsBinaryIO(AttributeManager attrMgr, String attr) {
    return attrMgr.containsBinaryData(attr);
  }

  /** get map and check/warn deprecated attrs. */
  public Map<String, Object> getMapAndCheck(String[] args, int offset, boolean isCreateCmd)
      throws ArgException, ServiceException {
    Map<String, Object> attrs = getAttrMap(args, offset, isCreateCmd);
    checkDeprecatedAttrs(attrs);
    return attrs;
  }

  /**
   * Convert an array of the form:
   *
   * <p>a1 v1 a2 v2 a2 v3
   *
   * <p>to a map of the form:
   *
   * <p>a1 -> v1 a2 -> [v2, v3]
   *
   * <p>For binary attribute, the argument following an attribute name will be treated as a file
   * path and value for the attribute will be the base64 encoded string of the content of the file.
   */
  private Map<String, Object> keyValueArrayToMultiMap(
      String[] args, int offset, boolean isCreateCmd) throws IOException, ServiceException {
    AttributeManager attrMgr = AttributeManager.getInstance();

    Map<String, Object> attrs = new HashMap<>();

    String safeguarded_attrs_prop = LC.get("zmprov_safeguarded_attrs");
    Set<String> safeguarded_attrs =
        safeguarded_attrs_prop == null
            ? Sets.newHashSet()
            : Sets.newHashSet(safeguarded_attrs_prop.toLowerCase().split(","));
    Multiset<String> multiValAttrsToCheck = HashMultiset.create();

    for (int i = offset; i < args.length; i += 2) {
      String n = args[i];
      if (i + 1 >= args.length) {
        throw new IllegalArgumentException("not enough arguments");
      }
      String v = args[i + 1];
      String attrName = n;
      if (n.charAt(0) == '+' || n.charAt(0) == '-') {
        attrName = attrName.substring(1);
      } else if (safeguarded_attrs.contains(attrName.toLowerCase())
          && attrMgr.isMultiValued(attrName)) {
        multiValAttrsToCheck.add(attrName.toLowerCase());
      }
      if (needsBinaryIO(attrMgr, attrName) && v.length() > 0) {
        File file = new File(v);
        byte[] bytes = ByteUtil.getContent(file);
        v = ByteUtil.encodeLDAPBase64(bytes);
      }
      StringUtil.addToMultiMap(attrs, n, v);
    }

    if (!allowMultiValuedAttrReplacement && !isCreateCmd) {
      for (Multiset.Entry<String> entry : multiValAttrsToCheck.entrySet()) {
        if (entry.getCount() == 1) {
          // If multiple values are being assigned to an attr as part of the same command
          // then we don't consider it an unsafe replacement
          console.printError("error: cannot replace multi-valued attr value unless -r is specified");
          System.exit(2);
        }
      }
    }

    return attrs;
  }

  private Map<String, Object> getAttrMap(String[] args, int offset, boolean isCreateCmd)
      throws ArgException, ServiceException {
    try {
      return keyValueArrayToMultiMap(args, offset, isCreateCmd);
    } catch (IllegalArgumentException iae) {
      throw new ArgException("not enough arguments");
    } catch (IOException ioe) {
      throw ServiceException.INVALID_REQUEST("unable to process arguments", ioe);
    }
  }

  public Map<String, Object> getMap(String[] args, int offset) throws ArgException {
    try {
      return StringUtil.keyValueArrayToMultiMap(args, offset);
    } catch (IllegalArgumentException iae) {
      throw new ArgException("not enough arguments");
    }
  }

  public Set<String> getArgNameSet(String[] args, int offset) {
    if (offset >= args.length) {
      return null;
    }
    Set<String> result = new HashSet<>();
    for (int i = offset; i < args.length; i++) {
      result.add(args[i].toLowerCase());
    }
    return result;
  }

  private void interactive(BufferedReader in) throws IOException {
    cliReader = in;
    interactiveMode = true;
    while (true) {
      console.print("prov> ");
      String line = StringUtil.readLine(in);
      if (line == null) {
        break;
      }
      if (verboseMode) {
        console.println(line);
      }
      String[] args = StringUtil.parseLine(line);
      args = fixArgs(args);

      if (args.length == 0) {
        continue;
      }
      try {
        if (!execute(args)) {
          console.println("Unknown command. Type: 'help commands' for a list");
        }
      } catch (ServiceException e) {
        Throwable cause = e.getCause();
        errorOccursDuringInteraction = true;
        String errText =
            "ERROR: "
                + e.getCode()
                + " ("
                + e.getMessage()
                + ")"
                + (cause == null
                    ? ""
                    : " (cause: " + cause.getClass().getName() + " " + cause.getMessage() + ")");
        console.printError(errText);
        if (verboseMode) {
          console.printStacktrace(e);
        }
      } catch (ArgException | HttpException e) {
        usage();
      }
    }
  }

  /**
   * Output binary attribute to file.
   *
   * <p>value is written to:
   * {LC.zmprov_tmp_directory}/{attr-name}[_{index-if-multi-valued}]{timestamp}
   *
   * <p>e.g. /opt/zextras/data/tmp/zmprov/zimbraFoo_20110202161621
   * /opt/zextras/data/tmp/zmprov/zimbraBar_0_20110202161507
   * /opt/zextras/data/tmp/zmprov/zimbraBar_1_20110202161507
   */
  private void outputBinaryAttrToFile(String attrName, Integer idx, byte[] value, String timestamp)
      throws ServiceException {
    StringBuilder sb = new StringBuilder(LC.zmprov_tmp_directory.value());
    sb.append(File.separator).append(attrName);
    if (idx != null) {
      sb.append("_").append(idx);
    }
    sb.append("_").append(timestamp);

    File file = new File(sb.toString());
    if (file.exists()) {
      file.delete();
    }

    try {
      FileUtil.ensureDirExists(file.getParentFile());
    } catch (IOException e) {
      throw ServiceException.FAILURE(
          "Unable to create directory " + file.getParentFile().getAbsolutePath(), e);
    }

    try {
      ByteUtil.putContent(file.getAbsolutePath(), value);
    } catch (IOException e) {
      throw ServiceException.FAILURE("Unable to write to file " + file.getAbsolutePath(), e);
    }
  }

  private void printAttr(
      String attrName, String value, Integer idx, boolean isBinary, String timestamp)
      throws ServiceException {
    if (isBinary) {
      byte[] binary = ByteUtil.decodeLDAPBase64(value);
      if (outputBinaryToFile()) {
        outputBinaryAttrToFile(attrName, idx, binary, timestamp);
      } else {
        // print base64 encoded content
        // follow ldapsearch notion of using two colons when printing base64 encoded data
        // re-encode into 76 character blocks
        String based64Chunked = new String(Base64.encodeBase64Chunked(binary));
        // strip off the \n at the end
        if (based64Chunked.charAt(based64Chunked.length() - 1) == '\n') {
          based64Chunked = based64Chunked.substring(0, based64Chunked.length() - 1);
        }
        console.printOutput(attrName + ":: " + based64Chunked);
      }
    } else {
      console.printOutput(attrName + ": " + value);
    }
  }

  public static void main(String[] args) throws IOException, ServiceException {

    main(new Console(System.out, System.err), args);
  }

  public static void main(Console console, String[] args) throws IOException, ServiceException {

    CliUtil.setCliSoapHttpTransportTimeout();
    ZimbraLog.toolSetupLog4jConsole("INFO", true, false); // send all logs to stderr
    SocketFactories.registerProtocols();

    SoapTransport.setDefaultUserAgent("zmprov", BuildInfo.VERSION);

    ProvUtil pu = createProvUtil(console);
    CommandLineParser parser = new PosixParser();
    Options options = new Options();

    options.addOption("h", "help", false, "display usage");
    options.addOption("f", "file", true, "use file as input stream");
    options.addOption("s", "server", true, "host[:port] of server to connect to");
    options.addOption("l", "ldap", false, "provision via LDAP");
    options.addOption("L", "logpropertyfile", true, "log4j property file");
    options.addOption("a", "account", true, "account name (not used with --ldap)");
    options.addOption("p", "password", true, "password for account");
    options.addOption("P", "passfile", true, "filename with password in it");
    options.addOption(
        "z",
        "zadmin",
        false,
        "use zimbra admin name/password from localconfig for account/password");
    options.addOption("v", "verbose", false, "verbose mode");
    options.addOption("d", "debug", false, "debug mode (SOAP request and response payload)");
    options.addOption(
        "D", "debughigh", false, "debug mode (SOAP req/resp payload and http headers)");
    options.addOption("m", "master", false, "use LDAP master (has to be used with --ldap)");
    options.addOption(
        "t",
        "temp",
        false,
        "write binary values to files in temporary directory specified in localconfig key"
            + " zmprov_tmp_directory");
    options.addOption("r", "replace", false, "allow replacement of multi-valued attr value");
    options.addOption("fd", "forcedisplay", false, "force display attr value");
    options.addOption(SoapCLI.OPT_AUTHTOKEN);
    options.addOption(SoapCLI.OPT_AUTHTOKENFILE);

    CommandLine cl = null;
    boolean err = false;

    try {
      args = fixArgs(args);

      cl = parser.parse(options, args, true);
    } catch (ParseException pe) {
      console.printError("error: " + pe.getMessage());
      err = true;
    }

    if (err || cl.hasOption('h')) {
      pu.usage();
    }

    if (cl.hasOption('l') && cl.hasOption('s')) {
      console.printError("error: cannot specify both -l and -s at the same time");
      System.exit(2);
    }

    pu.setVerbose(cl.hasOption('v'));
    if (cl.hasOption('l')) {
      pu.setUseLdap(true, cl.hasOption('m'));
    }

    if (cl.hasOption('L')) {
      if (cl.hasOption('l')) {
        ZimbraLog.toolSetupLog4j("INFO", cl.getOptionValue('L'));
      } else {
        console.printError("error: cannot specify -L when -l is not specified");
        System.exit(2);
      }
    }

    if (cl.hasOption('z')) {
      pu.setAccount(LC.zimbra_ldap_user.value());
      pu.setPassword(LC.zimbra_ldap_password.value());
    }

    if (cl.hasOption(SoapCLI.O_AUTHTOKEN) && cl.hasOption(SoapCLI.O_AUTHTOKENFILE)) {
      console.printError(
          "error: cannot specify "
              + SoapCLI.O_AUTHTOKEN
              + " when "
              + SoapCLI.O_AUTHTOKENFILE
              + " is specified");
      System.exit(2);
    }
    if (cl.hasOption(SoapCLI.O_AUTHTOKEN)) {
      ZAuthToken zat = ZAuthToken.fromJSONString(cl.getOptionValue(SoapCLI.O_AUTHTOKEN));
      pu.setAuthToken(zat);
    }
    if (cl.hasOption(SoapCLI.O_AUTHTOKENFILE)) {
      String authToken =
          StringUtil.readSingleLineFromFile(cl.getOptionValue(SoapCLI.O_AUTHTOKENFILE));
      ZAuthToken zat = ZAuthToken.fromJSONString(authToken);
      pu.setAuthToken(zat);
    }

    if (cl.hasOption('s')) {
      pu.setServer(cl.getOptionValue('s'));
    }
    if (cl.hasOption('a')) {
      pu.setAccount(cl.getOptionValue('a'));
    }
    if (cl.hasOption('p')) {
      pu.setPassword(cl.getOptionValue('p'));
    }
    if (cl.hasOption('P')) {
      pu.setPassword(StringUtil.readSingleLineFromFile(cl.getOptionValue('P')));
    }

    if (cl.hasOption('d') && cl.hasOption('D')) {
      console.printError("error: cannot specify both -d and -D at the same time");
      System.exit(2);
    }
    if (cl.hasOption('D')) {
      pu.setDebug(SoapDebugLevel.high);
    } else if (cl.hasOption('d')) {
      pu.setDebug(SoapDebugLevel.normal);
    }

    if (!pu.useLdap() && cl.hasOption('m')) {
      console.printError("error: cannot specify -m when -l is not specified");
      System.exit(2);
    }

    if (cl.hasOption('t')) {
      pu.setOutputBinaryToFile(true);
    }

    if (cl.hasOption('r')) {
      pu.setAllowMultiValuedAttrReplacement(true);
    }

    if (cl.hasOption("fd")) {
      pu.setForceDisplayAttrValue(true);
    }

    args = recombineDecapitatedAttrs(cl.getArgs(), options, args);

    try {
      if (args.length < 1) {
        pu.initProvisioning();
        InputStream is = null;
        if (cl.hasOption('f')) {
          pu.setBatchMode(true);
          is = new FileInputStream(cl.getOptionValue('f'));
        } else {
          if (LC.command_line_editing_enabled.booleanValue()) {
            try {
              CliUtil.enableCommandLineEditing(LC.zimbra_home.value() + "/.zmprov_history");
            } catch (IOException e) {
              console.println("Command line editing will be disabled: " + e);
              if (pu.verboseMode) {
                console.printStacktrace(e);
              }
            }
          }

          // This has to happen last because JLine modifies System.in.
          is = System.in;
        }
        pu.interactive(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
      } else {
        Command cmd = pu.lookupCommand(args[0]);
        if (cmd == null) {
          pu.usage();
        }
        if (cmd.isDeprecated()) {
          pu.deprecated();
        }
        if (pu.forceLdapButDontRequireUseLdapOption(cmd)) {
          pu.setUseLdap(true, false);
        }

        if (pu.needProvisioningInstance(cmd)) {
          pu.initProvisioning();
        }

        try {
          if (!pu.execute(args)) {
            pu.usage();
          }
        } catch (ArgException | HttpException e) {
          pu.usage();
        }
      }
    } catch (ServiceException e) {
      Throwable cause = e.getCause();
      String errText =
          "ERROR: "
              + e.getCode()
              + " ("
              + e.getMessage()
              + ")"
              + (cause == null
                  ? ""
                  : " (cause: " + cause.getClass().getName() + " " + cause.getMessage() + ")");

      console.printError(errText);

      if (pu.verboseMode) {
        console.printStacktrace(e);
      }
      System.exit(2);
    }
  }

  private static String[] fixArgs(String[] args) {
    // if prov is the first argument remove it
    if ((args.length > 0) && (args[0].equalsIgnoreCase("prov")))
      args = Arrays.copyOfRange(args, 1, args.length);

    return args;
  }

  private void doGetAllMtaAuthURLs() throws ServiceException {
    List<Server> servers = prov.getAllServers();
    for (Server server : servers) {
      boolean isTarget = server.getBooleanAttr(Provisioning.A_zimbraMtaAuthTarget, false);
      if (isTarget) {
        String url = URLUtil.getMtaAuthURL(server) + " ";
        console.print(url);
      }
    }
    console.println();
  }

  private void doGetAllReverseProxyURLs() throws ServiceException {
    String REVERSE_PROXY_PROTO = ""; // don't need proto for nginx.conf
    String REVERSE_PROXY_PATH = ExtensionDispatcherServlet.EXTENSION_PATH + "/nginx-lookup";

    List<Server> servers = prov.getAllMailClientServers();
    for (Server server : servers) {
      int port = server.getIntAttr(Provisioning.A_zimbraExtensionBindPort, 7072);
      boolean isTarget =
          server.getBooleanAttr(Provisioning.A_zimbraReverseProxyLookupTarget, false);
      if (isTarget) {
        String serviceName = server.getAttr(Provisioning.A_zimbraServiceHostname, "");
        console.print(REVERSE_PROXY_PROTO + serviceName + ":" + port + REVERSE_PROXY_PATH + " ");
      }
    }
    console.println();
  }

  private void doGetAllReverseProxyBackends() throws ServiceException {
    List<Server> servers = prov.getAllServers();
    boolean atLeastOne = false;
    for (Server server : servers) {
      boolean isTarget =
          server.getBooleanAttr(Provisioning.A_zimbraReverseProxyLookupTarget, false);
      if (!isTarget) {
        continue;
      }

      // (For now) assume HTTP can be load balanced to...
      String mode = server.getAttr(Provisioning.A_zimbraMailMode, null);
      if (mode == null) {
        continue;
      }
      MailMode mailMode = Provisioning.MailMode.fromString(mode);

      boolean isPlain =
          (mailMode == Provisioning.MailMode.http
              || (!LC.zimbra_require_interprocess_security.booleanValue()
                  && (mailMode == Provisioning.MailMode.mixed
                      || mailMode == Provisioning.MailMode.both)));

      int backendPort;
      if (isPlain) {
        backendPort = server.getIntAttr(Provisioning.A_zimbraMailPort, 0);
      } else {
        backendPort = server.getIntAttr(Provisioning.A_zimbraMailSSLPort, 0);
      }

      String serviceName = server.getAttr(Provisioning.A_zimbraServiceHostname, "");
      console.println("    server " + serviceName + ":" + backendPort + ";");
      atLeastOne = true;
    }

    if (!atLeastOne) {
      // workaround zmmtaconfig not being able to deal with empty output
      console.println("    server localhost:8080;");
    }
  }

  private void doGetAllReverseProxyDomains() throws ServiceException {

    NamedEntry.Visitor visitor =
        entry -> {
          if (entry.getAttr(Provisioning.A_zimbraVirtualHostname) != null
              && entry.getAttr(Provisioning.A_zimbraSSLPrivateKey) != null
              && entry.getAttr(Provisioning.A_zimbraSSLCertificate) != null) {
            StringBuilder virtualHosts = new StringBuilder();
            for (String vh : entry.getMultiAttr(Provisioning.A_zimbraVirtualHostname)) {
              virtualHosts.append(vh).append(" ");
            }
            console.println(entry.getName() + " " + virtualHosts);
          }
        };

    prov.getAllDomains(
        visitor,
        new String[] {
          Provisioning.A_zimbraVirtualHostname,
          Provisioning.A_zimbraSSLPrivateKey,
          Provisioning.A_zimbraSSLCertificate
        });
  }

  private void doGetAllMemcachedServers() throws ServiceException {
    List<Server> servers = prov.getAllServers(Provisioning.SERVICE_MEMCACHED);
    for (Server server : servers) {
      console.print(
          server.getAttr(Provisioning.A_zimbraMemcachedBindAddress, "")
              + ":"
              + server.getAttr(Provisioning.A_zimbraMemcachedBindPort, "")
              + " ");
    }
    console.println();
  }

  private List<Pair<String /* hostname */, Integer /* port */>> getMailboxServersFromArgs(
      String[] args) throws ServiceException {
    List<Pair<String, Integer>> entries = new ArrayList<>();
    if (args.length == 2 && "all".equalsIgnoreCase(args[1])) {
      // Get all mailbox servers.
      List<Server> servers = prov.getAllMailClientServers();
      for (Server svr : servers) {
        String host = svr.getAttr(Provisioning.A_zimbraServiceHostname);
        int port = (int) svr.getLongAttr(Provisioning.A_zimbraAdminPort, serverPort);
        Pair<String, Integer> entry = new Pair<>(host, port);
        entries.add(entry);
      }
    } else {
      // Only named servers.
      for (int i = 1; i < args.length; ++i) {
        String arg = args[i];
        if (serverHostname.equalsIgnoreCase(arg)) {
          entries.add(new Pair<>(serverHostname, serverPort));
        } else {
          Server svr = prov.getServerByServiceHostname(arg);
          if (svr == null) {
            throw AccountServiceException.NO_SUCH_SERVER(arg);
          }
          // TODO: Verify svr has mailbox service enabled.
          int port = (int) svr.getLongAttr(Provisioning.A_zimbraAdminPort, serverPort);
          entries.add(new Pair<>(arg, port));
        }
      }
    }
    return entries;
  }

  private void doReloadMemcachedClientConfig(String[] args) throws ServiceException {
    List<Pair<String, Integer>> servers = getMailboxServersFromArgs(args);
    // Send command to each server.
    for (Pair<String, Integer> server : servers) {
      String hostname = server.getFirst();
      int port = server.getSecond();
      if (verboseMode) {
        console.print("Updating " + hostname + " ... ");
      }
      boolean success = false;
      try {
        SoapProvisioning sp = new SoapProvisioning();
        sp.soapSetURI(
            LC.zimbra_admin_service_scheme.value()
                + hostname
                + ":"
                + port
                + AdminConstants.ADMIN_SERVICE_URI);
        if (debugLevel != SoapDebugLevel.none) {
          sp.soapSetHttpTransportDebugListener(this);
        }
        if (account != null && password != null) {
          sp.soapAdminAuthenticate(account, password);
        } else if (authToken != null) {
          sp.soapAdminAuthenticate(authToken);
        } else {
          sp.soapZimbraAdminAuthenticate();
        }
        sp.reloadMemcachedClientConfig();
        success = true;
      } catch (ServiceException e) {
        if (verboseMode) {
          console.println("fail");
          console.printStacktrace(e);
        } else {
          console.println("Error updating " + hostname + ": " + e.getMessage());
        }
      } finally {
        if (verboseMode && success) {
          console.println("ok");
        }
      }
    }
  }

  private void doGetMemcachedClientConfig(String[] args) throws ServiceException {
    List<Pair<String, Integer>> servers = getMailboxServersFromArgs(args);
    // Send command to each server.
    int longestHostname = 0;
    for (Pair<String, Integer> server : servers) {
      String hostname = server.getFirst();
      longestHostname = Math.max(longestHostname, hostname.length());
    }
    String hostnameFormat = String.format("%%-%ds", longestHostname);
    boolean consistent = true;
    String prevConf = null;
    for (Pair<String, Integer> server : servers) {
      String hostname = server.getFirst();
      int port = server.getSecond();
      try {
        SoapProvisioning sp = new SoapProvisioning();
        sp.soapSetURI(
            LC.zimbra_admin_service_scheme.value()
                + hostname
                + ":"
                + port
                + AdminConstants.ADMIN_SERVICE_URI);
        if (debugLevel != SoapDebugLevel.none) {
          sp.soapSetHttpTransportDebugListener(this);
        }
        if (account != null && password != null) {
          sp.soapAdminAuthenticate(account, password);
        } else if (authToken != null) {
          sp.soapAdminAuthenticate(authToken);
        } else {
          sp.soapZimbraAdminAuthenticate();
        }
        MemcachedClientConfig config = sp.getMemcachedClientConfig();
        String serverList = config.serverList != null ? config.serverList : "none";
        if (verboseMode) {
          console.print(String.format(
              hostnameFormat
                  + " => serverList=[%s], hashAlgo=%s, binaryProto=%s,"
                  + " expiry=%ds, timeout=%dms\n",
              hostname,
              serverList,
              config.hashAlgorithm,
              config.binaryProtocol,
              config.defaultExpirySeconds,
              config.defaultTimeoutMillis));
        } else if (config.serverList != null) {
          if (DefaultHashAlgorithm.KETAMA_HASH.toString().equals(config.hashAlgorithm)) {
            // Don't print the default hash algorithm to keep the output clutter-free.
            console.print(String.format(hostnameFormat + " => %s\n", hostname, serverList));
          } else {
            console.print(String.format(
                hostnameFormat + " => %s (%S)\n", hostname, serverList, config.hashAlgorithm));
          }
        } else {
          console.print(String.format(hostnameFormat + " => none\n", hostname));
        }

        String listAndAlgo = serverList + "/" + config.hashAlgorithm;
        if (prevConf == null) {
          prevConf = listAndAlgo;
        } else if (!prevConf.equals(listAndAlgo)) {
          consistent = false;
        }
      } catch (ServiceException e) {
        console.print(String.format(hostnameFormat + " => ERROR: unable to get configuration\n", hostname));
        if (verboseMode) {
          console.printStacktrace(e);
        }
      }
    }
    if (!consistent) {
      console.println("Inconsistency detected!");
    }
  }

  private void doPurgeAccountCalendarCache(String[] args) throws ServiceException {
    if (!(prov instanceof SoapProvisioning)) {
      throwSoapOnly();
    }
    if (args.length > 1) {
      for (int i = 1; i < args.length; i++) {
        Account acct = lookupAccount(args[i], true);
        prov.purgeAccountCalendarCache(acct.getId());
      }
    }
  }

  public void dumpEffectiveRight(
          RightCommand.EffectiveRights effRights, boolean expandSetAttrs, boolean expandGetAttrs) {

    List<String> presetRights = effRights.presetRights();
    if (presetRights != null && presetRights.size() > 0) {
      console.println("================");
      console.println("Preset rights");
      console.println("================");
      for (String r : presetRights) {
        console.println("    " + r);
      }
    }

    displayAttrs("set", expandSetAttrs, effRights.canSetAllAttrs(), effRights.canSetAttrs());
    displayAttrs("get", expandGetAttrs, effRights.canGetAllAttrs(), effRights.canGetAttrs());

    console.println();
    console.println();
  }

  public void displayAttrs(
          String op,
          boolean expandAll,
          boolean allAttrs,
          SortedMap<String, RightCommand.EffectiveAttr> attrs) {
    if (!allAttrs && attrs.isEmpty()) {
      return;
    }
    String format = "    %-50s %-30s\n";
    console.println();
    console.println("=========================");
    console.println(op + " attributes rights");
    console.println("=========================");
    if (allAttrs) {
      console.println("Can " + op + " all attributes");
    }
    if (!allAttrs || expandAll) {
      console.println("Can " + op + " the following attributes");
      console.println("--------------------------------");
      console.print(String.format(format, "attribute", "default"));
      console.print(String.format(format, "----------------------------------------", "--------------------"));
      for (RightCommand.EffectiveAttr ea : attrs.values()) {
        boolean first = true;
        if (ea.getDefault().isEmpty()) {
          console.print(String.format(format, ea.getAttrName(), ""));
        } else {
          for (String v : ea.getDefault()) {
            if (first) {
              console.print(String.format(format, ea.getAttrName(), v));
              first = false;
            } else {
              console.print(String.format(format, "", v));
            }
          }
        }
      }
    }
  }

  private void doGetAuthTokenInfo(String[] args) {
    String authToken = args[1];

    try {
      Map attrs = AuthToken.getInfo(authToken);
      List keys = new ArrayList(attrs.keySet());
      Collections.sort(keys);

      for (Object k : keys) {
        String key = k.toString();
        String value = attrs.get(k).toString();

        if ("exp".equals(key)) {
          long exp = Long.parseLong(value);
          console.print(String.format("%s: %s (%s)\n", key, value, DateUtil.toRFC822Date(new Date(exp))));
        } else {
          console.print(String.format("%s: %s\n", key, value));
        }
      }
    } catch (AuthTokenException e) {
      console.println("Unable to parse auth token: " + e.getMessage());
    }

    console.println();
  }

  private void doPushFreeBusy(String[] args) throws ServiceException, IOException, HttpException {
    FbCli fbcli = new FbCli();
    Map<String, HashSet<String>> accountMap = new HashMap<>();
    for (int i = 1; i < args.length; i++) {
      String acct = args[i];
      Account account = prov.getAccountById(acct);
      if (account == null) {
        throw AccountServiceException.NO_SUCH_ACCOUNT(acct);
      }
      String host = account.getMailHost();
      HashSet<String> accountSet = accountMap.get(host);
      if (accountSet == null) {
        accountSet = new HashSet<>();
        accountMap.put(host, accountSet);
      }
      accountSet.add(acct);
    }
    for (String host : accountMap.keySet()) {
      console.println("pushing to server " + host);
      fbcli.setServer(host);
      fbcli.pushFreeBusyForAccounts(accountMap.get(host));
    }
  }

  private void doPushFreeBusyForDomain(String[] args)
      throws ServiceException, IOException, HttpException {
    lookupDomain(args[1]);
    FbCli fbcli = new FbCli();
    for (Server server : prov.getAllMailClientServers()) {
      console.println("pushing to server " + server.getName());
      fbcli.setServer(server.getName());
      fbcli.pushFreeBusyForDomain(args[1]);
    }
  }

  private void doPurgeFreeBusyQueue(String[] args)
      throws ServiceException, IOException, HttpException {
    String provider = null;
    if (args.length > 1) {
      provider = args[1];
    }
    FbCli fbcli = new FbCli();
    fbcli.purgeFreeBusyQueue(provider);
  }

  private void doHelp(String[] args) {
    Category cat = null;
    if (args != null && args.length >= 2) {
      String s = args[1].toUpperCase();
      try {
        cat = Category.valueOf(s);
      } catch (IllegalArgumentException e) {
        for (Category c : Category.values()) {
          if (c.name().startsWith(s)) {
            cat = c;
            break;
          }
        }
      }
    }

    if (args == null || args.length == 1 || cat == null) {
      console.println(" zmprov is used for provisioning. Try:");
      console.println("");
      for (Category c : Category.values()) {
        console.print(String.format("     zmprov help %-15s %s\n", c.name().toLowerCase(), c.getDescription()));
      }
    }

    if (cat != null) {
      console.println("");
      for (Command c : Command.values()) {
        if (!c.hasHelp()) {
          continue;
        }
        if (cat == Category.COMMANDS || cat == c.getCategory()) {
          Command.Via via = c.getVia();
          console.print(String.format("  %s(%s) %s\n", c.getName(), c.getAlias(), c.getHelp()));
          if (via == Command.Via.ldap) {
            console.print(String.format(
                "    -- NOTE: %s can only be used with \"zmprov -l/--ldap\"\n", c.getName()));
          }
          console.println();
        }
      }

      console.println(helpCategory(cat));
    }
    console.println();
  }

  @Override
  public void receiveSoapMessage(HttpPost postMethod, Element envelope) {
    console.print("======== SOAP RECEIVE =========\n");

    if (debugLevel == SoapDebugLevel.high) {
      Header[] headers = postMethod.getAllHeaders();
      for (Header header : headers) {
        console.println(header.toString().trim()); // trim the ending crlf
      }
    }

    long end = System.currentTimeMillis();
    console.println(envelope.prettyPrint());
    console.print(String.format("=============================== (%d msecs)\n", end - sendStart));
  }

  @Override
  public void sendSoapMessage(HttpPost postMethod, Element envelope, BasicCookieStore httpState) {
    console.println("========== SOAP SEND ==========");

    if (debugLevel == SoapDebugLevel.high) {

      URI uri = postMethod.getURI();
      console.println(uri.toString());
      Header[] headers = postMethod.getAllHeaders();
      for (Header header : headers) {
        console.println(header.toString().trim()); // trim the ending crlf
      }
      console.println();
    }

    sendStart = System.currentTimeMillis();

    console.println(envelope.prettyPrint());
    console.println("===============================");
  }

  public void throwSoapOnly() throws ServiceException {
    throw ServiceException.INVALID_REQUEST(ERR_VIA_SOAP_ONLY, null);
  }

  private void throwLdapOnly() throws ServiceException {
    throw ServiceException.INVALID_REQUEST(ERR_VIA_LDAP_ONLY, null);
  }

  private void loadLdapSchemaExtensionAttrs() {
    if (prov instanceof LdapProv) {
      AttributeManager.loadLdapSchemaExtensionAttrs((LdapProv) prov);
    }
  }

  /**
   * To remove a particular instance of an attribute, the prefix indicator '-' is used before the
   * attribute name. When the attribute name is started with one of the valid command arguments,
   * such as -z or -a, the parser mistakenly divides it into two parts instead of treating as one
   * parameter of the '-' and attribute name.
   *
   * <p>This method detects such decapitated attribute, and recombines those two into one attribute
   * name with '-'.
   *
   * @param parsedArgs [cmd-args] which are parsed by PosixParser
   * @param options set of valid [args]
   * @throws ServiceException
   */
  private static String[] recombineDecapitatedAttrs(
      String[] parsedArgs, Options options, String[] orgArgs) {
    List<String> newArgs = new ArrayList<>(parsedArgs.length);
    String headStr = null;
    for (int i = 0; i < parsedArgs.length; i++) {
      String arg = parsedArgs[i];
      if (arg.startsWith("-") && arg.length() == 2 && options.hasOption(arg)) {
        // Detect legitimate POSIX style parameters even after operation command;
        // such as "zmprov describe -a <attr>"
        if (i < parsedArgs.length - 1) {
          boolean missParsed = false;
          String tmpParam = arg + parsedArgs[i + 1];
          for (String orgArg : orgArgs) {
            if (orgArg.equals(tmpParam)) {
              missParsed = true;
              break;
            }
          }
          if (missParsed) {
            headStr = arg;
          } else {
            newArgs.add(arg);
          }
        } else {
          newArgs.add(arg);
        }
      } else if (headStr != null) {
        newArgs.add(headStr + arg);
        headStr = null;
      } else {
        newArgs.add(arg);
      }
    }
    return newArgs.toArray(new String[0]);
  }

  private void doDeleteDistributionList(String[] args) throws ServiceException {
    String groupId = lookupGroup(args[1]).getId();
    Boolean cascadeDelete = false;
    if (args.length > 2) {
      cascadeDelete = Boolean.valueOf(args[2]) != null ? Boolean.valueOf(args[2]) : false;
    }
    prov.deleteGroup(groupId, cascadeDelete);
  }

  public String helpCategory(Category cat) {
    switch (cat) {
      case CALENDAR:
        return helpCALENDAR();
      case RIGHT:
        return helpRIGHT();
      case LOG:
        return helpLOG();
      default:
        return "";
    }
  }

  String helpCALENDAR() {
    StringBuilder help = new StringBuilder();
    help.append("\n");
    StringBuilder sb = new StringBuilder();
    EntrySearchFilter.Operator[] vals = EntrySearchFilter.Operator.values();
    for (int i = 0; i < vals.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(vals[i].toString());
    }
    help.append("    op = " + sb);
    help.append("\n");
    return help.toString();
  }

  String helpRIGHT() {
    return helpRIGHTCommon(true) +
    helpRIGHTRights(false, true);
  }

  static String helpRIGHTCommand(boolean secretPossible, boolean modifierPossible) {
    return
        helpRIGHTCommon(secretPossible) +
        helpRIGHTRights(false, modifierPossible);
  }

  static String helpRIGHTRights(boolean printRights, boolean modifierPossible) {
    // rights
    StringBuilder help = new StringBuilder();
    help.append("\n");
    if (modifierPossible) {
      help.append("    {right}: can have the following prefixes:");
      help.append("\n");
      for (RightModifier rm : RightModifier.values()) {
        help.append("            " + rm.getModifier() + " : " + rm.getDescription());
        help.append("\n");
      }
      help.append("\n");
    }

    if (printRights) {
      try {
        Map<String, AdminRight> allAdminRights = RightManager.getInstance().getAllAdminRights();
        // print non-combo rights first
        for (com.zimbra.cs.account.accesscontrol.Right r : allAdminRights.values()) {
          if (RightType.combo != r.getRightType()) {
            help.append("        " + r.getName() + " (" + r.getRightType().toString() + ")");
            help.append("\n");
          }
        }
        // then combo rights
        for (com.zimbra.cs.account.accesscontrol.Right r : allAdminRights.values()) {
          if (RightType.combo == r.getRightType()) {
            help.append("        " + r.getName() + " (" + r.getRightType().toString() + ")");
            help.append("\n");
          }
        }
      } catch (ServiceException e) {
        help.append("cannot get RightManager instance: " + e.getMessage());
        help.append("\n");
      }
    } else {
      help.append("         for complete list of rights, do \"zmprov gar -c ALL\"");
      help.append("\n");
    }
    help.append("\n");
    return help.toString();
  }

  static String helpRIGHTCommon(boolean secretPossible) {
    // target types
    StringBuilder targetTypes = new StringBuilder();
    targetTypes.append("\n");
    StringBuilder ttNeedsTargetIdentity = new StringBuilder();
    StringBuilder ttNoTargetId = new StringBuilder();
    TargetType[] tts = TargetType.values();
    for (int i = 0; i < tts.length; i++) {
      if (i > 0) {
        targetTypes.append(", ");
      }
      targetTypes.append(tts[i].getCode());
      if (tts[i].needsTargetIdentity()) {
        ttNeedsTargetIdentity.append(tts[i].getCode()).append(" ");
      } else {
        ttNoTargetId.append(tts[i].getCode()).append(" ");
      }
    }
    StringBuilder help = new StringBuilder();
    help.append("    {target-type} = " + targetTypes);
    help.append("\n");
    help.append(
        "    {target-id|target-name} is required if target-type is: " + ttNeedsTargetIdentity);
    help.append("\n");
    help.append(
        "    {target-id|target-name} should not be specified if target-type is: " + ttNoTargetId);

    // grantee types
    help.append("\n");
    StringBuilder gt = new StringBuilder();
    StringBuilder gtNeedsGranteeIdentity = new StringBuilder();
    StringBuilder gtNoGranteeId = new StringBuilder();
    StringBuilder gtNeedsSecret = new StringBuilder();
    StringBuilder gtNoSecret = new StringBuilder();
    GranteeType[] gts = GranteeType.values();
    for (int i = 0; i < gts.length; i++) {
      if (i > 0) {
        gt.append(", ");
      }
      gt.append(gts[i].getCode());
      if (gts[i].needsGranteeIdentity()) {
        gtNeedsGranteeIdentity.append(gts[i].getCode()).append(" ");
      } else {
        gtNoGranteeId.append(gts[i].getCode()).append(" ");
      }
      if (secretPossible) {
        if (gts[i].allowSecret()) {
          gtNeedsSecret.append(gts[i].getCode()).append(" ");
        } else {
          gtNoSecret.append(gts[i].getCode()).append(" ");
        }
      }
    }
    help.append("    {grantee-type} = " + gt);
    help.append("\n");
    help.append(
        "    {grantee-id|grantee-name} is required if grantee-type is one of: "
            + gtNeedsGranteeIdentity);
    help.append("\n");
    help.append(
        "    {grantee-id|grantee-name} should not be specified if grantee-type is one"
            + " of: "
            + gtNoGranteeId);
    help.append("\n");
    if (secretPossible) {
      help.append("    {secret} is required if grantee-type is one of: " + gtNeedsSecret);
      help.append("\n");
      help.append(
          "    {secret} should not be specified if grantee-type is one of: " + gtNoSecret);
      help.append("\n");
    }
    return help.toString();
  }

  String helpLOG() {
    StringBuilder help = new StringBuilder();
    help.append("    Log categories:");
    help.append("\n");
    int maxNameLength = 0;
    for (String name : ZimbraLog.CATEGORY_DESCRIPTIONS.keySet()) {
      if (name.length() > maxNameLength) {
        maxNameLength = name.length();
      }
    }
    for (String name : ZimbraLog.CATEGORY_DESCRIPTIONS.keySet()) {
      help.append("        ");
      help.append(name);
      help.append("\n");
      for (int i = 0; i < (maxNameLength - name.length()); i++) {
        help.append(" ");
        help.append("\n");
      }
      help.append(String.format(" - %s\n", ZimbraLog.CATEGORY_DESCRIPTIONS.get(name)));
      help.append("\n");
    }
    return help.toString();
  }

  public Provisioning getProvisioning() {
    return prov;
  }

  public Console getConsole() {
    return console;
  }

  public boolean getErrorOccursDuringInteraction() {
    return errorOccursDuringInteraction;
  }
}
