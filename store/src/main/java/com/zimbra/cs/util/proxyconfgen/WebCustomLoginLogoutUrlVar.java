package com.zimbra.cs.util.proxyconfgen;

/**
 * This class represents a custom login/logout URL variable for a web proxy configuration. This
 * class extends the ProxyConfVar class and provides functionality to update the value based on the
 * custom URL or the default value.
 *
 * @author Keshav Bhatt
 */
class WebCustomLoginLogoutUrlVar extends ProxyConfVar {

  private final String mCustomUrl;

  public WebCustomLoginLogoutUrlVar(
      String keyword,
      String attribute,
      Object defaultValue,
      ProxyConfValueType valueType,
      ProxyConfOverride overrideType,
      String description,
      String customUrl) {
    super(keyword, attribute, defaultValue, valueType, overrideType, description);
    this.mCustomUrl = customUrl;
    update();
  }

  @Override
  public void update() {
    mValue = ProxyConfUtil.isEmptyString(mCustomUrl) ? mDefault : mCustomUrl;
  }
}
