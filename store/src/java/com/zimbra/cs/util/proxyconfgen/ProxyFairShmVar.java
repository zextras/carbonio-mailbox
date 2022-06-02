package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;

class ProxyFairShmVar extends ProxyConfVar {

  public ProxyFairShmVar() {
    super(
        "upstream.fair.shm.size",
        ZAttrProvisioning.A_zimbraReverseProxyUpstreamFairShmSize,
        "",
        ProxyConfValueType.CUSTOM,
        ProxyConfOverride.CONFIG,
        "Controls the 'upstream_fair_shm_size' configuration in the proxy configuration file:"
            + " nginx.conf.web.template.");
  }

  @Override
  public void update() {
    String setting =
        serverSource.getAttr(ZAttrProvisioning.A_zimbraReverseProxyUpstreamFairShmSize, "32");

    try {
      if (Integer.parseInt(setting) < 32) {
        setting = "32";
      }
    } catch (NumberFormatException e) {
      mLog.info(
          "Value provided in 'zimbraReverseProxyUpstreamFairShmSize': "
              + setting
              + " is invalid. Falling back to default value of 32.");
      setting = "32";
    }

    mValue = setting;
  }

  @Override
  public String format(Object o) {
    return "upstream_fair_shm_size " + mValue + "k;";
  }
}
