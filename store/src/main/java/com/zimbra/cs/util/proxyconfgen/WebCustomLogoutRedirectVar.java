package com.zimbra.cs.util.proxyconfgen;

/**
 * Represents a custom logout redirect configuration variable for Carbonio web proxy configuration.
 *
 * <p>This class extends the {@link ProxyConfVar} and provides functionality to define a custom
 * logout redirect rule.
 *
 * <p>The custom logout redirect configuration variable is used when a user logs out from the web
 * application, and the web proxy needs to handle the logout redirection. Upon calling update, if
 * the custom URL is provided, the web proxy will return a 200 response (this is intentional as the
 * redirect in this case will be handled by UI) with no redirection. Otherwise, it will return a 307
 * Temporary Redirect response with the default URL provided during initialization, usually we use
 * {@link ProxyConfGen#DEFAULT_WEB_LOGIN_PATH} as default value.
 *
 * @author Keshav Bhatt
 * @see ProxyConfVar
 * @since 23.9.0
 */
class WebCustomLogoutRedirectVar extends ProxyConfVar {

  private final String mCustomUrl;

  public WebCustomLogoutRedirectVar(
      String keyword, String attribute, Object defaultValue, String description, String customUrl) {
    super(
        keyword,
        attribute,
        defaultValue,
        ProxyConfValueType.STRING,
        ProxyConfOverride.CUSTOM,
        description);
    this.mCustomUrl = customUrl;
  }

  @Override
  public void update() {
    mValue =
        ProxyConfUtil.isEmptyString(mCustomUrl)
            ? String.format("return 307 %s", mDefault)
            : "return 200";
  }
}
