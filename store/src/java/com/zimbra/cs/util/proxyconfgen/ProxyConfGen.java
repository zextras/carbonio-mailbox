// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.CliUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.util.BuildInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ProxyConfGen {
  static final String ZIMBRA_USER = "zextras";
  static final String ZIMBRA_UPSTREAM_NAME = "zimbra";
  static final String ZIMBRA_UPSTREAM_WEBCLIENT_NAME = "zimbra_webclient";
  static final String ZIMBRA_SSL_UPSTREAM_NAME = "zimbra_ssl";
  static final String ZIMBRA_SSL_UPSTREAM_WEBCLIENT_NAME = "zimbra_ssl_webclient";
  static final String ZIMBRA_ADMIN_CONSOLE_UPSTREAM_NAME = "zimbra_admin";
  static final String ZIMBRA_ADMIN_CONSOLE_CLIENT_UPSTREAM_NAME = "zimbra_adminclient";
  static final String ZIMBRA_UPSTREAM_EWS_NAME = "zimbra_ews";
  static final String ZIMBRA_SSL_UPSTREAM_EWS_NAME = "zimbra_ews_ssl";
  static final String ZIMBRA_UPSTREAM_LOGIN_NAME = "zimbra_login";
  static final String ZIMBRA_SSL_UPSTREAM_LOGIN_NAME = "zimbra_login_ssl";
  static final String ZIMBRA_UPSTREAM_ZX_NAME = "zx";
  static final String ZIMBRA_SSL_UPSTREAM_ZX_NAME = "zx_ssl";
  static final int ZIMBRA_UPSTREAM_ZX_PORT = 8742;
  static final int ZIMBRA_UPSTREAM_SSL_ZX_PORT = 8743;
  private static final int DEFAULT_SERVERS_NAME_HASH_MAX_SIZE = 512;
  private static final int DEFAULT_SERVERS_NAME_HASH_BUCKET_SIZE = 64;
  private static final Log LOG = LogFactory.getLog(ProxyConfGen.class);
  private static final Options mOptions = new Options();
  private static final String SSL_CRT_EXT = ".crt";
  private static final String SSL_KEY_EXT = ".key";
  private static final String SSL_CLIENT_CERT_CA_EXT = ".client.ca.crt";
  private static final String TEMPLATE_SUFFIX = ".template";
  private static final Map<String, ProxyConfVar> mConfVars = new HashMap<>();
  private static final Map<String, String> mVars = new HashMap<>();
  private static final Map<String, ProxyConfVar> mDomainConfVars = new HashMap<>();
  /** the pattern for custom header cmd, such as "!{explode domain} */
  private static final Pattern CMD_PATTERN =
      Pattern.compile("(.*)!\\{([^}]+)}(.*)", Pattern.DOTALL);

  static List<DomainAttrItem> mDomainReverseProxyAttrs;
  static List<ServerAttrItem> mServerAttrs;
  static Set<String> mListenAddresses = new HashSet<>();
  private static boolean mDryRun = false;
  private static boolean mEnforceDNSResolution = false;
  private static String mWorkingDir = "/opt/zextras";
  static final String OVERRIDE_TEMPLATE_DIR = mWorkingDir + "/conf/nginx/templates_custom";
  static String mTemplateDir = mWorkingDir + "/conf/nginx/templates";
  private static String mConfDir = mWorkingDir + "/conf";
  private static final String DOMAIN_SSL_DIR = mConfDir + File.separator + "domaincerts";
  private static final String DEFAULT_SSL_CRT = mConfDir + File.separator + "nginx.crt";
  private static final String DEFAULT_SSL_KEY = mConfDir + File.separator + "nginx.key";
  private static final String DEFAULT_SSL_CLIENT_CERT_CA =
      mConfDir + File.separator + "nginx.client.ca.crt";
  private static final String DEFAULT_DH_PARAM_FILE = mConfDir + File.separator + "dhparam.pem";
  private static String mIncDir = "nginx/includes";
  private static String mConfIncludesDir = mConfDir + File.separator + mIncDir;
  private static String mConfPrefix = "nginx.conf";
  private static String mTemplatePrefix = mConfPrefix;
  private static Provisioning mProv = null;
  private static boolean mGenConfPerVhn = false;
  private static boolean hasCustomTemplateLocationArg = false;

  static {
    mOptions.addOption("h", "help", false, "show this usage text");
    mOptions.addOption("v", "verbose", false, "be verbose");

    mOptions.addOption("w", "workdir", true, "Proxy Working Directory (defaults to /opt/zextras)");
    mOptions.addOption(
        "t",
        "templatedir",
        true,
        "Proxy Template Directory (defaults to $workdir/conf/nginx/templates)");
    mOptions.addOption(
        "n",
        "dry-run",
        false,
        "Do not write any configuration, just show which files would be written");
    mOptions.addOption("d", "defaults", false, "Print default variable map");
    mOptions.addOption(
        "D",
        "definitions",
        false,
        "Print variable map Definitions after loading LDAP configuration (and processing"
            + " overrides). -D requires -s upstream server. If \"-s upstream server\" is not"
            + " specified, it just dumps the default variable map");
    mOptions.addOption("p", "prefix", true, "Config File prefix (defaults to nginx.conf)");
    mOptions.addOption("P", "template-prefix", true, "Template File prefix (defaults to $prefix)");
    mOptions.addOption(
        "i",
        "include-dir",
        true,
        "Directory Path (relative to $workdir/conf), where included configuration files will be"
            + " written. Defaults to nginx/includes");
    mOptions.addOption(
        "s",
        "server",
        true,
        "If provided, this should be the name of a valid server object. Configuration will be"
            + " generated based on server attributes. Otherwise, if not provided, Configuration"
            + " will be generated based on Global configuration values");
    mOptions.addOption(
        "f",
        "force-dns-resolution",
        false,
        "Force configuration generation to stop if DNS resolution failure of any hostnames is"
            + " detected. Defaults to 'false'");

    Option cOpt =
        new Option(
            "c",
            "config",
            true,
            "Override a config variable. Argument format must be name=value. For list of names, run"
                + " with -d or -D");
    cOpt.setArgs(Option.UNLIMITED_VALUES);
    mOptions.addOption(cOpt);
  }

  private static void usage(String errorMsg) {
    if (errorMsg != null) {
      System.out.println(errorMsg);
    }
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(
        "ProxyConfGen [options] ",
        "where [options] are one of:",
        mOptions,
        "ProxyConfGen generates the NGINX Proxy configuration files");
  }

  private static CommandLine parseArgs(String[] args) {
    CommandLineParser parser = new GnuParser();
    CommandLine cl;
    try {
      cl = parser.parse(mOptions, args, false);
    } catch (ParseException pe) {
      usage(pe.getMessage());
      return null;
    }
    return cl;
  }

  /**
   * Retrieve all server and store only few attrs
   *
   * @return a list of <code>ServerAttrItem</code>
   * @throws ServiceException this method can work only when LDAP is available
   * @author Davide Baldo
   */
  private static List<ServerAttrItem> loadServerAttrs() throws ServiceException {
    if (!(mProv instanceof LdapProv))
      throw ServiceException.INVALID_REQUEST(
          "The method can work only when LDAP is available", null);

    final List<ServerAttrItem> serverAttrItems = new ArrayList<>();
    for (Server server : mProv.getAllServers()) {
      String zimbraId = server.getAttr(ZAttrProvisioning.A_zimbraId);
      String serviceHostname = server.getAttr(ZAttrProvisioning.A_zimbraServiceHostname);
      String[] services = server.getMultiAttr(ZAttrProvisioning.A_zimbraServiceEnabled);

      serverAttrItems.add(new ServerAttrItem(zimbraId, serviceHostname, services));
    }

    return serverAttrItems;
  }

  /**
   * Retrieve all the necessary domain level reverse proxy attrs, like virtualHostname, SSL
   * certificate, ... this method only when LDAP is available
   *
   * @return a list of <code>DomainAttrItem</code>
   * @throws ServiceException if Provisioning type is not LDAP
   * @author Jiankuan
   */
  private static List<DomainAttrItem> loadDomainReverseProxyAttrs() throws ServiceException {

    if (!mGenConfPerVhn) {
      return Collections.emptyList();
    }
    if (!(mProv instanceof LdapProv))
      throw ServiceException.INVALID_REQUEST(
          "The method can work only when LDAP is available", null);

    final Set<String> attrsNeeded = new HashSet<>();
    attrsNeeded.add(ZAttrProvisioning.A_zimbraVirtualHostname);
    attrsNeeded.add(ZAttrProvisioning.A_zimbraVirtualIPAddress);
    attrsNeeded.add(ZAttrProvisioning.A_zimbraSSLCertificate);
    attrsNeeded.add(ZAttrProvisioning.A_zimbraSSLPrivateKey);
    attrsNeeded.add(ZAttrProvisioning.A_zimbraReverseProxyClientCertMode);
    attrsNeeded.add(ZAttrProvisioning.A_zimbraReverseProxyClientCertCA);
    attrsNeeded.add(ZAttrProvisioning.A_zimbraWebClientLoginURL);
    attrsNeeded.add(ZAttrProvisioning.A_zimbraReverseProxyResponseHeaders);

    final List<DomainAttrItem> result = new ArrayList<>();

    // visit domains
    NamedEntry.Visitor visitor =
        entry -> {
          String domainName = entry.getAttr(ZAttrProvisioning.A_zimbraDomainName);
          String[] virtualHostnames = entry.getMultiAttr(ZAttrProvisioning.A_zimbraVirtualHostname);
          String[] virtualIPAddresses =
              entry.getMultiAttr(ZAttrProvisioning.A_zimbraVirtualIPAddress);
          String certificate = entry.getAttr(ZAttrProvisioning.A_zimbraSSLCertificate);
          String privateKey = entry.getAttr(ZAttrProvisioning.A_zimbraSSLPrivateKey);
          String clientCertMode =
              entry.getAttr(ZAttrProvisioning.A_zimbraReverseProxyClientCertMode);
          String clientCertCA = entry.getAttr(ZAttrProvisioning.A_zimbraReverseProxyClientCertCA);
          String[] rspHeaders =
              entry.getMultiAttr(ZAttrProvisioning.A_zimbraReverseProxyResponseHeaders);

          if (virtualHostnames.length == 0
              || (certificate == null
                  && privateKey == null
                  && clientCertMode == null
                  && clientCertCA == null)) {

            return; // ignore the items that don't have virtual host
            // name, cert or key. Those domains will use the
            // config
          }

          boolean lookupVIP = true; // lookup virtual IP from DNS or /etc/hosts
          if (virtualIPAddresses.length > 0) {
            Collections.addAll(mListenAddresses, virtualIPAddresses);
            lookupVIP = false;
          }

          for (int i = 0; i < virtualHostnames.length; i++) {
            // bug 66892, only lookup IP when zimbraVirtualIPAddress is unset
            String vip;
            if (lookupVIP) {
              vip = null;
            } else {
              if (virtualIPAddresses.length == virtualHostnames.length) {
                vip = virtualIPAddresses[i];
              } else {
                vip = virtualIPAddresses[0];
              }
            }

            if (!ProxyConfUtil.isEmptyString(clientCertCA)) {
              createDomainSSLDirIfNotExists();
            }
            result.add(
                new DomainAttrItem(
                    domainName,
                    virtualHostnames[i],
                    vip,
                    certificate,
                    privateKey,
                    clientCertMode,
                    clientCertCA,
                    rspHeaders));
          }
        };
    mProv.getAllDomains(visitor, attrsNeeded.toArray(new String[0]));
    return result;
  }

  /** Load all the client cert ca content */
  private static String loadAllClientCertCA() {
    // to avoid redundancy CA if some domains share the same CA
    HashSet<String> caSet = new HashSet<>();
    String globalCA =
        ProxyConfVar.serverSource.getAttr(ZAttrProvisioning.A_zimbraReverseProxyClientCertCA, "");
    if (!ProxyConfUtil.isEmptyString(globalCA)) {
      caSet.add(globalCA);
    }

    mDomainReverseProxyAttrs.stream()
        .filter(item -> !ProxyConfUtil.isEmptyString(item.clientCertCa))
        .map(item -> item.clientCertCa)
        .forEachOrdered(caSet::add);

    StringBuilder sb = new StringBuilder();
    String separator = System.getProperty("line.separator");
    caSet.forEach(
        ca -> {
          sb.append(ca);
          sb.append(separator);
        });
    if (sb.length() > separator.length()) {
      sb.setLength(sb.length() - separator.length()); // trim the last separator
    }
    return sb.toString();
  }

  public static void createDomainSSLDirIfNotExists() {
    File domainSSLDir = new File(DOMAIN_SSL_DIR);
    if (!domainSSLDir.exists()) {
      //noinspection ResultOfMethodCallIgnored
      domainSSLDir.mkdirs();
    }
  }

  /* Guess how to find a server object -- taken from ProvUtil::guessServerBy */
  public static Key.ServerBy guessServerBy(String value) {
    if (Provisioning.isUUID(value)) return Key.ServerBy.id;
    return Key.ServerBy.name;
  }

  public static Server getServer(String key) throws ProxyConfException {
    Server s;

    try {
      s = mProv.get(guessServerBy(key), key);
      if (s == null) {
        throw new ProxyConfException("Cannot find server: " + key);
      }
    } catch (ServiceException se) {
      throw new ProxyConfException("Error getting server: " + se.getMessage());
    }

    return s;
  }

  private static String getCoreConf() {
    return mConfPrefix;
  }

  private static String getCoreConfTemplate() {
    return mTemplatePrefix + TEMPLATE_SUFFIX;
  }

  private static String getConfFileName(String name) {
    return mConfPrefix + "." + name;
  }

  private static String getConfTemplateFileName(String name) {
    return mTemplatePrefix + "." + name + TEMPLATE_SUFFIX;
  }

  private static String getWebHttpModeConf(String mode) {
    return mConfPrefix + ".web.http.mode-" + mode;
  }

  private static String getWebHttpModeConfTemplate(String mode) {
    return mTemplatePrefix + ".web.http.mode-" + mode + TEMPLATE_SUFFIX;
  }

  private static String getWebHttpSModeConf(String mode) {
    return mConfPrefix + ".web.https.mode-" + mode;
  }

  public static String getWebHttpSModeConfTemplate(String mode) {
    return mTemplatePrefix + ".web.https.mode-" + mode + TEMPLATE_SUFFIX;
  }

  public static String getClientCertCaPathByDomain(String domainName) {

    return DOMAIN_SSL_DIR + File.separator + domainName + SSL_CLIENT_CERT_CA_EXT;
  }

  public static String getDefaultClientCertCaPath() {
    return DEFAULT_SSL_CLIENT_CERT_CA;
  }

  public static void expandTemplate(File templateFile, File configFile) throws ProxyConfException {

    String configFilePath = configFile.getAbsolutePath();

    final String templateFileName = templateFile.getName();
    final String overrideTemplateFilePath =
        OVERRIDE_TEMPLATE_DIR + File.separator + templateFileName;
    final boolean usingCustomTemplateOverride =
        !hasCustomTemplateLocationArg && Files.exists(Paths.get(overrideTemplateFilePath));
    final String templateFilePath =
        getTemplateFilePath(templateFile, overrideTemplateFilePath, usingCustomTemplateOverride);

    // return if DryRun
    if (mDryRun) {
      LOG.info("Will expand template: " + templateFilePath + " to file: " + configFilePath);
      return;
    }

    if (!templateFile.exists()) {
      throw new ProxyConfException("Template file " + templateFilePath + " does not exist");
    }

    // process otherwise
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(templateFilePath));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(configFilePath))) {

      LOG.info("Expanding template: " + templateFilePath + " to file: " + configFilePath);

      // for the first line of template, check the custom header command
      bufferedReader.mark(100); // assume the first line won't beyond 100
      String line = bufferedReader.readLine();

      // only for backward compatibility
      if (line.equalsIgnoreCase("!{explode vhn_vip_ssl}")) {
        expandTemplateExplodeSSLConfigsForAllVhnsAndVIPs(bufferedReader, bufferedWriter);
        return;
      }

      addCustomTemplateWarningHeader(usingCustomTemplateOverride, templateFilePath, bufferedWriter);

      Matcher cmdMatcher = CMD_PATTERN.matcher(line);
      if (cmdMatcher.matches()) {
        // the command is found
        String[] cmdArg = cmdMatcher.group(2).split("[ \t]+", 2);
        // command selection can be extracted if more commands are introduced
        if (cmdArg.length == 2 && cmdArg[0].compareTo("explode") == 0) {
          if (cmdArg[1].startsWith("server(") && cmdArg[1].endsWith(")")) {
            String serviceName = cmdArg[1].substring("server(".length(), cmdArg[1].length() - 1);
            if (serviceName.isEmpty()) {
              throw new ProxyConfException(
                  "Missing service parameter in custom header command: " + cmdMatcher.group(2));
            }
            expandTemplateByExplodeServer(bufferedReader, bufferedWriter, serviceName);
          } else {
            // explode only when GenConfPerVhn is enabled
            if (!mGenConfPerVhn) {
              return;
            }

            if (cmdArg[1].startsWith("domain(") && cmdArg[1].endsWith(")")) {
              // extract the args in "domain(arg1, arg2, ...)
              String argList = cmdArg[1].substring("domain(".length(), cmdArg[1].length() - 1);
              String[] args;
              if (argList.equals("")) {
                args = new String[0];
              } else {
                args = argList.split(",([ \t])*");
              }
              expandTemplateByExplodeDomain(bufferedReader, bufferedWriter, args);
            } else {
              throw new ProxyConfException("Illegal custom header command: " + cmdMatcher.group(2));
            }
          }
        } else {
          throw new ProxyConfException("Illegal custom header command: " + cmdMatcher.group(2));
        }
      } else {
        bufferedReader.reset(); // reset to read the first line
        expandTemplateSimple(bufferedReader, bufferedWriter);
      }
    } catch (IOException | SecurityException ie) {
      throw new ProxyConfException("Cannot expand template file: " + ie.getMessage());
    }
  }

  static String getTemplateFilePath(
      File templateFile, String overrideTemplateFilePath, boolean usingCustomTemplateOverride) {
    return usingCustomTemplateOverride ? overrideTemplateFilePath : templateFile.getAbsolutePath();
  }

  /**
   * If using a custom override template from CUSTOM_TEMPLATE_DIR
   * ($workdir/conf/nginx/templates_custom) prefix the resulting conf with warning comment
   *
   * @param usingCustomTemplateOverride either using custom template override
   * @param templateFilePath template file path
   * @param bufferedWriter buffered writer
   * @throws IOException IO exception while write
   */
  private static void addCustomTemplateWarningHeader(
      boolean usingCustomTemplateOverride, String templateFilePath, BufferedWriter bufferedWriter)
      throws IOException {
    if (!usingCustomTemplateOverride) {
      return;
    }
    String sb =
        "# WARNING: This conf is generated using the custom template overload located at "
            + templateFilePath
            + "\n"
            + "# To avoid this and use the default template from "
            + mTemplateDir
            + ", just delete the override template file from "
            + OVERRIDE_TEMPLATE_DIR
            + "\n#\n";
    bufferedWriter.write(sb);
  }

  /**
   * Enumerate all domains, if the required attrs are valid, generate the "server" block according
   * to the template.
   *
   * @author Jiankuan
   * @deprecated use expandTemplateByExplodeDomain instead
   */
  @SuppressWarnings("DeprecatedIsStillUsed")
  @Deprecated
  private static void expandTemplateExplodeSSLConfigsForAllVhnsAndVIPs(
      BufferedReader temp, BufferedWriter conf) throws IOException, ProxyConfException {
    int size = mDomainReverseProxyAttrs.size();
    List<String> cache;

    if (size > 0) {
      Iterator<DomainAttrItem> it = mDomainReverseProxyAttrs.iterator();
      DomainAttrItem item = it.next();
      fillVarsWithDomainAttrs(item);
      cache = expandTemplateAndCache(temp, conf);
      conf.newLine();

      while (it.hasNext()) {
        item = it.next();
        fillVarsWithDomainAttrs(item);
        expandTemplateFromCache(cache, conf);
        conf.newLine();
      }
    }
  }

  /**
   * Enumerate all virtual host names and virtual ip addresses and apply them into the var
   * replacement.<br>
   * explode domain command has this format:<br>
   * <code>!{explode domain(arg1, arg2, ...)}</code><br>
   * The args indicate the required attrs to generate a server block , which now supports:
   *
   * <ul>
   *   <li>vhn: zimbraVirtualHostname must not be empty
   *   <li>sso: zimbraClientCertMode must not be empty or "off"
   * </ul>
   *
   * @author Jiankuan
   */
  private static void expandTemplateByExplodeDomain(
      BufferedReader temp, BufferedWriter conf, String[] requiredAttrs)
      throws IOException, ProxyConfException {
    int size = mDomainReverseProxyAttrs.size();
    List<String> cache = null;

    if (size > 0) {
      Iterator<DomainAttrItem> it = mDomainReverseProxyAttrs.iterator();
      DomainAttrItem item;
      while (cache == null && it.hasNext()) {
        item = it.next();
        if (item instanceof DomainAttrExceptionItem) {
          throw ((DomainAttrExceptionItem) item).exception;
        }

        if (isRequiredAttrsNotValid(item, requiredAttrs)) {
          continue;
        }
        fillVarsWithDomainAttrs(item);
        cache = expandTemplateAndCache(temp, conf);
        conf.newLine();
      }

      while (it.hasNext()) {
        item = it.next();
        if (item instanceof DomainAttrExceptionItem) {
          throw ((DomainAttrExceptionItem) item).exception;
        }

        if (isRequiredAttrsNotValid(item, requiredAttrs)) {
          continue;
        }
        fillVarsWithDomainAttrs(item);
        expandTemplateFromCache(cache, conf);
        conf.newLine();
      }
    }
  }

  /**
   * Iterate all server of type `serviceName` and populate server_id and server_hostname when
   * exploding with !{explode server(docs)} initially created for docs.
   *
   * @param temp Reader of the file which will be exploded per server
   * @param conf Target buffer where generated configuration will be written
   * @param serviceName Filter only servers which contains `serviceName`
   * @author Davide Baldo
   */
  private static void expandTemplateByExplodeServer(
      BufferedReader temp, BufferedWriter conf, String serviceName) throws IOException {

    List<ServerAttrItem> filteredServers = new ArrayList<>();
    for (ServerAttrItem serverAttrItem : mServerAttrs) {
      if (serverAttrItem.hasService(serviceName)) {
        filteredServers.add(serverAttrItem);
      }
    }
    if (!filteredServers.isEmpty()) {
      ArrayList<String> cache = new ArrayList<>(50);
      String line;
      while ((line = temp.readLine()) != null) {
        if (!line.startsWith("#")) cache.add(line); // cache only non-comment lines
      }

      for (ServerAttrItem server : filteredServers) {
        mVars.put("server_id", server.zimbraId);
        mVars.put("server_hostname", server.hostname);
        expandTemplateFromCache(cache, conf);
        mVars.remove("server_id");
        mVars.remove("server_hostname");
      }
    }
  }

  private static boolean isRequiredAttrsNotValid(DomainAttrItem item, String[] requiredAttrs) {
    for (String attr : requiredAttrs) {
      if (attr.equals("vhn")) {
        // check virtual hostname
        if (item.virtualHostname == null || item.virtualHostname.equals("")) {
          return true;
        }
      } else if (attr.equals("sso")
          && (item.clientCertMode == null
              || item.clientCertMode.equals("")
              || item.clientCertMode.equals("off"))) {
        return true;
      }
    }
    return false;
  }

  private static void fillVarsWithDomainAttrs(DomainAttrItem item) throws ProxyConfException {

    String defaultVal;
    mVars.put("vhn", item.virtualHostname);
    int i;

    // resolve the virtual host name
    InetAddress vip = null;
    try {
      if (item.virtualIPAddress == null) {
        vip = InetAddress.getByName(item.virtualHostname);
      } else {
        vip = InetAddress.getByName(item.virtualIPAddress);
      }
    } catch (UnknownHostException e) {
      if (mEnforceDNSResolution) {
        throw new ProxyConfException(
            "virtual host name \"" + item.virtualHostname + "\" is not resolvable", e);
      } else {
        LOG.warn("virtual host name \"" + item.virtualHostname + "\" is not resolvable");
      }
    }

    if (IPModeEnablerVar.getZimbraIPMode() != IPModeEnablerVar.IPMode.BOTH) {
      if (IPModeEnablerVar.getZimbraIPMode() == IPModeEnablerVar.IPMode.IPV4_ONLY
          && vip instanceof Inet6Address) {
        String msg = vip.getHostAddress() + " is an IPv6 address but zimbraIPMode is 'ipv4'";
        LOG.error(msg);
        throw new ProxyConfException(msg);
      }

      if (IPModeEnablerVar.getZimbraIPMode() == IPModeEnablerVar.IPMode.IPV6_ONLY
          && vip instanceof Inet4Address) {
        String msg = vip.getHostAddress() + " is an IPv4 address but zimbraIPMode is 'ipv6'";
        LOG.error(msg);
        throw new ProxyConfException(msg);
      }
    }

    boolean sni =
        ProxyConfVar.serverSource.getBooleanAttr(
            ZAttrProvisioning.A_zimbraReverseProxySNIEnabled, true);
    if (vip instanceof Inet6Address) {
      // ipv6 address has to be enclosed with [ ]
      if (sni) {
        mVars.put("vip", "[::]:");
      } else {
        mVars.put("vip", "[" + vip.getHostAddress() + "]:");
      }
    } else {
      if (sni || vip == null) {
        mVars.put("vip", "");
      } else {
        mVars.put("vip", vip.getHostAddress() + ":");
      }
    }

    // Get the response headers list for this domain
    ArrayList<String> responseHeadersList = new ArrayList<>();
    for (i = 0; i < item.rspHeaders.length; i++) {
      responseHeadersList.add(item.rspHeaders[i]);
    }
    mDomainConfVars.put(
        "web.add.headers.vhost",
        new AddHeadersVar(
            "web.add.headers.vhost",
            responseHeadersList,
            "add_header directive for vhost web proxy"));

    LOG.debug("Updating Default Domain Variable Map");
    try {
      updateDefaultDomainVars();
    } catch (ProxyConfException | ServiceException pe) {
      handleException(pe);
    }

    if (item.sslCertificate != null) {
      mVars.put("ssl.crt", DOMAIN_SSL_DIR + File.separator + item.domainName + SSL_CRT_EXT);
    } else {
      defaultVal = mVars.get("ssl.crt.default");
      mVars.put("ssl.crt", defaultVal);
    }

    if (item.sslPrivateKey != null) {
      mVars.put("ssl.key", DOMAIN_SSL_DIR + File.separator + item.domainName + SSL_KEY_EXT);
    } else {
      defaultVal = mVars.get("ssl.key.default");
      mVars.put("ssl.key", defaultVal);
    }

    if (item.clientCertMode != null) {
      mVars.put("ssl.clientcertmode", item.clientCertMode);
      if (item.clientCertMode.equals("on") || item.clientCertMode.equals("optional")) {
        mVars.put("web.sso.certauth.enabled", "");
      } else {
        mVars.put("web.sso.certauth.enabled", "#");
      }
    } else {
      defaultVal = mVars.get("ssl.clientcertmode.default");
      mVars.put("ssl.clientcertmode", defaultVal);
    }

    if (item.clientCertCa != null) {
      String clientCertCaPath = getClientCertCaPathByDomain(item.domainName);
      mVars.put("ssl.clientcertca", clientCertCaPath);
      // DnVhnVIPItem.clientCertCa stores the CA cert's content, other than path
      // if it is not null or "", loadReverseProxyVhnAndVIP() will save its content .
      // into clientCertCaPath before coming here
    } else {
      defaultVal = mVars.get("ssl.clientcertca.default");
      mVars.put("ssl.clientcertca", defaultVal);
    }
  }

  /**
   * Read from template file and translate the contents to conf. The template will be cached and
   * returned
   */
  private static List<String> expandTemplateAndCache(BufferedReader temp, BufferedWriter conf)
      throws IOException {
    String line;
    ArrayList<String> cache = new ArrayList<>(50);
    while ((line = temp.readLine()) != null) {
      if (!line.startsWith("#")) cache.add(line); // cache only non-comment lines
      line = StringUtil.fillTemplate(line, mVars);
      conf.write(line);
      conf.newLine();
    }
    return cache;
  }

  /** Read from template file and translate the contents to conf */
  private static void expandTemplateSimple(BufferedReader temp, BufferedWriter conf)
      throws IOException {
    String line;
    while ((line = temp.readLine()) != null) {
      line = StringUtil.fillTemplate(line, mVars);
      conf.write(line);
      conf.newLine();
    }
  }

  /** Read from cache that holding template file's content and translate to conf */
  private static void expandTemplateFromCache(List<String> cache, BufferedWriter conf)
      throws IOException {
    for (String line : cache) {
      line = StringUtil.fillTemplate(line, mVars);
      conf.write(line);
      conf.newLine();
    }
  }

  /* Print the default variable map */
  public static void displayDefaultVariables() throws ProxyConfException {
    for (ProxyConfVar proxyConfVar : mConfVars.values()) {
      if (proxyConfVar instanceof TimeInSecVarWrapper) {
        proxyConfVar = ((TimeInSecVarWrapper) proxyConfVar).mVar;
      }
      proxyConfVar.write(System.out);
    }
  }

  /* Print the variable map */
  public static void displayVariables() throws ProxyConfException {
    SortedSet<String> sk = new TreeSet<>(mVars.keySet());
    for (String k : sk) {
      ProxyConfVar proxyConfVar = mConfVars.get(k);
      if (proxyConfVar instanceof TimeInSecVarWrapper)
        proxyConfVar = ((TimeInSecVarWrapper) proxyConfVar).mVar;
      proxyConfVar.write(System.out);
    }
  }

  public static void buildDefaultVars() {
    mConfVars.put(
        "core.workdir",
        new ProxyConfVar(
            "core.workdir",
            null,
            mWorkingDir,
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "Working Directory for NGINX worker processes"));
    mConfVars.put(
        "core.includes",
        new ProxyConfVar(
            "core.includes",
            null,
            mConfIncludesDir,
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "Include directory (relative to ${core.workdir}/conf)"));
    mConfVars.put(
        "core.cprefix",
        new ProxyConfVar(
            "core.cprefix",
            null,
            mConfPrefix,
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "Common config file prefix"));
    mConfVars.put(
        "core.tprefix",
        new ProxyConfVar(
            "core.tprefix",
            null,
            mTemplatePrefix,
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "Common template file prefix"));
    mConfVars.put("core.ipv4only.enabled", new IPv4OnlyEnablerVar());
    mConfVars.put("core.ipv6only.enabled", new IPv6OnlyEnablerVar());
    mConfVars.put("core.ipboth.enabled", new IPBothEnablerVar());
    mConfVars.put(
        "ssl.crt.default",
        new ProxyConfVar(
            "ssl.crt.default",
            null,
            DEFAULT_SSL_CRT,
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "default nginx certificate file path"));
    mConfVars.put(
        "ssl.key.default",
        new ProxyConfVar(
            "ssl.key.default",
            null,
            DEFAULT_SSL_KEY,
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "default nginx private key file path"));
    mConfVars.put(
        "ssl.clientcertmode.default",
        new ProxyConfVar(
            "ssl.clientcertmode.default",
            ZAttrProvisioning.A_zimbraReverseProxyClientCertMode,
            "off",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "enable authentication via X.509 Client Certificate in nginx proxy (https only)"));
    mConfVars.put("ssl.clientcertca.default", new ClientCertAuthDefaultCAVar());
    mConfVars.put(
        "ssl.clientcertdepth.default",
        new ProxyConfVar(
            "ssl.clientcertdepth.default",
            "zimbraReverseProxyClientCertDepth",
            10,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.NONE,
            "indicate how depth the verification will load the ca chain. This is useful when client"
                + " crt is signed by multiple intermediate ca"));
    mConfVars.put(
        "main.user",
        new ProxyConfVar(
            "main.user",
            null,
            ZIMBRA_USER,
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "The user as which the worker processes will run"));
    mConfVars.put(
        "main.group",
        new ProxyConfVar(
            "main.group",
            null,
            ZIMBRA_USER,
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "The group as which the worker processes will run"));
    mConfVars.put(
        "main.workers",
        new ProxyConfVar(
            "main.workers",
            ZAttrProvisioning.A_zimbraReverseProxyWorkerProcesses,
            4,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Number of worker processes"));
    mConfVars.put(
        "main.pidfile",
        new ProxyConfVar(
            "main.pidfile",
            null,
            mWorkingDir + "/log/nginx.pid",
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "PID file path (relative to ${core.workdir})"));
    mConfVars.put(
        "main.logfile",
        new ProxyConfVar(
            "main.logfile",
            null,
            mWorkingDir + "/log/nginx.log",
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "Log file path (relative to ${core.workdir})"));
    mConfVars.put(
        "main.loglevel",
        new ProxyConfVar(
            "main.loglevel",
            ZAttrProvisioning.A_zimbraReverseProxyLogLevel,
            "info",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "Log level - can be debug|info|notice|warn|error|crit"));
    mConfVars.put(
        "main.connections",
        new ProxyConfVar(
            "main.connections",
            ZAttrProvisioning.A_zimbraReverseProxyWorkerConnections,
            10240,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Maximum number of simultaneous connections per worker process"));
    mConfVars.put(
        "main.krb5keytab",
        new ProxyConfVar(
            "main.krb5keytab",
            "krb5_keytab",
            "/opt/zextras/conf/krb5.keytab",
            ProxyConfValueType.STRING,
            ProxyConfOverride.LOCALCONFIG,
            "Path to kerberos keytab file used for GSSAPI authentication"));
    mConfVars.put("memcache.:servers", new MemcacheServersVar());
    mConfVars.put(
        "memcache.timeout",
        new ProxyConfVar(
            "memcache.timeout",
            ZAttrProvisioning.A_zimbraReverseProxyCacheFetchTimeout,
            3000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.CONFIG,
            "Time (ms) given to a cache-fetch operation to complete"));
    mConfVars.put(
        "memcache.reconnect",
        new ProxyConfVar(
            "memcache.reconnect",
            ZAttrProvisioning.A_zimbraReverseProxyCacheReconnectInterval,
            60000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.CONFIG,
            "Time (ms) after which NGINX will attempt to re-establish a broken connection to a"
                + " memcache server"));
    mConfVars.put(
        "memcache.ttl",
        new ProxyConfVar(
            "memcache.ttl",
            ZAttrProvisioning.A_zimbraReverseProxyCacheEntryTTL,
            3600000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.CONFIG,
            "Time interval (ms) for which cached entries remain in memcache"));
    mConfVars.put(
        "mail.ctimeout",
        new ProxyConfVar(
            "mail.ctimeout",
            ZAttrProvisioning.A_zimbraReverseProxyConnectTimeout,
            120000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.SERVER,
            "Time interval (ms) after which a POP/IMAP proxy connection to a remote host will give"
                + " up"));
    mConfVars.put(
        "mail.pop3.timeout",
        new ProxyConfVar(
            "mail.pop3.timeout",
            "pop3_max_idle_time",
            60,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.LOCALCONFIG,
            "pop3 network timeout before authentication"));
    mConfVars.put(
        "mail.pop3.proxytimeout",
        new ProxyConfVar(
            "mail.pop3.proxytimeout",
            "pop3_max_idle_time",
            60,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.LOCALCONFIG,
            "pop3 network timeout after authentication"));
    mConfVars.put(
        "mail.imap.timeout",
        new ProxyConfVar(
            "mail.imap.timeout",
            "imap_max_idle_time",
            60,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.LOCALCONFIG,
            "imap network timeout before authentication"));
    mConfVars.put(
        "mail.imap.proxytimeout",
        new TimeoutVar(
            "mail.imap.proxytimeout",
            "imap_authenticated_max_idle_time",
            1800,
            ProxyConfOverride.LOCALCONFIG,
            300,
            "imap network timeout after authentication"));
    mConfVars.put(
        "mail.passerrors",
        new ProxyConfVar(
            "mail.passerrors",
            ZAttrProvisioning.A_zimbraReverseProxyPassErrors,
            true,
            ProxyConfValueType.BOOLEAN,
            ProxyConfOverride.SERVER,
            "Indicates whether mail proxy will pass any protocol specific errors from the upstream"
                + " server back to the downstream client"));
    mConfVars.put(
        "mail.auth_http_timeout",
        new ProxyConfVar(
            "mail.auth_http_timeout",
            ZAttrProvisioning.A_zimbraReverseProxyRouteLookupTimeout,
            15000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.SERVER,
            "Time interval (ms) given to mail route lookup handler to respond to route lookup"
                + " request (after this time elapses, Proxy fails over to next handler, or fails"
                + " the request if there are no more lookup handlers)"));
    mConfVars.put(
        "mail.authwait",
        new ProxyConfVar(
            "mail.authwait",
            ZAttrProvisioning.A_zimbraReverseProxyAuthWaitInterval,
            10000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.CONFIG,
            "Time delay (ms) after which an incorrect POP/IMAP login attempt will be rejected"));
    mConfVars.put("mail.pop3capa", new Pop3CapaVar());
    mConfVars.put("mail.imapcapa", new ImapCapaVar());
    mConfVars.put(
        "mail.imapid",
        new ProxyConfVar(
            "mail.imapid",
            null,
            "\"NAME\" \"Zimbra\" \"VERSION\" \""
                + BuildInfo.VERSION
                + "\" \"RELEASE\" \""
                + BuildInfo.RELEASE
                + "\"",
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "NGINX response to IMAP ID command"));
    mConfVars.put(
        "mail.defaultrealm",
        new ProxyConfVar(
            "mail.defaultrealm",
            ZAttrProvisioning.A_zimbraReverseProxyDefaultRealm,
            "",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "Default SASL realm used in case Kerberos principal does not contain realm"
                + " information"));
    mConfVars.put("mail.sasl_host_from_ip", new SaslHostFromIPVar());
    mConfVars.put(
        "mail.saslapp",
        new ProxyConfVar(
            "mail.saslapp",
            null,
            "nginx",
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Application name used by NGINX to initialize SASL authentication"));
    mConfVars.put(
        "mail.ipmax",
        new ProxyConfVar(
            "mail.ipmax",
            ZAttrProvisioning.A_zimbraReverseProxyIPLoginLimit,
            0,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.CONFIG,
            "IP Login Limit (Throttle) - 0 means infinity"));
    mConfVars.put(
        "mail.ipttl",
        new ProxyConfVar(
            "mail.ipttl",
            ZAttrProvisioning.A_zimbraReverseProxyIPLoginLimitTime,
            3600000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.CONFIG,
            "Time interval (ms) after which IP Login Counter is reset"));
    mConfVars.put(
        "mail.imapmax",
        new ProxyConfVar(
            "mail.imapmax",
            ZAttrProvisioning.A_zimbraReverseProxyIPLoginImapLimit,
            0,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.CONFIG,
            "IMAP Login Limit (Throttle) - 0 means infinity"));
    mConfVars.put(
        "mail.imapttl",
        new ProxyConfVar(
            "mail.imapttl",
            ZAttrProvisioning.A_zimbraReverseProxyIPLoginImapLimitTime,
            3600000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.CONFIG,
            "Time interval (ms) after which IMAP Login Counter is reset"));
    mConfVars.put(
        "mail.pop3max",
        new ProxyConfVar(
            "mail.pop3max",
            ZAttrProvisioning.A_zimbraReverseProxyIPLoginPop3Limit,
            0,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.CONFIG,
            "POP3 Login Limit (Throttle) - 0 means infinity"));
    mConfVars.put(
        "mail.pop3ttl",
        new ProxyConfVar(
            "mail.pop3ttl",
            ZAttrProvisioning.A_zimbraReverseProxyIPLoginPop3LimitTime,
            3600000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.CONFIG,
            "Time interval (ms) after which POP3 Login Counter is reset"));
    mConfVars.put(
        "mail.iprej",
        new ProxyConfVar(
            "mail.iprej",
            ZAttrProvisioning.A_zimbraReverseProxyIpThrottleMsg,
            "Login rejected from this IP",
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Rejection message for IP throttle"));
    mConfVars.put(
        "mail.usermax",
        new ProxyConfVar(
            "mail.usermax",
            ZAttrProvisioning.A_zimbraReverseProxyUserLoginLimit,
            0,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.CONFIG,
            "User Login Limit (Throttle) - 0 means infinity"));
    mConfVars.put(
        "mail.userttl",
        new ProxyConfVar(
            "mail.userttl",
            ZAttrProvisioning.A_zimbraReverseProxyUserLoginLimitTime,
            3600000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.CONFIG,
            "Time interval (ms) after which User Login Counter is reset"));
    mConfVars.put(
        "mail.userrej",
        new ProxyConfVar(
            "mail.userrej",
            ZAttrProvisioning.A_zimbraReverseProxyUserThrottleMsg,
            "Login rejected for this user",
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Rejection message for User throttle"));
    mConfVars.put(
        "mail.upstream.pop3xoip",
        new ProxyConfVar(
            "mail.upstream.pop3xoip",
            ZAttrProvisioning.A_zimbraReverseProxySendPop3Xoip,
            true,
            ProxyConfValueType.BOOLEAN,
            ProxyConfOverride.CONFIG,
            "Whether NGINX issues the POP3 XOIP command to the upstream server prior to logging in"
                + " (audit purpose)"));
    mConfVars.put(
        "mail.upstream.imapid",
        new ProxyConfVar(
            "mail.upstream.imapid",
            ZAttrProvisioning.A_zimbraReverseProxySendImapId,
            true,
            ProxyConfValueType.BOOLEAN,
            ProxyConfOverride.CONFIG,
            "Whether NGINX issues the IMAP ID command to the upstream server prior to logging in"
                + " (audit purpose)"));
    mConfVars.put("mail.ssl.protocols", new MailSSLProtocolsVar());
    mConfVars.put(
        "mail.ssl.preferserverciphers",
        new ProxyConfVar(
            "mail.ssl.preferserverciphers",
            null,
            true,
            ProxyConfValueType.BOOLEAN,
            ProxyConfOverride.CONFIG,
            "Requires TLS protocol server ciphers be preferred over the client's ciphers"));
    mConfVars.put(
        "mail.ssl.ciphers",
        new ProxyConfVar(
            "mail.ssl.ciphers",
            ZAttrProvisioning.A_zimbraReverseProxySSLCiphers,
            "EECDH+AESGCM:EDH+AESGCM",
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Permitted ciphers for mail proxy"));
    mConfVars.put(
        "mail.ssl.ecdh.curve",
        new ProxyConfVar(
            "mail.ssl.ecdh.curve",
            ZAttrProvisioning.A_zimbraReverseProxySSLECDHCurve,
            "auto",
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "SSL ECDH cipher curve for mail proxy"));
    mConfVars.put(
        "mail.imap.authplain.enabled",
        new ProxyConfVar(
            "mail.imap.authplain.enabled",
            ZAttrProvisioning.A_zimbraReverseProxyImapSaslPlainEnabled,
            true,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Whether SASL PLAIN is enabled for IMAP"));
    mConfVars.put(
        "mail.imap.authgssapi.enabled",
        new ProxyConfVar(
            "mail.imap.authgssapi.enabled",
            ZAttrProvisioning.A_zimbraReverseProxyImapSaslGssapiEnabled,
            false,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Whether SASL GSSAPI is enabled for IMAP"));
    mConfVars.put(
        "mail.pop3.authplain.enabled",
        new ProxyConfVar(
            "mail.pop3.authplain.enabled",
            ZAttrProvisioning.A_zimbraReverseProxyPop3SaslPlainEnabled,
            true,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Whether SASL PLAIN is enabled for POP3"));
    mConfVars.put(
        "mail.pop3.authgssapi.enabled",
        new ProxyConfVar(
            "mail.pop3.authgssapi.enabled",
            ZAttrProvisioning.A_zimbraReverseProxyPop3SaslGssapiEnabled,
            false,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Whether SASL GSSAPI is enabled for POP3"));
    mConfVars.put(
        "mail.imap.literalauth",
        new ProxyConfVar(
            "mail.imap.literalauth",
            null,
            true,
            ProxyConfValueType.BOOLEAN,
            ProxyConfOverride.CONFIG,
            "Whether NGINX uses literal strings for user name/password when logging in to upstream"
                + " IMAP server - if false, NGINX uses quoted strings"));
    mConfVars.put(
        "mail.imap.port",
        new ProxyConfVar(
            "mail.imap.port",
            ZAttrProvisioning.A_zimbraImapProxyBindPort,
            143,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Mail Proxy IMAP Port"));
    mConfVars.put(
        "mail.imap.tls",
        new ProxyConfVar(
            "mail.imap.tls",
            ZAttrProvisioning.A_zimbraReverseProxyImapStartTlsMode,
            "only",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "TLS support for IMAP - can be on|off|only - on indicates TLS support present, off"
                + " indicates TLS support absent, only indicates TLS is enforced on unsecure"
                + " channel"));
    mConfVars.put(
        "mail.imaps.port",
        new ProxyConfVar(
            "mail.imaps.port",
            ZAttrProvisioning.A_zimbraImapSSLProxyBindPort,
            993,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Mail Proxy IMAPS Port"));
    mConfVars.put(
        "mail.pop3.port",
        new ProxyConfVar(
            "mail.pop3.port",
            ZAttrProvisioning.A_zimbraPop3ProxyBindPort,
            110,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Mail Proxy POP3 Port"));
    mConfVars.put(
        "mail.pop3.tls",
        new ProxyConfVar(
            "mail.pop3.tls",
            ZAttrProvisioning.A_zimbraReverseProxyPop3StartTlsMode,
            "only",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "TLS support for POP3 - can be on|off|only - on indicates TLS support present, off"
                + " indicates TLS support absent, only indicates TLS is enforced on unsecure"
                + " channel"));
    mConfVars.put(
        "mail.pop3s.port",
        new ProxyConfVar(
            "mail.pop3s.port",
            ZAttrProvisioning.A_zimbraPop3SSLProxyBindPort,
            995,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Mail Proxy POP3S Port"));
    mConfVars.put("mail.imap.greeting", new ImapGreetingVar());
    mConfVars.put("mail.pop3.greeting", new Pop3GreetingVar());
    final String keyword = "mail.enabled";
    mConfVars.put(
        keyword,
        new ProxyConfVar(
            keyword,
            ZAttrProvisioning.A_zimbraReverseProxyMailEnabled,
            true,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Indicates whether Mail Proxy is enabled"));
    mConfVars.put(
        "mail.imap.enabled",
        new ProxyConfVar(
            "mail.imap.enabled",
            ZAttrProvisioning.A_zimbraReverseProxyMailImapEnabled,
            true,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Indicates whether IMAP Mail Proxy is enabled"));
    mConfVars.put(
        "mail.imaps.enabled",
        new ProxyConfVar(
            "mail.imaps.enabled",
            ZAttrProvisioning.A_zimbraReverseProxyMailImapsEnabled,
            true,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Indicates whether IMAP Mail Proxy is enabled"));
    mConfVars.put(
        "mail.pop3.enabled",
        new ProxyConfVar(
            "mail.pop3.enabled",
            ZAttrProvisioning.A_zimbraReverseProxyMailPop3Enabled,
            true,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Indicates whether POP Mail Proxy is enabled"));
    mConfVars.put(
        "mail.pop3s.enabled",
        new ProxyConfVar(
            "mail.pop3s.enabled",
            ZAttrProvisioning.A_zimbraReverseProxyMailPop3sEnabled,
            true,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Indicates whether Pops Mail Proxy is enabled"));
    mConfVars.put(
        "mail.proxy.ssl",
        new ProxyConfVar(
            "mail.proxy.ssl",
            ZAttrProvisioning.A_zimbraReverseProxySSLToUpstreamEnabled,
            true,
            ProxyConfValueType.BOOLEAN,
            ProxyConfOverride.SERVER,
            "Indicates whether using SSL to connect to upstream mail server"));
    mConfVars.put("mail.whitelistip.:servers", new ReverseProxyIPThrottleWhitelist());
    mConfVars.put(
        "mail.whitelist.ttl",
        new TimeInSecVarWrapper(
            new ProxyConfVar(
                "mail.whitelist.ttl",
                ZAttrProvisioning.A_zimbraReverseProxyIPThrottleWhitelistTime,
                300000L,
                ProxyConfValueType.TIME,
                ProxyConfOverride.CONFIG,
                "Time-to-live, in seconds, of the list of servers for which IP throttling is"
                    + " disabled")));
    mConfVars.put(
        "web.logfile",
        new ProxyConfVar(
            "web.logfile",
            null,
            mWorkingDir + "/log/nginx.access.log",
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "Access log file path (relative to ${core.workdir})"));
    mConfVars.put(
        "web.mailmode",
        new ProxyConfVar(
            "web.mailmode",
            ZAttrProvisioning.A_zimbraReverseProxyMailMode,
            "both",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "Reverse Proxy Mail Mode - can be http|https|both|redirect|mixed"));
    mConfVars.put(
        "web.server_name.default",
        new ProxyConfVar(
            "web.server_name.default",
            "zimbra_server_hostname",
            "localhost",
            ProxyConfValueType.STRING,
            ProxyConfOverride.LOCALCONFIG,
            "The server name for default server config"));
    mConfVars.put(
        "web.upstream.name",
        new ProxyConfVar(
            "web.upstream.name",
            null,
            ZIMBRA_UPSTREAM_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for HTTP upstream cluster"));
    mConfVars.put(
        "web.upstream.webclient.name",
        new ProxyConfVar(
            "web.upstream.webclient.name",
            null,
            ZIMBRA_UPSTREAM_WEBCLIENT_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for HTTP upstream webclient cluster"));
    mConfVars.put(
        "web.ssl.upstream.name",
        new ProxyConfVar(
            "web.ssl.upstream.name",
            null,
            ZIMBRA_SSL_UPSTREAM_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for HTTPS upstream cluster"));
    mConfVars.put(
        "web.ssl.upstream.webclient.name",
        new ProxyConfVar(
            "web.ssl.upstream.webclient.name",
            null,
            ZIMBRA_SSL_UPSTREAM_WEBCLIENT_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for HTTPS upstream webclient cluster"));
    mConfVars.put("web.upstream.:servers", new WebUpstreamServersVar());
    mConfVars.put("web.upstream.webclient.:servers", new WebUpstreamClientServersVar());
    mConfVars.put(
        "web.server_names.max_size",
        new ProxyConfVar(
            "web.server_names.max_size",
            "proxy_server_names_hash_max_size",
            DEFAULT_SERVERS_NAME_HASH_MAX_SIZE,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.LOCALCONFIG,
            "the server names hash max size, needed to be increased if too many virtual host names"
                + " are added"));
    mConfVars.put(
        "web.server_names.bucket_size",
        new ProxyConfVar(
            "web.server_names.bucket_size",
            "proxy_server_names_hash_bucket_size",
            DEFAULT_SERVERS_NAME_HASH_BUCKET_SIZE,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.LOCALCONFIG,
            "the server names hash bucket size, needed to be increased if too many virtual host"
                + " names are added"));
    mConfVars.put("web.ssl.upstream.:servers", new WebSSLUpstreamServersVar());
    mConfVars.put("web.ssl.upstream.webclient.:servers", new WebSSLUpstreamClientServersVar());
    mConfVars.put(
        "web.uploadmax",
        new ProxyConfVar(
            "web.uploadmax",
            ZAttrProvisioning.A_zimbraFileUploadMaxSize,
            10485760L,
            ProxyConfValueType.LONG,
            ProxyConfOverride.SERVER,
            "Maximum accepted client request body size (indicated by Content-Length) - if content"
                + " length exceeds this limit, then request fails with HTTP 413"));
    mConfVars.put("web.:error_pages", new ErrorPagesVar());
    mConfVars.put(
        "web.http.port",
        new ProxyConfVar(
            "web.http.port",
            ZAttrProvisioning.A_zimbraMailProxyPort,
            0,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Web Proxy HTTP Port"));
    mConfVars.put(
        "web.http.maxbody",
        new ProxyConfVar(
            "web.http.maxbody",
            ZAttrProvisioning.A_zimbraFileUploadMaxSize,
            10485760L,
            ProxyConfValueType.LONG,
            ProxyConfOverride.SERVER,
            "Maximum accepted client request body size (indicated by Content-Length) - if content"
                + " length exceeds this limit, then request fails with HTTP 413"));
    mConfVars.put(
        "web.https.port",
        new ProxyConfVar(
            "web.https.port",
            ZAttrProvisioning.A_zimbraMailSSLProxyPort,
            0,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Web Proxy HTTPS Port"));
    mConfVars.put(
        "web.https.maxbody",
        new ProxyConfVar(
            "web.https.maxbody",
            ZAttrProvisioning.A_zimbraFileUploadMaxSize,
            10485760L,
            ProxyConfValueType.LONG,
            ProxyConfOverride.SERVER,
            "Maximum accepted client request body size (indicated by Content-Length) - if content"
                + " length exceeds this limit, then request fails with HTTP 413"));
    mConfVars.put("web.ssl.protocols", new WebSSLProtocolsVar());
    mConfVars.put(
        "web.ssl.preferserverciphers",
        new ProxyConfVar(
            "web.ssl.preferserverciphers",
            null,
            true,
            ProxyConfValueType.BOOLEAN,
            ProxyConfOverride.CONFIG,
            "Requires TLS protocol server ciphers be preferred over the client's ciphers"));
    mConfVars.put(
        "web.ssl.ciphers",
        new ProxyConfVar(
            "web.ssl.ciphers",
            ZAttrProvisioning.A_zimbraReverseProxySSLCiphers,
            "EECDH+AESGCM:EDH+AESGCM",
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Permitted ciphers for web proxy"));
    mConfVars.put(
        "web.ssl.ecdh.curve",
        new ProxyConfVar(
            "web.ssl.ecdh.curve",
            ZAttrProvisioning.A_zimbraReverseProxySSLECDHCurve,
            "auto",
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "SSL ECDH cipher curve for web proxy"));
    mConfVars.put(
        "web.http.uport",
        new ProxyConfVar(
            "web.http.uport",
            ZAttrProvisioning.A_zimbraMailPort,
            80,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Web upstream server port"));
    mConfVars.put(
        "web.upstream.connect.timeout",
        new ProxyConfVar(
            "web.upstream.connect.timeout",
            ZAttrProvisioning.A_zimbraReverseProxyUpstreamConnectTimeout,
            25,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "upstream connect timeout"));
    mConfVars.put(
        "web.upstream.read.timeout",
        new TimeInSecVarWrapper(
            new ProxyConfVar(
                "web.upstream.read.timeout",
                ZAttrProvisioning.A_zimbraReverseProxyUpstreamReadTimeout,
                60L,
                ProxyConfValueType.TIME,
                ProxyConfOverride.SERVER,
                "upstream read timeout")));
    mConfVars.put(
        "web.upstream.send.timeout",
        new TimeInSecVarWrapper(
            new ProxyConfVar(
                "web.upstream.send.timeout",
                ZAttrProvisioning.A_zimbraReverseProxyUpstreamSendTimeout,
                60L,
                ProxyConfValueType.TIME,
                ProxyConfOverride.SERVER,
                "upstream send timeout")));
    mConfVars.put(
        "web.upstream.polling.timeout",
        new TimeInSecVarWrapper(
            new ProxyConfVar(
                "web.upstream.polling.timeout",
                ZAttrProvisioning.A_zimbraReverseProxyUpstreamPollingTimeout,
                3600L,
                ProxyConfValueType.TIME,
                ProxyConfOverride.SERVER,
                "the response timeout for Microsoft Active Sync polling")));
    mConfVars.put(
        "web.enabled",
        new ProxyConfVar(
            "web.enabled",
            ZAttrProvisioning.A_zimbraReverseProxyHttpEnabled,
            false,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Indicates whether HTTP proxying is enabled"));
    mConfVars.put(
        "web.upstream.exactversioncheck",
        new ProxyConfVar(
            "web.upstream.exactversioncheck",
            ZAttrProvisioning.A_zimbraReverseProxyExactServerVersionCheck,
            "on",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "Indicates whether nginx will match exact server version against the version received"
                + " in the client request"));
    mConfVars.put("web.http.enabled", new HttpEnablerVar());
    mConfVars.put("web.https.enabled", new HttpsEnablerVar());
    mConfVars.put("web.upstream.target", new WebProxyUpstreamTargetVar());
    mConfVars.put("web.upstream.webclient.target", new WebProxyUpstreamClientTargetVar());
    mConfVars.put("lookup.available", new ZMLookupAvailableVar());
    mConfVars.put("web.available", new ZMWebAvailableVar());
    mConfVars.put("zmlookup.:handlers", new ZMLookupHandlerVar());
    mConfVars.put(
        "zmlookup.timeout",
        new ProxyConfVar(
            "zmlookup.timeout",
            ZAttrProvisioning.A_zimbraReverseProxyRouteLookupTimeout,
            15000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.SERVER,
            "Time interval (ms) given to lookup handler to respond to route lookup request (after"
                + " this time elapses, Proxy fails over to next handler, or fails the request if"
                + " there are no more lookup handlers)"));
    mConfVars.put(
        "zmlookup.retryinterval",
        new ProxyConfVar(
            "zmlookup.retryinterval",
            ZAttrProvisioning.A_zimbraReverseProxyRouteLookupTimeoutCache,
            60000L,
            ProxyConfValueType.TIME,
            ProxyConfOverride.SERVER,
            "Time interval (ms) given to lookup handler to cache a failed response to route a"
                + " previous lookup request (after this time elapses, Proxy retries this host)"));
    mConfVars.put(
        "zmlookup.dpasswd",
        new ProxyConfVar(
            "zmlookup.dpasswd",
            "ldap_nginx_password",
            "zmnginx",
            ProxyConfValueType.STRING,
            ProxyConfOverride.LOCALCONFIG,
            "Password for master credentials used by NGINX to log in to upstream for GSSAPI"
                + " authentication"));
    mConfVars.put(
        "zmlookup.caching",
        new ProxyConfVar(
            "zmlookup.caching",
            ZAttrProvisioning.A_zimbraReverseProxyZmlookupCachingEnabled,
            true,
            ProxyConfValueType.BOOLEAN,
            ProxyConfOverride.SERVER,
            "Whether to turn on nginx lookup caching"));
    mConfVars.put(
        "zmprefix.url",
        new ProxyConfVar(
            "zmprefix.url",
            ZAttrProvisioning.A_zimbraMailURL,
            "/",
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "http URL prefix for where the zimbra app resides on upstream server"));
    mConfVars.put(
        "web.sso.certauth.port",
        new ProxyConfVar(
            "web.sso.certauth.port",
            ZAttrProvisioning.A_zimbraMailSSLProxyClientCertPort,
            0,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "reverse proxy client cert auth port"));
    mConfVars.put("web.sso.certauth.default.enabled", new ZMSSOCertAuthDefaultEnablerVar());
    mConfVars.put("web.sso.enabled", new ZMSSOEnablerVar());
    mConfVars.put("web.sso.default.enabled", new ZMSSODefaultEnablerVar());
    mConfVars.put(
        "web.admin.default.enabled",
        new ProxyConfVar(
            "web.admin.default.enabled",
            ZAttrProvisioning.A_zimbraReverseProxyAdminEnabled,
            Boolean.FALSE,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Indicate whether admin console proxy is enabled"));
    mConfVars.put(
        "web.admin.port",
        new ProxyConfVar(
            "web.admin.port",
            ZAttrProvisioning.A_zimbraAdminProxyPort,
            9071,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Admin console proxy port"));
    mConfVars.put(
        "web.admin.uport",
        new ProxyConfVar(
            "web.admin.uport",
            ZAttrProvisioning.A_zimbraAdminPort,
            7071,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Admin console upstream port"));
    mConfVars.put(
        "web.admin.upstream.name",
        new ProxyConfVar(
            "web.admin.upstream.name",
            null,
            ZIMBRA_ADMIN_CONSOLE_UPSTREAM_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for admin console upstream cluster"));
    mConfVars.put(
        "web.admin.upstream.adminclient.name",
        new ProxyConfVar(
            "web.admin.upstream.adminclient.name",
            null,
            ZIMBRA_ADMIN_CONSOLE_CLIENT_UPSTREAM_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for admin client console upstream cluster"));
    mConfVars.put("web.admin.upstream.:servers", new WebAdminUpstreamServersVar());
    mConfVars.put(
        "web.admin.upstream.adminclient.:servers", new WebAdminUpstreamAdminClientServersVar());
    mConfVars.put(
        "web.upstream.noop.timeout",
        new TimeoutVar(
            "web.upstream.noop.timeout",
            "zimbra_noop_max_timeout",
            1200,
            ProxyConfOverride.LOCALCONFIG,
            20,
            "the response timeout for NoOpRequest"));
    mConfVars.put(
        "web.upstream.waitset.timeout",
        new TimeoutVar(
            "web.upstream.waitset.timeout",
            "zimbra_waitset_max_request_timeout",
            1200,
            ProxyConfOverride.LOCALCONFIG,
            20,
            "the response timeout for WaitSetRequest"));
    mConfVars.put(
        "main.accept_mutex",
        new ProxyConfVar(
            "main.accept_mutex",
            ZAttrProvisioning.A_zimbraReverseProxyAcceptMutex,
            "on",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "accept_mutex flag for NGINX - can be on|off - on indicates regular distribution, off"
                + " gets better distribution of client connections between workers"));
    mConfVars.put("web.ews.upstream.disable", new EwsEnablerVar());
    mConfVars.put("web.upstream.ewsserver.:servers", new WebEwsUpstreamServersVar());
    mConfVars.put("web.ssl.upstream.ewsserver.:servers", new WebEwsSSLUpstreamServersVar());
    mConfVars.put(
        "web.ews.upstream.name",
        new ProxyConfVar(
            "web.ews.upstream.name",
            null,
            ZIMBRA_UPSTREAM_EWS_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for ews upstream server cluster"));
    mConfVars.put(
        "web.ssl.ews.upstream.name",
        new ProxyConfVar(
            "web.ssl.ews.upstream.name",
            null,
            ZIMBRA_SSL_UPSTREAM_EWS_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for https ews upstream server cluster"));
    mConfVars.put("web.login.upstream.disable", new LoginEnablerVar());
    mConfVars.put("web.upstream.loginserver.:servers", new WebLoginUpstreamServersVar());
    mConfVars.put("web.ssl.upstream.loginserver.:servers", new WebLoginSSLUpstreamServersVar());
    mConfVars.put(
        "web.login.upstream.name",
        new ProxyConfVar(
            "web.login.upstream.name",
            null,
            ZIMBRA_UPSTREAM_LOGIN_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for upstream login server cluster"));
    mConfVars.put(
        "web.ssl.login.upstream.name",
        new ProxyConfVar(
            "web.ssl.login.upstream.name",
            null,
            ZIMBRA_SSL_UPSTREAM_LOGIN_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for https upstream login server cluster"));
    mConfVars.put(
        "web.login.upstream.url",
        new ProxyConfVar(
            "web.login.upstream.url",
            ZAttrProvisioning.A_zimbraMailURL,
            "/",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "Zimbra Login URL"));
    mConfVars.put("web.upstream.login.target", new WebProxyUpstreamLoginTargetVar());
    mConfVars.put("web.upstream.ews.target", new WebProxyUpstreamEwsTargetVar());
    mConfVars.put(
        "ssl.session.timeout",
        new TimeInSecVarWrapper(
            new ProxyConfVar(
                "ssl.session.timeout",
                ZAttrProvisioning.A_zimbraReverseProxySSLSessionTimeout,
                600L,
                ProxyConfValueType.TIME,
                ProxyConfOverride.SERVER,
                "SSL session timeout value for the proxy in secs")));
    mConfVars.put("ssl.session.cachesize", new WebSSLSessionCacheSizeVar());
    mConfVars.put("web.xmpp.upstream.proto", new XmppBoshProxyUpstreamProtoVar());
    mConfVars.put("web.xmpp.bosh.upstream.disable", new WebXmppBoshEnablerVar());
    mConfVars.put(
        "web.xmpp.bosh.enabled",
        new ProxyConfVar(
            "web.xmpp.bosh.enabled",
            ZAttrProvisioning.A_zimbraReverseProxyXmppBoshEnabled,
            true,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Indicates whether XMPP/Bosh Reverse Proxy is enabled"));
    mConfVars.put(
        "web.xmpp.local.bind.url",
        new ProxyConfVar(
            "web.xmpp.local.bind.url",
            ZAttrProvisioning.A_zimbraReverseProxyXmppBoshLocalHttpBindURL,
            "/http-bind",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "Local HTTP-BIND URL prefix where ZWC sends XMPP over BOSH requests"));
    mConfVars.put(
        "web.xmpp.remote.bind.url",
        new ProxyConfVar(
            "web.xmpp.remote.bind.url",
            ZAttrProvisioning.A_zimbraReverseProxyXmppBoshRemoteHttpBindURL,
            "",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "Remote HTTP-BIND URL prefix for an external XMPP server where XMPP over BOSH requests"
                + " need to be proxied"));
    mConfVars.put(
        "web.xmpp.bosh.hostname",
        new ProxyConfVar(
            "web.xmpp.bosh.hostname",
            ZAttrProvisioning.A_zimbraReverseProxyXmppBoshHostname,
            "",
            ProxyConfValueType.STRING,
            ProxyConfOverride.SERVER,
            "Hostname of the external XMPP server where XMPP over BOSH requests need to be"
                + " proxied"));
    mConfVars.put(
        "web.xmpp.bosh.port",
        new ProxyConfVar(
            "web.xmpp.bosh.port",
            ZAttrProvisioning.A_zimbraReverseProxyXmppBoshPort,
            0,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Port number of the external XMPP server where XMPP over BOSH requests need to be"
                + " proxied"));
    mConfVars.put(
        "web.xmpp.bosh.timeout",
        new TimeInSecVarWrapper(
            new ProxyConfVar(
                "web.xmpp.bosh.timeout",
                ZAttrProvisioning.A_zimbraReverseProxyXmppBoshTimeout,
                60L,
                ProxyConfValueType.TIME,
                ProxyConfOverride.SERVER,
                "the response timeout for an external XMPP/BOSH server")));
    mConfVars.put(
        "web.xmpp.bosh.use_ssl",
        new ProxyConfVar(
            "web.xmpp.bosh.use_ssl",
            ZAttrProvisioning.A_zimbraReverseProxyXmppBoshSSL,
            true,
            ProxyConfValueType.ENABLER,
            ProxyConfOverride.SERVER,
            "Indicates whether XMPP/Bosh uses SSL"));
    ProxyConfVar webSslDhParamFile =
        new ProxyConfVar(
            "web.ssl.dhparam.file",
            null,
            DEFAULT_DH_PARAM_FILE,
            ProxyConfValueType.STRING,
            ProxyConfOverride.NONE,
            "Filename with DH parameters for EDH ciphers to be used by the proxy");
    mConfVars.put("web.ssl.dhparam.enabled", new WebSSLDhparamEnablerVar(webSslDhParamFile));
    mConfVars.put("web.ssl.dhparam.file", webSslDhParamFile);
    mConfVars.put("upstream.fair.shm.size", new ProxyFairShmVar());
    mConfVars.put("web.strict.servername", new WebStrictServerName());
    mConfVars.put("web.upstream.zx", new WebProxyUpstreamZxTargetVar());
    mConfVars.put(
        "web.upstream.zx.name",
        new ProxyConfVar(
            "web.upstream.zx.name",
            null,
            ZIMBRA_UPSTREAM_ZX_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for HTTP zx upstream"));
    mConfVars.put(
        "web.ssl.upstream.zx.name",
        new ProxyConfVar(
            "web.ssl.upstream.zx.name",
            null,
            ZIMBRA_SSL_UPSTREAM_ZX_NAME,
            ProxyConfValueType.STRING,
            ProxyConfOverride.CONFIG,
            "Symbolic name for HTTPS zx upstream"));
    mConfVars.put("web.upstream.zx.:servers", new WebUpstreamZxServersVar());
    mConfVars.put("web.ssl.upstream.zx.:servers", new WebSslUpstreamZxServersVar());
    mConfVars.put(
        "web.carbonio.admin.port",
        new ProxyConfVar(
            "web.admin.login.port",
            ZAttrProvisioning.A_carbonioAdminProxyPort,
            6071,
            ProxyConfValueType.INTEGER,
            ProxyConfOverride.SERVER,
            "Carbonio proxy admin login port"));

    // Get the response headers list from globalconfig
    String[] rspHeaders =
        ProxyConfVar.configSource.getMultiAttr(
            ZAttrProvisioning.A_zimbraReverseProxyResponseHeaders);
    ArrayList<String> responseHeadersList = new ArrayList<>();
    Collections.addAll(responseHeadersList, rspHeaders);
    mConfVars.put(
        "web.add.headers.default",
        new AddHeadersVar(
            "web.add.headers.default",
            responseHeadersList,
            "add_header directive for default web proxy"));
  }

  /* update the default variable map from the active configuration */
  public static void updateDefaultVars() throws ServiceException, ProxyConfException {
    Set<String> keys = mConfVars.keySet();
    for (String key : keys) {
      mConfVars.get(key).update();
      mVars.put(key, mConfVars.get(key).confValue());
    }
  }

  /* update the default domain variable map from the active configuration */
  public static void updateDefaultDomainVars() throws ServiceException, ProxyConfException {
    Set<String> keys = mDomainConfVars.keySet();
    for (String key : keys) {
      mDomainConfVars.get(key).update();
      mVars.put(key, mDomainConfVars.get(key).confValue());
    }
  }

  public static void updateListenAddresses() throws ProxyConfException {
    mVars.put("listen.:addresses", new ListenAddressesVar(mListenAddresses).confValue());
  }

  public static void overrideDefaultVars(CommandLine cl) {
    String[] overrides = cl.getOptionValues('c');

    if (overrides != null) {
      for (String o : overrides) {
        LOG.debug("Processing config override " + o);
        int e = o.indexOf("=");
        if (e <= 0) {
          LOG.info("Ignoring config override " + o + " because it is not of the form name=value");
        } else {
          String k = o.substring(0, e);
          String v = o.substring(e + 1);

          if (mVars.containsKey(k)) {
            LOG.info("Overriding config variable " + k + " with " + v);
            mVars.put(k, v);
          } else {
            LOG.info("Ignoring non-existent config variable " + k);
          }
        }
      }
    }
  }

  /* Indicate whether configuration is valid, taking into consideration "essential" configuration values */
  @SuppressWarnings("unchecked")
  public static boolean isWorkableConf() {

    ArrayList<String> webUpstreamServers;
    ArrayList<String> webUpstreamClientServers;
    ArrayList<String> zmLookupHandlers;
    ArrayList<String> webSSLUpstreamServers;
    ArrayList<String> webSSLUpstreamClientServers;

    boolean validConf = true;
    boolean webEnabled = (Boolean) mConfVars.get("web.enabled").rawValue();
    boolean mailEnabled = (Boolean) mConfVars.get("mail.enabled").rawValue();

    webUpstreamServers = (ArrayList<String>) mConfVars.get("web.upstream.:servers").rawValue();
    webUpstreamClientServers =
        (ArrayList<String>) mConfVars.get("web.upstream.webclient.:servers").rawValue();
    webSSLUpstreamServers =
        (ArrayList<String>) mConfVars.get("web.ssl.upstream.:servers").rawValue();
    webSSLUpstreamClientServers =
        (ArrayList<String>) mConfVars.get("web.ssl.upstream.webclient.:servers").rawValue();
    zmLookupHandlers = (ArrayList<String>) mConfVars.get("zmlookup.:handlers").rawValue();

    if (webEnabled && (webUpstreamServers.isEmpty() || webUpstreamClientServers.isEmpty())) {
      LOG.info("Web is enabled but there are no HTTP upstream webclient/mailclient servers");
      validConf = false;
    }

    if (webEnabled && (webSSLUpstreamServers.isEmpty() || webSSLUpstreamClientServers.isEmpty())) {
      LOG.info("Web is enabled but there are no HTTPS upstream webclient/mailclient servers");
      validConf = false;
    }

    if ((webEnabled || mailEnabled) && (zmLookupHandlers.isEmpty())) {
      LOG.info("Proxy is enabled but there are no lookup handlers");
      validConf = false;
    }
    return validConf;
  }

  public static int createConf(String[] args) throws ServiceException, ProxyConfException {
    int exitCode = 0;
    CommandLine cl = parseArgs(args);

    if (cl == null) {
      exitCode = 1;
      return (exitCode);
    }

    if (cl.hasOption('v')) { // BUG 51624, must initialize log4j first
      CliUtil.toolSetup("DEBUG");
    } else {
      CliUtil.toolSetup("INFO");
    }

    mProv = Provisioning.getInstance();
    ProxyConfVar.configSource = mProv.getConfig();
    ProxyConfVar.serverSource = mProv.getLocalServer();

    if (cl.hasOption('h')) {
      usage(null);
      return (exitCode);
    }

    if (cl.hasOption('n')) {
      mDryRun = true;
    }

    if (cl.hasOption('w')) {
      mWorkingDir = cl.getOptionValue('w');
      mConfDir = mWorkingDir + "/conf";
      mTemplateDir = mWorkingDir + "/conf/nginx/templates";
      mConfIncludesDir = mConfDir + "/" + mIncDir;
    }

    if (cl.hasOption('i')) {
      mIncDir = cl.getOptionValue('i');
      mConfIncludesDir = mConfDir + "/" + mIncDir;
    }

    if (cl.hasOption('t')) {
      hasCustomTemplateLocationArg = true;
      mTemplateDir = cl.getOptionValue('t');
    }

    LOG.debug("Working Directory: " + mWorkingDir);
    LOG.debug("Template Directory: " + mTemplateDir);
    LOG.debug("Config Includes Directory: " + mConfIncludesDir);

    if (cl.hasOption('p')) {
      mConfPrefix = cl.getOptionValue('p');
      mTemplatePrefix = mConfPrefix;
    }

    if (cl.hasOption('P')) {
      mTemplatePrefix = cl.getOptionValue('P');
    }

    LOG.debug("Config File Prefix: " + mConfPrefix);
    LOG.debug("Template File Prefix: " + mTemplatePrefix);

    /* set up the default variable map */
    LOG.debug("Building Default Variable Map");
    buildDefaultVars();

    if (cl.hasOption('d')) {
      displayDefaultVariables();
      return (exitCode);
    }

    /* If a server object has been provided, then use that */
    if (cl.hasOption('s')) {
      String mHost = cl.getOptionValue('s');
      LOG.info("Loading server object: " + mHost);
      try {
        ProxyConfVar.serverSource = getServer(mHost);
      } catch (ProxyConfException pe) {
        LOG.error("Cannot load server object. Make sure the server specified with -s exists");
        exitCode = 1;
        return (exitCode);
      }
    }

    mEnforceDNSResolution = cl.hasOption('f');

    mGenConfPerVhn =
        ProxyConfVar.serverSource.getBooleanAttr(
            ZAttrProvisioning.A_zimbraReverseProxyGenConfigPerVirtualHostname, false);

    try {
      /* upgrade the variable map from the config in force */
      LOG.debug("Loading Attrs in Domain Level");
      mDomainReverseProxyAttrs = loadDomainReverseProxyAttrs();
      mServerAttrs = loadServerAttrs();
      updateListenAddresses();

      LOG.debug("Updating Default Variable Map");
      updateDefaultVars();

      LOG.debug("Processing Config Overrides");
      overrideDefaultVars(cl);

      String clientCA = loadAllClientCertCA();
      writeClientCAtoFile(clientCA);
    } catch (ProxyConfException | ServiceException pe) {
      handleException(pe);
      exitCode = 1;
    }

    if (exitCode > 0) {
      LOG.info("Proxy configuration files generation is interrupted by errors");
      return exitCode;
    }
    if (cl.hasOption('D')) {
      displayVariables();
      return (exitCode);
    }

    if (cl.getArgs().length > 0) {
      usage(null);
      return (exitCode);
    }

    if (!isWorkableConf()) {
      LOG.warn(
          "Configuration is not valid because no route lookup handlers exist, or because no"
              + " HTTP/HTTPS upstream servers were found");
      LOG.warn("Please ensure that the output of 'zmprov garpu/garpb' returns at least one entry");
    }

    try {
      File confDir = new File(mConfDir, "");
      String confPath = confDir.getAbsolutePath();
      if (!confDir.canRead()) {
        throw new ProxyConfException("Cannot read configuration directory " + confPath);
      }
      if (!confDir.canWrite()) {
        throw new ProxyConfException("Cannot write to configuration directory " + confPath);
      }
      if (!confDir.exists()) {
        throw new ProxyConfException(
            "Configuration directory " + confDir.getAbsolutePath() + " does not exist");
      }

      expandTemplate(
          new File(mTemplateDir, getCoreConfTemplate()),
          new File(
              mConfDir,
              getCoreConf())); /* Only core nginx conf goes to mConfDir, rest to mConfIncludesDir */
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("main")),
          new File(mConfIncludesDir, getConfFileName("main")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("memcache")),
          new File(mConfIncludesDir, getConfFileName("memcache")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("zmlookup")),
          new File(mConfIncludesDir, getConfFileName("zmlookup")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("mail")),
          new File(mConfIncludesDir, getConfFileName("mail")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("mail.imap")),
          new File(mConfIncludesDir, getConfFileName("mail.imap")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("mail.imap.default")),
          new File(mConfIncludesDir, getConfFileName("mail.imap.default")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("mail.imaps")),
          new File(mConfIncludesDir, getConfFileName("mail.imaps")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("mail.imaps.default")),
          new File(mConfIncludesDir, getConfFileName("mail.imaps.default")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("mail.pop3")),
          new File(mConfIncludesDir, getConfFileName("mail.pop3")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("mail.pop3.default")),
          new File(mConfIncludesDir, getConfFileName("mail.pop3.default")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("mail.pop3s")),
          new File(mConfIncludesDir, getConfFileName("mail.pop3s")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("mail.pop3s.default")),
          new File(mConfIncludesDir, getConfFileName("mail.pop3s.default")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("web")),
          new File(mConfIncludesDir, getConfFileName("web")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("web.http")),
          new File(mConfIncludesDir, getConfFileName("web.http")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("web.http.default")),
          new File(mConfIncludesDir, getConfFileName("web.http.default")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("web.https")),
          new File(mConfIncludesDir, getConfFileName("web.https")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("web.https.default")),
          new File(mConfIncludesDir, getConfFileName("web.https.default")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("web.sso")),
          new File(mConfIncludesDir, getConfFileName("web.sso")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("web.sso.default")),
          new File(mConfIncludesDir, getConfFileName("web.sso.default")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("web.admin")),
          new File(mConfIncludesDir, getConfFileName("web.admin")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("web.admin.default")),
          new File(mConfIncludesDir, getConfFileName("web.admin.default")));
      expandTemplate(
          new File(mTemplateDir, getWebHttpModeConfTemplate("http")),
          new File(mConfIncludesDir, getWebHttpModeConf("http")));
      expandTemplate(
          new File(mTemplateDir, getWebHttpModeConfTemplate("https")),
          new File(mConfIncludesDir, getWebHttpModeConf("https")));
      expandTemplate(
          new File(mTemplateDir, getWebHttpModeConfTemplate("both")),
          new File(mConfIncludesDir, getWebHttpModeConf("both")));
      expandTemplate(
          new File(mTemplateDir, getWebHttpModeConfTemplate("redirect")),
          new File(mConfIncludesDir, getWebHttpModeConf("redirect")));
      expandTemplate(
          new File(mTemplateDir, getWebHttpModeConfTemplate("mixed")),
          new File(mConfIncludesDir, getWebHttpModeConf("mixed")));
      expandTemplate(
          new File(mTemplateDir, getWebHttpSModeConfTemplate("http")),
          new File(mConfIncludesDir, getWebHttpSModeConf("http")));
      expandTemplate(
          new File(mTemplateDir, getWebHttpSModeConfTemplate("https")),
          new File(mConfIncludesDir, getWebHttpSModeConf("https")));
      expandTemplate(
          new File(mTemplateDir, getWebHttpSModeConfTemplate("both")),
          new File(mConfIncludesDir, getWebHttpSModeConf("both")));
      expandTemplate(
          new File(mTemplateDir, getWebHttpSModeConfTemplate("redirect")),
          new File(mConfIncludesDir, getWebHttpSModeConf("redirect")));
      expandTemplate(
          new File(mTemplateDir, getWebHttpSModeConfTemplate("mixed")),
          new File(mConfIncludesDir, getWebHttpSModeConf("mixed")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("docs.common")),
          new File(mConfIncludesDir, getConfFileName("docs.common")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("docs.upstream")),
          new File(mConfIncludesDir, getConfFileName("docs.upstream")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("web.carbonio.admin.default")),
          new File(mConfIncludesDir, getConfFileName("web.carbonio.admin.default")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("web.carbonio.admin")),
          new File(mConfIncludesDir, getConfFileName("web.carbonio.admin")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("messaging")),
          new File(mConfIncludesDir, getConfFileName("messaging")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("messaging.xmpp.default")),
          new File(mConfIncludesDir, getConfFileName("messaging.xmpp.default")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("messaging.xmpps.default")),
          new File(mConfIncludesDir, getConfFileName("messaging.xmpps.default")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("messaging.xmpp")),
          new File(mConfIncludesDir, getConfFileName("messaging.xmpp")));
      expandTemplate(
          new File(mTemplateDir, getConfTemplateFileName("messaging.xmpps")),
          new File(mConfIncludesDir, getConfFileName("messaging.xmpps")));
    } catch (ProxyConfException | SecurityException pe) {
      handleException(pe);
      exitCode = 1;
    }
    if (!mDryRun) {
      if (exitCode == 0) {
        LOG.info("Proxy configuration files are generated successfully");
        appendConfGenResultToConf("__SUCCESS__");
      } else {
        LOG.info("Proxy configuration files generation is interrupted by errors");
      }
    }
    return (exitCode);
  }

  private static void handleException(Exception e) {
    LOG.error("Error while expanding templates: " + e.getMessage());
    appendConfGenResultToConf("__CONF_GEN_ERROR__:" + e.getMessage());
  }

  /**
   * bug 66072#c3, always append the conf generation result to <zimbr home>/conf/nginx.conf. In this
   * way, zmnginxctl restart can detect the problem.
   */
  private static void appendConfGenResultToConf(String text) {
    File confFile = new File(mConfDir, getCoreConf());
    if (!confFile.exists()) {
      return;
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(confFile, true))) {
      writer.write("\n#" + text + "\n");
    } catch (IOException e) {
      // do nothing
    }
  }

  private static void writeClientCAtoFile(String clientCA) throws ServiceException {
    int exitCode;
    ProxyConfVar clientCAEnabledVar;
    final String keyword = "ssl.clientcertca.enabled";

    if (ProxyConfUtil.isEmptyString(clientCA)) {
      clientCAEnabledVar =
          new ProxyConfVar(
              keyword,
              null,
              false,
              ProxyConfValueType.ENABLER,
              ProxyConfOverride.CUSTOM,
              "is there valid client ca cert");

      if (isClientCertVerifyEnabled() || isDomainClientCertVerifyEnabled()) {
        LOG.error("Client certificate verification is enabled but no client cert ca is provided");
        exitCode = 1;
        System.exit(exitCode);
      }

    } else {
      clientCAEnabledVar =
          new ProxyConfVar(
              keyword,
              null,
              true,
              ProxyConfValueType.ENABLER,
              ProxyConfOverride.CUSTOM,
              "is there valid client ca cert");
      LOG.debug("Write Client CA file");
      ProxyConfUtil.writeContentToFile(clientCA, getDefaultClientCertCaPath());
    }
    mConfVars.put(keyword, clientCAEnabledVar);
    try {
      mVars.put(keyword, clientCAEnabledVar.confValue());
    } catch (ProxyConfException e) {
      LOG.error("ProxyConfException during format ssl.clientcertca.enabled", e);
      System.exit(1);
    }
  }

  /** check whether client cert verify is enabled in server level */
  static boolean isClientCertVerifyEnabled() {
    String globalMode =
        ProxyConfVar.serverSource.getAttr(
            ZAttrProvisioning.A_zimbraReverseProxyClientCertMode, "off");

    return globalMode.equals("on") || globalMode.equals("optional");
  }

  /** check whether client cert verify is enabled in domain level */
  static boolean isDomainClientCertVerifyEnabled() {
    for (DomainAttrItem item : mDomainReverseProxyAttrs) {
      if (item.clientCertMode != null
          && (item.clientCertMode.equals("on") || item.clientCertMode.equals("optional"))) {
        return true;
      }
    }

    return false;
  }

  public static void main(String[] args) throws ServiceException, ProxyConfException {
    int exitCode = createConf(args);
    System.exit(exitCode);
  }
}
