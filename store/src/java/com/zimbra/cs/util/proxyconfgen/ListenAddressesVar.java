package com.zimbra.cs.util.proxyconfgen;

import java.util.Set;

/**
 * ListenAddressesVar This class is intended to produce strings that are embedded inside the
 * 'nginx.conf.web.https.default' template It is placed inside the strict server_name enforcing
 * server block.
 */
class ListenAddressesVar extends ProxyConfVar {

  public ListenAddressesVar(Set<String> addresses) {
    super(
        "listen.:addresses",
        null, // this is a fake attribute
        addresses,
        ProxyConfValueType.CUSTOM,
        ProxyConfOverride.CUSTOM,
        "List of ip addresses nginx needs to listen to catch all unknown server names");
  }

  @Override
  public String format(Object o) throws ProxyConfException {
    //noinspection unchecked
    Set<String> addresses = (Set<String>) o;
    if (addresses.isEmpty()) {
      return "${web.strict.servername}";
    }
    StringBuilder sb = new StringBuilder();
    for (String addr : addresses) {
      sb.append(
          String.format(
              "${web.strict.servername}    listen                  %s:${web.https.port}"
                  + " default_server;\n",
              addr));
    }
    sb.setLength(sb.length() - 1); // trim the last newline
    return sb.toString();
  }
}
