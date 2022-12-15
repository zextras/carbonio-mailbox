package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import java.util.ArrayList;

class ReverseProxyIPThrottleWhitelist extends ProxyConfVar {

  public ReverseProxyIPThrottleWhitelist() {
    super(
        "mail.whitelistip.:servers",
        null,
        null,
        ProxyConfValueType.CUSTOM,
        ProxyConfOverride.CUSTOM,
        "List of Client IP addresses immune to IP Throttling");
  }

  @Override
  public void update() throws ServiceException {
    ArrayList<String> directives = new ArrayList<>();
    String[] ips =
        serverSource.getMultiAttr(ZAttrProvisioning.A_zimbraReverseProxyIPThrottleWhitelist);
    for (String ip : ips) {
      directives.add(ip);
      mLog.debug("Added %s IP Throttle whitelist", ip);
    }
    mValue = directives;
  }

  @Override
  public String format(Object o) {
    @SuppressWarnings("unchecked")
    ArrayList<String> servers = (ArrayList<String>) o;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < servers.size(); i++) {
      String s = servers.get(i);
      if (i == 0) {
        sb.append(String.format("mail_whitelist_ip    %s;%n", s));
      } else {
        sb.append(String.format("    mail_whitelist_ip    %s;%n", s));
      }
    }
    return sb.toString();
  }
}
