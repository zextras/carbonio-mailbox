// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.server.ServerConfig;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DoSFilter extends org.eclipse.jetty.servlets.DoSFilter {

  private List<String> whitelistedRequests;

  public List<String> getWhitelistedRequests() {
    return whitelistedRequests;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);

    whitelistedRequests = List.of(
        MailConstants.E_GET_FOLDER_REQUEST
    );

    try {
      addAllMailboxServersIpToWhiteList();
      addHttpThrottleSafeIpsToWhiteList();
    } catch (ServiceException e) {
      ZimbraLog.misc.warn("Unable to get throttle safe IPs", e);
    }
    addLogbackAddressesToWhiteList();
    ZimbraLog.misc.info("DoSFilter: Configured whitelist IPs = " + getWhitelist());
  }

  private void addLogbackAddressesToWhiteList() {
    addWhitelistAddress("127.0.0.1");
    addWhitelistAddress("::1");
    addWhitelistAddress("0:0:0:0:0:0:0:1");
  }

  private void addHttpThrottleSafeIpsToWhiteList() throws ServiceException {
    var addrs =
        ServerConfig.getAddrListCsv(
            Provisioning.getInstance().getLocalServer().getHttpThrottleSafeIPs());
    for (var addr : addrs) {
      addWhitelistAddress(addr);
    }
  }

  private void addAllMailboxServersIpToWhiteList() throws ServiceException {
    var servers = Provisioning.getInstance().getAllServers(Provisioning.SERVICE_MAILBOX);
    for (var server : servers) {
      try {
        var addresses = InetAddress.getAllByName(server.getServiceHostname());
        for (var address : addresses) {
          addWhitelistAddress(address.getHostAddress());
        }
      } catch (UnknownHostException e) {
        ZimbraLog.misc.warn("Invalid hostname: " + server.getServiceHostname(), e);
      }
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    if (isWhitelistedRequest(request)) {
      skipDosFilter(request, response, chain);
    } else {
      super.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }
  }

  void skipDosFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    ZimbraLog.filter.info(
        "Skipping DOS Filter for whitelisted HTTP request: " + ((HttpServletRequest) request).getRequestURI());
    chain.doFilter(request, response);
  }

  private boolean isWhitelistedRequest(ServletRequest request) {
    if (request instanceof HttpServletRequest) {
      var userIdOptional = this.extractUserIdOptional(request);
      var httpServletRequest = (HttpServletRequest) request;
      for (var pattern : whitelistedRequests) {
        if (httpServletRequest.getRequestURI().contains(pattern) && userIdOptional.isPresent() && !userIdOptional.get()
            .isEmpty()) {
          return true;
        }
      }
    }
    return false;
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

  protected Optional<String> extractUserIdOptional(ServletRequest request) {
    return Optional.ofNullable(ZimbraQoSFilter.extractUserId(request));
  }
}
