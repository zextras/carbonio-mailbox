package com.zimbra.cs.util.proxyconfgen;

class IPv4OnlyEnablerVar extends IPModeEnablerVar {

  public IPv4OnlyEnablerVar() {
    super("core.ipv4only.enabled", false, "IPv4 Only");
  }

  @Override
  public void update() {
    IPMode ipmode = getZimbraIPMode();
    mValue = ipmode == IPMode.IPV4_ONLY;
  }
}
