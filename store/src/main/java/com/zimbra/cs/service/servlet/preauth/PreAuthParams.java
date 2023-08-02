package com.zimbra.cs.service.servlet.preauth;

import java.util.Set;

/**
 * This enum represents the pre-auth parameters used by this servlet. It provides a set of
 * predefined parameter names that can be used for pre-authentication processing.
 *
 * @author Keshav Bhatt
 * @since 23.9.0
 */
enum PreAuthParams {
  PARAM_PRE_AUTH("preauth"),
  PARAM_AUTHTOKEN("authtoken"),
  PARAM_ACCOUNT("account"),
  PARAM_ADMIN("admin"),
  PARAM_IS_REDIRECT("isredirect"),
  PARAM_BY("by"),
  PARAM_REDIRECT_URL("redirectURL"),
  PARAM_TIMESTAMP("timestamp"),
  PARAM_EXPIRES("expires");

  private static final Set<String> PRE_AUTH_PARAMS =
      Set.of(
          PARAM_PRE_AUTH.getParamName(),
          PARAM_AUTHTOKEN.getParamName(),
          PARAM_ACCOUNT.getParamName(),
          PARAM_ADMIN.getParamName(),
          PARAM_IS_REDIRECT.getParamName(),
          PARAM_BY.getParamName(),
          PARAM_REDIRECT_URL.getParamName(),
          PARAM_TIMESTAMP.getParamName(),
          PARAM_EXPIRES.getParamName());

  private final String paramName;

  PreAuthParams(String paramName) {
    this.paramName = paramName;
  }

  static Set<String> getPreAuthParams() {
    return PRE_AUTH_PARAMS;
  }

  public String getParamName() {
    return paramName;
  }
}
