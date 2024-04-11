// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.soap.AdminConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.google.common.collect.Sets;
import com.zimbra.common.account.Key;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.CsvReader;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetServiceStatusResponse;
import com.zimbra.soap.admin.type.ServiceStatus;
import com.zimbra.soap.admin.type.TimeZoneInfo;
import com.zimbra.soap.type.ZeroOrOne;

/** @author schemers */
public class GetServiceStatus extends AdminDocumentHandler {
  private static final String ZMRRDFETCH = LC.zimbra_home.value() + "/libexec/zmrrdfetch";
  private static final String ZMSTATUSLOG_CSV = "zmstatuslog";

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    // this command can only execute on the monitor host, so proxy if necessary
    Provisioning prov = Provisioning.getInstance();
    String monitorHost = prov.getConfig().getAttr(Provisioning.A_zimbraLogHostname);
    if (monitorHost == null || monitorHost.trim().equals(""))
      throw ServiceException.FAILURE("zimbraLogHostname is not configured", null);
    Server monitorServer = prov.get(Key.ServerBy.name, monitorHost);
    if (monitorServer == null)
      throw ServiceException.FAILURE(
          "could not find zimbraLogHostname server: " + monitorServer, null);
    if (!prov.getLocalServer().getId().equalsIgnoreCase(monitorServer.getId()))
      return proxyRequest(request, context, monitorServer);

    GetServiceStatusResponse resp = new GetServiceStatusResponse();

    TimeZone tz = TimeZone.getDefault();
    TimeZoneInfo timezone = TimeZoneInfo.fromIdAndDisplayName(tz.getID(), tz.getDisplayName());
    resp.setTimezone(timezone);

    boolean loggerEnabled = false;
    Server local = prov.getLocalServer();
    String[] services = local.getMultiAttr(Provisioning.A_zimbraServiceEnabled);
    if (services != null) {
      for (int i = 0; i < services.length && !loggerEnabled; i++) {
        loggerEnabled = "logger".equals(services[i]);
      }
    }
    if (loggerEnabled) {
      HashSet<ServiceStatus> serviceStatus = Sets.newHashSet();
      List<Server> servers = prov.getAllServers();
      for (Server s : servers) {
        String[] srvs = s.getMultiAttr(Provisioning.A_zimbraServiceEnabled);
        for (String service : srvs) {
          serviceStatus.add(
              ServiceStatus.fromServerServiceTimeStatus(
                  s.getName(), service, System.currentTimeMillis() / 1000, ZeroOrOne.ZERO));
        }
      }
      BufferedReader in = null;
      try {
        ProcessBuilder pb = new ProcessBuilder(ZMRRDFETCH, "-f", ZMSTATUSLOG_CSV);
        Process p = pb.start();
        in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        Map<String, CsvReader> hostStatus = new HashMap<>();
        StringWriter currentWriter = null;
        String currentHost = null;
        String line;
        while ((line = in.readLine()) != null) {
          if ("".equals(line.trim())) continue;
          if (line.startsWith("Host: ")) {
            if (currentHost != null)
              hostStatus.put(
                  currentHost, new CsvReader(new StringReader(currentWriter.toString())));
            currentHost = line.substring("Host: ".length());
            currentWriter = new StringWriter();
          } else {
            if (currentWriter != null) currentWriter.write(line + "\n");
          }
        }
        if (currentHost != null)
          hostStatus.put(currentHost, new CsvReader(new StringReader(currentWriter.toString())));
        List<ServiceStatus> status = ServiceStatus.parseData(hostStatus);
        String[] skippedServices = {"zimlet", "zimbra", "zimbraAdmin", "zmconfigd"};
        Element onlyCarbonioServicesEl =
            request.getOptionalElement(AdminConstants.A_CARBONIO_SERVICES_ONLY);
        String onlyCarbonioServicesElStr =
            onlyCarbonioServicesEl != null ? onlyCarbonioServicesEl.getText() : "0";
        boolean reqContainsOnlyCarbonioServices =
            onlyCarbonioServicesElStr.equalsIgnoreCase("true")
                || onlyCarbonioServicesElStr.equalsIgnoreCase("1");

        for (ServiceStatus stat : status) {
          serviceStatus.remove(stat);
          if (reqContainsOnlyCarbonioServices){
            if(Arrays.stream(skippedServices).noneMatch(stat.getService()::equals)) {
              serviceStatus.add(stat);
            }
          }else{
            serviceStatus.add(stat);
          }
        }
        for (ServiceStatus stat : serviceStatus) {
          if (!checkRights(zsc, stat.getServer())) {
            ZimbraLog.misc.info(
                "skipping server " + stat.getServer() + ", has not right to get service status");
            continue;
          }
          resp.addServiceStatus(stat);
        }
      } catch (IOException e) {
        try {
          if (in != null) in.close();
        } catch (IOException ignored) {
        }
        ServiceException.FAILURE("Unable to read logger stats", e);
      }
    }
    return zsc.jaxbToElement(resp);
  }

  private boolean checkRights(ZimbraSoapContext zsc, String serverName) throws ServiceException {
    try {
      Server server = Provisioning.getInstance().get(Key.ServerBy.name, serverName);
      checkRight(zsc, server, Admin.R_getServiceStatus);
    } catch (ServiceException e) {
      // if PERM_DENIED, return false and log, do not throw, so we
      // can continue with the next entry
      if (ServiceException.PERM_DENIED.equals(e.getCode())) {
        return false;
      } else {
        // encountered a real error, rethrow
        throw e;
      }
    }

    return true;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_getServiceStatus);
  }
}
