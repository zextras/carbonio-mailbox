package com.zimbra.cs.util.proxyconfgen;

/**
 * This class represents a custom login URL variable for a web proxy configuration. This class
 * extends the ProxyConfVar class and provides functionality to update the value based on the custom
 * URL or the default value.
 *
 * @author Keshav Bhatt
 * @see ProxyConfVar
 * @since 23.9.0
 */
class WebCustomLoginUrlVar extends ProxyConfVar {

  private final String mCustomUrl;

  public WebCustomLoginUrlVar(
      String keyword, String attribute, Object defaultValue, String description, String customUrl) {
    super(
        keyword,
        attribute,
        defaultValue,
        ProxyConfValueType.STRING,
        ProxyConfOverride.CUSTOM,
        description);
    this.mCustomUrl = customUrl;
    update();
  }

  @Override
  public void update() {
    mValue = ProxyConfUtil.isEmptyString(mCustomUrl) ? mDefault : mCustomUrl;
  }
}
