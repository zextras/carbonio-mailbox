package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.service.ServiceException;
import java.util.ArrayList;

/**
 * The variable for nginx "upstream" servers block
 *
 * @author jiankuan
 */
abstract class ServersVar extends ProxyConfVar {

  protected ServersVar(String key, String description) {
    super(key, null, null, ProxyConfValueType.CUSTOM, ProxyConfOverride.CUSTOM, description);
    /*
     The port attribute name
    */
  }

  @Override
  public abstract void update() throws ServiceException;

  @Override
  public String format(Object o) {
    @SuppressWarnings("unchecked")
    ArrayList<String> servers = (ArrayList<String>) o;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < servers.size(); i++) {
      String s = servers.get(i);
      if (i == 0) {
        sb.append(String.format("server    %s;%n", s));
      } else {
        sb.append(String.format("        server    %s;%n", s));
      }
    }
    return sb.toString();
  }
}
