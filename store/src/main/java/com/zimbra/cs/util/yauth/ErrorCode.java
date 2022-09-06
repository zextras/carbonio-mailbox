// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.yauth;

import java.util.HashMap;
import java.util.Map;

/** Yahoo authentication error codes. */
public enum ErrorCode {
  INVALID_PASSWORD("InvalidPassword", "Invalid Password"),
  UNDER_AGE_USER("UnderAgeUser", "User is under age"),
  CAPTCHA_REQUIRED("CaptchaRequired", "Captcha is required"),
  USER_MUST_LOGIN("UserMustLogin", "Browser login required"),
  LOGIN_DOESNT_EXIST("LoginDoesntExist", "User login does not exist"),
  LOCKED_USER("LockedUser", "User is in locked state"),
  TEMP_ERROR("TempError", "Temporary Error"),
  HTTPS_REQUIRED("HttpsRequired", "This webservice call requires HTTPS"),
  TOKEN_REQUIRED("TokenRequired", "Invalid (missing) token"),
  INVALID_APP_ID("InvalidAppId", "Invalid (missing) appid"),
  INVALID_TOKEN("InvalidToken", "Invalid (missing) token"),
  INVALID_LOGIN_OR_PASSWORD("InvalidLoginOrPassword", "Invalid (missing) login or password"),
  INVALID_CAPTCHA_WORD_LEN("InvalidCaptchaWordLen", "Invalid (missing) captchaword"),
  INVALID_CAPTCHA_DATA("InvalidCaptchaData", "Invalid (missing) captchadata"),
  INVALID_CAPTCHA("InvalidCaptcha", "Validation of captcha failed"),
  DEACTIVATED_APP_ID("DeactivatedAppId", "Application id disabled"),
  GENERIC_ERROR("GenericError", "Unspecified error");

  private static final Map<String, ErrorCode> byName;

  static {
    byName = new HashMap<String, ErrorCode>();
    for (ErrorCode error : values()) {
      byName.put(error.name, error);
    }
  }

  public static ErrorCode get(String name) {
    return byName.get(name);
  }

  private final String name;
  private final String description;

  private ErrorCode(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
