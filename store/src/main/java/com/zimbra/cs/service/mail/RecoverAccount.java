// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.account.ForgetPasswordEnums.CodeConstants;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.account.ZAttrProvisioning.FeatureResetPasswordStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ChannelProvider;
import com.zimbra.cs.account.ForgetPasswordException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.util.JWEUtil;
import com.zimbra.cs.service.util.ResetPasswordUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.RecoverAccountRequest;
import com.zimbra.soap.mail.message.RecoverAccountResponse;
import com.zimbra.soap.mail.type.RecoverAccountOperation;
import com.zimbra.soap.type.Channel;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang.RandomStringUtils;

public final class RecoverAccount extends MailDocumentHandler {
  public static final String LOG_OPERATION = "RecoverAccount:";

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    RecoverAccountRequest req = zsc.elementToJaxb(request);
    req.validateRecoverAccountRequest();
    RecoverAccountOperation op = req.getOp();
    String email = req.getEmail();
    Account user = Provisioning.getInstance().getAccountByName(email);
    if (user == null) {
      ZimbraLog.passwordreset.debug("%s Account not found for %s", LOG_OPERATION, email);
      throw ForgetPasswordException.CONTACT_ADMIN(
          "Something went wrong. Please contact your administrator.");
    }

    Element response = proxyIfNecessary(request, context, user);
    if (response == null) {
      user.refreshUserCredentials();
      ResetPasswordUtil.isResetPasswordEnabledAndValidRecoveryAccount(user);

      Channel channel = req.getChannel();
      ChannelProvider provider = ChannelProvider.getProviderForChannel(channel);
      String recoveryAccount = provider.getRecoveryAccount(user);
      RecoverAccountResponse resp = new RecoverAccountResponse();
      Map<String, String> recoveryCode;
      switch (op) {
        case GET_RECOVERY_ACCOUNT:
          ResetPasswordUtil.validateVerifiedPasswordRecoveryAccount(user);
          recoveryAccount = StringUtil.maskEmail(recoveryAccount);
          ZimbraLog.passwordreset.debug("%s Recovery account: %s", LOG_OPERATION, recoveryAccount);
          resp.setRecoveryAccount(recoveryAccount);
          break;
        case SEND_RECOVERY_CODE:
          recoveryCode = generatePasswordRecoveryCode(user, recoveryAccount, zsc, resp);
          provider.sendAndStoreResetPasswordRecoveryCode(zsc, user, recoveryCode);
          break;
        default:
          throw ServiceException.INVALID_REQUEST("Invalid op received", null);
      }
      response = zsc.jaxbToElement(resp);
    }
    return response;
  }

  private static Map<String, String> generatePasswordRecoveryCode(
      Account account, String recoveryAccount, ZimbraSoapContext zsc, RecoverAccountResponse resp)
      throws ServiceException {
    Map<String, String> recoveryCodeMap;
    int maxAttempts = account.getPasswordRecoveryMaxAttempts();
    try {
      String encoded = account.getResetPasswordRecoveryCode();
      recoveryCodeMap = JWEUtil.getDecodedJWE(encoded);
    } catch (Exception e) {
      ZimbraLog.account.warn("Error while fetching Password Recovery Code: ", e);
      throw ServiceException.FAILURE("Error while fetching Password Recovery Code.", e);
    }
    if (Objects.isNull(recoveryCodeMap)) {
      recoveryCodeMap = new HashMap<>();
    }
    checkVerifiedRecoveryAccAndSetResendCount(account, recoveryCodeMap, maxAttempts, zsc, resp);
    fetchAndFormRecoveryCodeParams(account, recoveryCodeMap, recoveryAccount, zsc);
    ZimbraLog.account.debug("Recovery Code Map formed: %s", recoveryCodeMap.toString());
    return recoveryCodeMap;
  }

  public static void checkVerifiedRecoveryAccAndSetResendCount(
      Account account,
      Map<String, String> recoveryCodeMap,
      int maxAttempts,
      ZimbraSoapContext zsc,
      RecoverAccountResponse resp)
      throws ServiceException {
    ResetPasswordUtil.validateVerifiedPasswordRecoveryAccount(account);
    int resendCount = 0;
    try {
      if (recoveryCodeMap != null
          && !recoveryCodeMap.isEmpty()
          && !StringUtil.isNullOrEmpty(
              recoveryCodeMap.get(CodeConstants.RESEND_COUNT.toString()))) {
        resendCount = Integer.parseInt(recoveryCodeMap.get(CodeConstants.RESEND_COUNT.toString()));
        if (resendCount >= maxAttempts) {
          account.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.suspended);
          long suspension = account.getFeatureResetPasswordSuspensionTime();
          Date now = new Date();
          long suspensionTime = now.getTime() + suspension;
          recoveryCodeMap.put(
              CodeConstants.SUSPENSION_TIME.toString(), String.valueOf(suspensionTime));
          HashMap<String, Object> prefs = new HashMap<>();
          prefs.put(
              ZAttrProvisioning.A_zimbraResetPasswordRecoveryCode, JWEUtil.getJWE(recoveryCodeMap));
          Provisioning.getInstance().modifyAttrs(account, prefs, true, zsc.getAuthToken());
          throw ForgetPasswordException.MAX_ATTEMPTS_REACHED_SUSPEND_FEATURE(
              "Max re-send attempts reached, feature is suspended.");
        } else {
          resendCount++;
          recoveryCodeMap.put(CodeConstants.RESEND_COUNT.toString(), String.valueOf(resendCount));
        }
      } else {
        ZimbraLog.passwordreset.debug(
            "%s Recovery code not found for %s, creating new one",
            LOG_OPERATION, account.getName());
        recoveryCodeMap.put(CodeConstants.RESEND_COUNT.toString(), String.valueOf(resendCount));
      }
      resp.setRecoveryAttemptsLeft(maxAttempts - resendCount);
    } catch (ServiceException e) {
      ZimbraLog.account.warn("Error while setting Password Recovery Resend Count : ", e);
      throw e;
    }
  }

  public static void fetchAndFormRecoveryCodeParams(
      Account account,
      Map<String, String> recoveryCodeMap,
      String recoveryAccount,
      ZimbraSoapContext zsc)
      throws ServiceException {
    ZonedDateTime currentDate = ZonedDateTime.now(ZoneId.systemDefault());
    try {
      if (recoveryCodeMap != null
          && !recoveryCodeMap.isEmpty()
          && !StringUtil.isNullOrEmpty(recoveryCodeMap.get(CodeConstants.EXPIRY_TIME.toString()))
          && !StringUtil.isNullOrEmpty(recoveryCodeMap.get(CodeConstants.CODE.toString()))) {
        ZonedDateTime storedDate =
            Instant.ofEpochMilli(
                    Long.valueOf(recoveryCodeMap.get(CodeConstants.EXPIRY_TIME.toString())))
                .atZone(ZoneId.systemDefault());
        if (ChronoUnit.MILLIS.between(currentDate, storedDate) <= 0L) {
          ZimbraLog.passwordreset.debug(
              "%s Recovery code expired, so generating new one.", LOG_OPERATION);
          recoveryCodeMap.put(
              CodeConstants.CODE.toString(), RandomStringUtils.random(8, true, true));
          // add expiry duration in current time.
          currentDate =
              currentDate.plus(account.getResetPasswordRecoveryCodeExpiry(), ChronoUnit.MILLIS);
          Long val = currentDate.toInstant().toEpochMilli();
          recoveryCodeMap.put(CodeConstants.EXPIRY_TIME.toString(), String.valueOf(val));
        } else {
          ZimbraLog.passwordreset.debug(
              "%s Recovery code has not expired yet, so using the same code.", LOG_OPERATION);
        }
        recoveryCodeMap.put(CodeConstants.EMAIL.toString(), recoveryAccount);
      } else {
        ZimbraLog.passwordreset.debug(
            "%s Recovery code not found for %s, creating new one",
            LOG_OPERATION, account.getName());
        recoveryCodeMap.put(CodeConstants.EMAIL.toString(), recoveryAccount);
        recoveryCodeMap.put(CodeConstants.CODE.toString(), RandomStringUtils.random(8, true, true));
        // add expiry duration in current time.
        currentDate =
            currentDate.plus(account.getResetPasswordRecoveryCodeExpiry(), ChronoUnit.MILLIS);
        Long val = currentDate.toInstant().toEpochMilli();
        recoveryCodeMap.put(CodeConstants.EXPIRY_TIME.toString(), String.valueOf(val));
      }
    } catch (Exception e) {
      ZimbraLog.account.warn("Error while setting Password Recovery Params : ", e);
      throw ServiceException.FAILURE("Error while setting Password Recovery Params.", e);
    }
  }

  private Element proxyIfNecessary(Element request, Map<String, Object> context, Account acct)
      throws ServiceException {
    Provisioning.Reasons reasons = new Provisioning.Reasons();
    if (acct != null && !Provisioning.getInstance().onLocalServer(acct, reasons)) {
      ZimbraLog.passwordreset.debug(
          "Proxying RecoverAccount request: requestedAccountId=%s", acct.getId());
      return proxyRequest(request, context, acct.getId());
    }
    return null;
  }

  // no auth required for this request, so return false for both needsAuth and needsAdminAuth
  @Override
  public boolean needsAuth(Map<String, Object> context) {
    return false;
  }

  @Override
  public boolean needsAdminAuth(Map<String, Object> context) {
    return false;
  }
}
