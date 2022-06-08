package com.zimbra.cs.util.proxyconfgen;

class IPv6OnlyEnablerVar extends IPModeEnablerVar {

  public IPv6OnlyEnablerVar() {
    super("core.ipv6only.enabled", false, "IPv6 Only");
  }

  @Override
  public void update() {
    IPMode ipmode = getZimbraIPMode();
    mValue = ipmode == IPMode.IPV6_ONLY;
  }
}
