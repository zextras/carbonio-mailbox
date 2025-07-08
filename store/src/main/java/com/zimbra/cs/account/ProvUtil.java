// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.zimbra.common.account.Key;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.net.SocketFactories;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport.HttpDebugListener;
import com.zimbra.common.soap.SoapTransport;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.CliUtil;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right.RightType;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.cs.account.commands.ProvUtilCommandHandlersFactory;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.UsageException;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.util.BuildInfo;
import com.zimbra.cs.util.SoapCLI;
import com.zimbra.soap.admin.type.GranteeSelector.GranteeBy;
import com.zimbra.soap.type.TargetBy;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * @author schemers
 */
public class ProvUtil implements HttpDebugListener, ProvUtilDumperOptions {

  private static final String ERR_VIA_SOAP_ONLY = "can only be used with SOAP";
  private static final String ERR_VIA_LDAP_ONLY = "can only be used with  \"zmprov -l/--ldap\"";
  public static final String ERR_INVALID_ARG_EV = "arg -e is invalid unless -v is also specified";
  private final Console console;
  public enum SoapDebugLevel {
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
  private final Map<Command, CommandHandler> handlersMap;

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

  public boolean outputBinaryToFile() {
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

  public void usageWithException() throws UsageException {
    usage(null);
  }

  /**
   * @deprecated: use {@link #usageWithException()}
   */
  @Deprecated
  public void usageWithExit1() {
		try {
			usageWithException();
		} catch (UsageException e) {
			System.exit(1);
		}
	}

  private void usage(Command.Via violatedVia) throws UsageException {
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
      throw new UsageException();
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
    throw new UsageException();
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
    handlersMap = ProvUtilCommandHandlersFactory.getCommandHandlersMap(this);
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

  private boolean execute(String[] args)
			throws ServiceException, ArgException, IOException, HttpException, UsageException {
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
      usageWithExit1();
      return true;
    }
    if (command.needsSchemaExtension()) {
      loadLdapSchemaExtensionAttrs();
    }
    var handler = handlersMap.get(command);
    if (handler == null) {
      return false;
    }
    handler.handle(args);
    return true;
  }

  public boolean confirm(String msg) {
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

  public Account lookupAccount(String key, boolean mustFind, boolean applyDefault)
      throws ServiceException {
    Account account;
    if (applyDefault || (prov instanceof LdapProv)) {
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

  public Account lookupAccount(String key, boolean mustFind) throws ServiceException {
    return lookupAccount(key, mustFind, true);
  }

  public CalendarResource lookupCalendarResource(String key) throws ServiceException {
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

  public Group lookupGroup(String key, boolean mustFind) throws ServiceException {
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
      } catch (UsageException e) {
        System.exit(1);
      }
      catch (ServiceException e) {
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
        usageWithExit1();
      }
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
      pu.usageWithExit1();
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
          pu.usageWithExit1();
        }
        if (cmd.isDeprecated()) {
          pu.console.println("This command has been deprecated.");
          System.exit(1);
        }
        if (pu.forceLdapButDontRequireUseLdapOption(cmd)) {
          pu.setUseLdap(true, false);
        }

        if (pu.needProvisioningInstance(cmd)) {
          pu.initProvisioning();
        }

        try {
          if (!pu.execute(args)) {
            pu.usageWithException();
          }
        } catch (UsageException e) {
          System.exit(1);
        }
        catch (ArgException | HttpException e) {
          pu.usageWithExit1();
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

  public List<Pair<String /* hostname */, Integer /* port */>> getMailboxServersFromArgs(
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
    TargetType[] tts = TargetType.valuesWithoutXmppComponent();
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

  public void setProvisioning(Provisioning prov) {
    this.prov = prov;
  }

  public Console getConsole() {
    return console;
  }

  public boolean getErrorOccursDuringInteraction() {
    return errorOccursDuringInteraction;
  }

  public boolean getUseLdapMaster() {
    return useLdapMaster;
  }

  public boolean getInteractiveMode() {
    return interactiveMode;
  }

  public boolean getVerboseMode() {
    return verboseMode;
  }

  public SoapDebugLevel getDebugLevel() {
    return debugLevel;
  }

  public BufferedReader getCliReader() {
    return cliReader;
  }

  public String getAccount() {
    return account;
  }

  public String getPassword() {
    return password;
  }

  public ZAuthToken getAuthToken() {
    return authToken;
  }

  public int getServerPort() {
    return serverPort;
  }

  @Override public boolean getForceDisplayAttrValue() {
    return forceDisplayAttrValue;
  }
}
