// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.server.ServerConfig;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

public class DoSFilter extends org.eclipse.jetty.servlets.DoSFilter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    LogFactory.init();
    super.init(filterConfig);
    try {
      List<Server> servers = Provisioning.getInstance().getAllServers(Provisioning.SERVICE_MAILBOX);
      for (Server server : servers) {
        try {
          InetAddress[] addresses = InetAddress.getAllByName(server.getServiceHostname());
          for (InetAddress address : addresses) {
            addWhitelistAddress(address.getHostAddress());
          }
        } catch (UnknownHostException e) {
          ZimbraLog.misc.warn("Invalid hostname: " + server.getServiceHostname(), e);
        }
      }
      String[] addrs =
          ServerConfig.getAddrListCsv(
              Provisioning.getInstance().getLocalServer().getHttpThrottleSafeIPs());
      for (String addr : addrs) {
        addWhitelistAddress(addr);
      }
    } catch (ServiceException e) {
      ZimbraLog.misc.warn("Unable to get throttle safe IPs", e);
    }
    // add loopback addresses
    addWhitelistAddress("127.0.0.1");
    addWhitelistAddress("::1");
    addWhitelistAddress("0:0:0:0:0:0:0:1");
    ZimbraLog.misc.info("DoSFilter: Configured whitelist IPs = " + getWhitelist());
  }

  @Override
  public boolean addWhitelistAddress(String address) {
    ZimbraLog.misc.debug("added whitelist address [%s]", address);
    return super.addWhitelistAddress(address);
  }

  @Override
  protected String extractUserId(ServletRequest request) {
    return ZimbraQoSFilter.extractUserId(request);
  }
}
