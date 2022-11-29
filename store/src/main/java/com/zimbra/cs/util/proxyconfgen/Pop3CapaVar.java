package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Pop3CapaVar extends ProxyConfVar {

  public Pop3CapaVar() {
    super(
        "mail.pop3capa",
        null,
        getDefaultPop3Capabilities(),
        ProxyConfValueType.CUSTOM,
        ProxyConfOverride.CUSTOM,
        "POP3 Capability List");
  }

  public static List<String> getDefaultPop3Capabilities() {
    ArrayList<String> pop3Capabilities = new ArrayList<>();
    pop3Capabilities.add("TOP");
    pop3Capabilities.add("USER");
    pop3Capabilities.add("UIDL");
    pop3Capabilities.add("EXPIRE 31 USER");
    return pop3Capabilities;
  }

  @Override
  public void update() {

    ArrayList<String> capabilities = new ArrayList<>();
    String[] capabilityNames =
        serverSource.getMultiAttr(ZAttrProvisioning.A_zimbraReverseProxyPop3EnabledCapability);
    Collections.addAll(capabilities, capabilityNames);
    if (!capabilities.isEmpty()) {
      mValue = capabilities;
    } else {
      mValue = mDefault;
    }
  }

  @Override
  public String format(Object o) {

    @SuppressWarnings("unchecked")
    ArrayList<String> capabilities = (ArrayList<String>) o;
    StringBuilder capa = new StringBuilder();
    for (String c : capabilities) {
      capa.append(" \"");
      capa.append(c);
      capa.append("\"");
    }
    return capa.toString();
  }
}
