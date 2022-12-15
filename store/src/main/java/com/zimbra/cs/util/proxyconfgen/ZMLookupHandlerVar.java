package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

class ZMLookupHandlerVar extends ProxyConfVar {

  public ZMLookupHandlerVar() {
    super(
        "zmlookup.:handlers",
        ZAttrProvisioning.A_zimbraReverseProxyLookupTarget,
        new ArrayList<String>(),
        ProxyConfValueType.CUSTOM,
        ProxyConfOverride.CUSTOM,
        "List of nginx lookup handlers (i.e. servers for which"
            + " zimbraReverseProxyLookupTarget is true)");
  }

  @Override
  public void update() throws ServiceException, ProxyConfException {
    ArrayList<String> servers = new ArrayList<>();
    int numFailedHandlers = 0;

    String[] handlerNames =
        serverSource.getMultiAttr(ZAttrProvisioning.A_zimbraReverseProxyAvailableLookupTargets);
    if (handlerNames.length > 0) {
      for (String handlerName : handlerNames) {
        Server s = mProv.getServerByName(handlerName);
        if (s != null) {
          String sn = s.getAttr(ZAttrProvisioning.A_zimbraServiceHostname, "");
          int port = s.getIntAttr(ZAttrProvisioning.A_zimbraExtensionBindPort, 7072);
          String proto = "https://";
          boolean isTarget =
              s.getBooleanAttr(ZAttrProvisioning.A_zimbraReverseProxyLookupTarget, false);
          if (isTarget) {
            try {
              InetAddress ip = ProxyConfUtil.getLookupTargetIPbyIPMode(sn);
              Formatter f = new Formatter();
              if (ip instanceof Inet4Address) {
                f.format("%s%s:%d", proto, ip.getHostAddress(), port);
              } else {
                f.format("%s[%s]:%d", proto, ip.getHostAddress(), port);
              }
              servers.add(f.toString());
              f.close();
              mLog.debug("Route Lookup: Added server " + ip);
            } catch (ProxyConfException pce) {
              numFailedHandlers++;
              mLog.error("Error resolving service host name: '" + sn + "'", pce);
            }
          }
        } else {
          mLog.warn(
              "Invalid value found in 'zimbraReverseProxyAvailableLookupTargets': "
                  + handlerName
                  + "\nPlease correct and run zmproxyconfgen again");
        }
      }
    } else {
      List<Server> allServers = mProv.getAllServers();

      for (Server s : allServers) {
        String sn = s.getAttr(ZAttrProvisioning.A_zimbraServiceHostname, "");
        int port = s.getIntAttr(ZAttrProvisioning.A_zimbraExtensionBindPort, 7072);
        String proto = "https://";
        boolean isTarget =
            s.getBooleanAttr(ZAttrProvisioning.A_zimbraReverseProxyLookupTarget, false);
        if (isTarget) {
          try {
            InetAddress ip = ProxyConfUtil.getLookupTargetIPbyIPMode(sn);
            Formatter f = new Formatter();
            if (ip instanceof Inet4Address) {
              f.format("%s%s:%d", proto, ip.getHostAddress(), port);
            } else {
              f.format("%s[%s]:%d", proto, ip.getHostAddress(), port);
            }
            servers.add(f.toString());
            f.close();
            mLog.debug("Route Lookup: Added server " + ip);
          } catch (ProxyConfException pce) {
            numFailedHandlers++;
            mLog.error("Error resolving service host name: '" + sn + "'", pce);
          }
        }
      }
    }
    if (servers.isEmpty()) {
      if (numFailedHandlers > 0) {
        throw new ProxyConfException("No available nginx lookup handlers could be contacted");
      } else {
        mLog.warn("No available nginx lookup handlers could be found");
      }
    }
    mValue = servers;
  }

  @Override
  public String format(Object o) throws ProxyConfException {
    String reverseProxyPath = ExtensionDispatcherServlet.EXTENSION_PATH + "/nginx-lookup";
    @SuppressWarnings("unchecked")
    ArrayList<String> servers = (ArrayList<String>) o;
    if (servers.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (String s : servers) {
      sb.append(s).append(reverseProxyPath);
      sb.append(' ');
    }
    sb.setLength(sb.length() - 1); // trim the last space
    return sb.toString();
  }
}
