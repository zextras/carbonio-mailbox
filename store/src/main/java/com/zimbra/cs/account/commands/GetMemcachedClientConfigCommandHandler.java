package com.zimbra.cs.account.commands;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.util.Pair;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import net.spy.memcached.DefaultHashAlgorithm;
import java.util.List;

class GetMemcachedClientConfigCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetMemcachedClientConfigCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetMemcachedClientConfig(args);
  }

  private void doGetMemcachedClientConfig(String[] args) throws ServiceException {
    List<Pair<String, Integer>> servers = provUtil.getMailboxServersFromArgs(args);
    // Send command to each server.
    int longestHostname = 0;
    for (Pair<String, Integer> server : servers) {
      String hostname = server.getFirst();
      longestHostname = Math.max(longestHostname, hostname.length());
    }
    String hostnameFormat = String.format("%%-%ds", longestHostname);
    boolean consistent = true;
    String prevConf = null;
    var debugLevel = provUtil.getDebugLevel();
    var account = provUtil.getAccount();
    var password = provUtil.getPassword();
    var authToken = provUtil.getAuthToken();
    var console = provUtil.getConsole();
    var verboseMode = provUtil.getVerboseMode();
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
        if (debugLevel != ProvUtil.SoapDebugLevel.none) {
          sp.soapSetHttpTransportDebugListener(provUtil);
        }
        if (account != null && password != null) {
          sp.soapAdminAuthenticate(account, password);
        } else if (authToken != null) {
          sp.soapAdminAuthenticate(authToken);
        } else {
          sp.soapZimbraAdminAuthenticate();
        }
        SoapProvisioning.MemcachedClientConfig config = sp.getMemcachedClientConfig();
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
}
