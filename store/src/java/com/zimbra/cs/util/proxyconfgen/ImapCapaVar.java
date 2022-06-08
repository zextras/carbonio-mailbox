package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import java.util.ArrayList;
import java.util.Collections;

class ImapCapaVar extends ProxyConfVar {

  public ImapCapaVar() {
    super(
        "mail.imapcapa",
        null,
        getDefaultImapCapabilities(),
        ProxyConfValueType.CUSTOM,
        ProxyConfOverride.CUSTOM,
        "IMAP Capability List");
  }

  static ArrayList<String> getDefaultImapCapabilities() {
    ArrayList<String> imapCapabilities = new ArrayList<>();
    imapCapabilities.add("IMAP4rev1");
    imapCapabilities.add("ID");
    imapCapabilities.add("LITERAL+");
    imapCapabilities.add("SASL-IR");
    imapCapabilities.add("IDLE");
    imapCapabilities.add("NAMESPACE");
    return imapCapabilities;
  }

  @Override
  public void update() {

    ArrayList<String> capabilities = new ArrayList<>();
    String[] capabilityNames =
        serverSource.getMultiAttr(ZAttrProvisioning.A_zimbraReverseProxyImapEnabledCapability);
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
