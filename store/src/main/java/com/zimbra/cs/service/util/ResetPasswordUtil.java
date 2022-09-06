// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.util;

import com.zimbra.common.account.ForgetPasswordEnums.CodeConstants;
import com.zimbra.common.account.ZAttrProvisioning.FeatureResetPasswordStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ForgetPasswordException;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class ResetPasswordUtil {

  public static void validateFeatureResetPasswordStatus(Account account) throws ServiceException {
    if (account == null) {
      throw ServiceException.INVALID_REQUEST("account is null.", null);
    }
    switch (account.getFeatureResetPasswordStatus()) {
      case enabled:
        break;
      case suspended:
        Map<String, String> codeMap = JWEUtil.getDecodedJWE(account.getResetPasswordRecoveryCode());
        if (codeMap != null
            && StringUtils.isNotEmpty(codeMap.get(CodeConstants.SUSPENSION_TIME.toString()))) {
          long suspensionTime =
              Long.parseLong(codeMap.get(CodeConstants.SUSPENSION_TIME.toString()));
          Date now = new Date();
          if (suspensionTime < now.getTime()) {
            account.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
            account.unsetResetPasswordRecoveryCode();
          } else {
            throw ForgetPasswordException.FEATURE_RESET_PASSWORD_SUSPENDED(
                "Password reset feature is suspended.");
          }
        } else {
          account.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
        }
        break;
      case disabled:
      default:
        throw ForgetPasswordException.FEATURE_RESET_PASSWORD_DISABLED(
            "Password reset feature is disabled.");
    }
  }

  public static void checkValidRecoveryAccount(Account account) throws ServiceException {
    if (account == null) {
      throw ServiceException.INVALID_REQUEST("account is null.", null);
    }
    if (StringUtil.isNullOrEmpty(account.getPrefPasswordRecoveryAddress())) {
      ZimbraLog.passwordreset.warn(
          "ResetPassword : Recovery Account is not found for %s", account.getName());
      throw ForgetPasswordException.CONTACT_ADMIN(
          "Recovery Account is not found. Please contact your administrator.");
    }
  }

  public static void validateVerifiedPasswordRecoveryAccount(Account account)
      throws ServiceException {
    if (account == null) {
      throw ServiceException.INVALID_REQUEST("account is null.", null);
    }
    if (account.getPrefPasswordRecoveryAddressStatus() == null
        || !account.getPrefPasswordRecoveryAddressStatus().isVerified()) {
      ZimbraLog.passwordreset.warn(
          "Verified recovery email is not found for %s", account.getName());
      throw ForgetPasswordException.CONTACT_ADMIN(
          "Recovery Account is not verified. Please contact your administrator.");
    }
  }

  public static void isResetPasswordEnabledAndValidRecoveryAccount(Account account)
      throws ServiceException {
    validateFeatureResetPasswordStatus(account);
    checkValidRecoveryAccount(account);
  }
}
