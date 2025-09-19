package com.zimbra.cs.account.commands;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.util.Pair;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import java.util.List;

class ReloadMemcachedClientConfigCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public ReloadMemcachedClientConfigCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doReloadMemcachedClientConfig(args);
  }

  private void doReloadMemcachedClientConfig(String[] args) throws ServiceException {
    List<Pair<String, Integer>> servers = provUtil.getMailboxServersFromArgs(args);
    // Send command to each server.
    var verboseMode = provUtil.getVerboseMode();
    var console = provUtil.getConsole();
    var debugLevel = provUtil.getDebugLevel();
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
        if (debugLevel != ProvUtil.SoapDebugLevel.none) {
          sp.soapSetHttpTransportDebugListener(provUtil);
        }
        var account = provUtil.getAccount();
        var password = provUtil.getPassword();
        if (account != null && password != null) {
          sp.soapAdminAuthenticate(account, password);
        } else if (provUtil.getAuthToken() != null) {
          sp.soapAdminAuthenticate(provUtil.getAuthToken());
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
}
