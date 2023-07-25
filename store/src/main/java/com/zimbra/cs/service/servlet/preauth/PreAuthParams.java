package com.zimbra.cs.service.servlet.preauth;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * This enum represents the pre-auth parameters used by this servlet. It provides a set of
 * predefined parameter names that can be used for pre-authentication processing.
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

  private static final HashSet<String> PRE_AUTH_PARAMS = new HashSet<>();

  static {
    for (PreAuthParams param : EnumSet.allOf(PreAuthParams.class)) {
      PRE_AUTH_PARAMS.add(param.getParamName());
    }
  }

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
