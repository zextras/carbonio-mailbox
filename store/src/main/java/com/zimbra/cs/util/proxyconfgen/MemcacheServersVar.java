package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

class MemcacheServersVar extends ProxyConfVar {

  public MemcacheServersVar() {
    super(
        "memcache.:servers",
        null,
        null,
        ProxyConfValueType.CUSTOM,
        ProxyConfOverride.CUSTOM,
        "List of known memcache servers (i.e. servers having memcached service enabled)");
  }

  @Override
  public void update() throws ServiceException, ProxyConfException {
    ArrayList<String> servers = new ArrayList<>();

    /* $(zmprov gamcs) */
    List<Server> mcs = mProv.getAllServers(Provisioning.SERVICE_MEMCACHED);
    for (Server mc : mcs) {
      String serverName = mc.getAttr(ZAttrProvisioning.A_zimbraMemcachedBindAddress, "");
      int serverPort = mc.getIntAttr(ZAttrProvisioning.A_zimbraMemcachedBindPort, 11211);
      try {
        InetAddress ip = ProxyConfUtil.getLookupTargetIPbyIPMode(serverName);

        Formatter f = new Formatter();
        if (ip instanceof Inet4Address) {
          f.format("%s:%d", ip.getHostAddress(), serverPort);
        } else {
          f.format("[%s]:%d", ip.getHostAddress(), serverPort);
        }

        servers.add(f.toString());
        f.close();
      } catch (ProxyConfException pce) {
        mLog.error("Error resolving memcached host name: '" + serverName + "'", pce);
      }
    }
    if (servers.isEmpty()) {
      throw new ProxyConfException("No available memcached servers could be contacted");
    }
    mValue = servers;
  }

  @Override
  public String format(Object o) {
    @SuppressWarnings("unchecked")
    ArrayList<String> servers = (ArrayList<String>) o;
    StringBuilder conf = new StringBuilder();
    for (String s : servers) {
      conf.append("  servers   ");
      conf.append(s);
      conf.append(";\n");
    }
    return conf.toString();
  }
}
