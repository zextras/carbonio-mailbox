package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.localconfig.LC;

class SaslHostFromIPVar extends ProxyConfVar {

  public SaslHostFromIPVar() {
    super(
        "mail.sasl_host_from_ip",
        "krb5_service_principal_from_interface_address",
        false,
        ProxyConfValueType.BOOLEAN,
        ProxyConfOverride.LOCALCONFIG,
        "Whether to use incoming interface IP address to determine service "
            + "principal name (if true, IP address is reverse mapped to DNS name, "
            + "else host name of proxy is used)");
  }

  @Override
  public void update() {
    if (LC.krb5_service_principal_from_interface_address.booleanValue()) {
      mValue = true;
    } else {
      mValue = false;
    }
  }
}
