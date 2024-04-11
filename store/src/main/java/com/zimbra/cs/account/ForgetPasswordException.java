// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;

// to be used for reset password feature exceptions
@SuppressWarnings("serial")
public class ForgetPasswordException extends AccountServiceException {
    private enum Codes{
        RECOVERY_EMAIL_SAME_AS_PRIMARY_OR_ALIAS("service.RECOVERY_EMAIL_SAME_AS_PRIMARY_OR_ALIAS"),
        CODE_ALREADY_SENT("service.CODE_ALREADY_SENT"),
        MAX_ATTEMPTS_REACHED("service.MAX_ATTEMPTS_REACHED"),
        MAX_ATTEMPTS_REACHED_SUSPEND_FEATURE("service.MAX_ATTEMPTS_REACHED_SUSPEND_FEATURE"),
        CODE_NOT_FOUND("service.CODE_NOT_FOUND"),
        CODE_MISMATCH("service.CODE_MISMATCH"),
        CODE_EXPIRED("service.CODE_EXPIRED"),
        CONTACT_ADMIN("service.CONTACT_ADMIN"),
        FEATURE_RESET_PASSWORD_SUSPENDED("service.FEATURE_RESET_PASSWORD_SUSPENDED"),
        FEATURE_RESET_PASSWORD_DISABLED("service.FEATURE_RESET_PASSWORD_DISABLED");

        private String code;
        Codes(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    protected ForgetPasswordException(String message, String code, boolean isReceiversFault) {
        this(message, code, isReceiversFault, null);
    }

    protected ForgetPasswordException(String message, String code, boolean isReceiversFault, Throwable cause) {
        super(message, code, isReceiversFault, cause);
    }

    public static ServiceException RECOVERY_EMAIL_SAME_AS_PRIMARY_OR_ALIAS(String message) {
        return new ForgetPasswordException("service exception: " + message, Codes.RECOVERY_EMAIL_SAME_AS_PRIMARY_OR_ALIAS.toString(), SENDERS_FAULT);
    }

    public static ServiceException CODE_ALREADY_SENT(String message) {
        return new ForgetPasswordException("service exception: " + message, Codes.CODE_ALREADY_SENT.toString(), SENDERS_FAULT);
    }

    public static ServiceException MAX_ATTEMPTS_REACHED(String message) {
        return new ForgetPasswordException("service exception: " + message, Codes.MAX_ATTEMPTS_REACHED.toString(), SENDERS_FAULT);
    }

    public static ServiceException MAX_ATTEMPTS_REACHED_SUSPEND_FEATURE(String message) {
        return new ForgetPasswordException("service exception: " + message, Codes.MAX_ATTEMPTS_REACHED_SUSPEND_FEATURE.toString(), SENDERS_FAULT);
    }

    public static ServiceException CODE_NOT_FOUND(String message) {
        return new ForgetPasswordException("service exception: " + message, Codes.CODE_NOT_FOUND.toString(), SENDERS_FAULT);
    }

    public static ServiceException CODE_MISMATCH(String message) {
        return new ForgetPasswordException("service exception: " + message, Codes.CODE_MISMATCH.toString(), SENDERS_FAULT);
    }

    public static ServiceException CODE_EXPIRED(String message) {
        return new ForgetPasswordException("service exception: " + message, Codes.CODE_EXPIRED.toString(), SENDERS_FAULT);
    }

    public static ServiceException CONTACT_ADMIN(String message) {
        return new ForgetPasswordException("service exception: " + message, Codes.CONTACT_ADMIN.toString(), SENDERS_FAULT);
    }

    public static ServiceException FEATURE_RESET_PASSWORD_SUSPENDED(String message) {
        return new ForgetPasswordException("service exception: " + message, Codes.FEATURE_RESET_PASSWORD_SUSPENDED.toString(), SENDERS_FAULT);
    }

    public static ServiceException FEATURE_RESET_PASSWORD_DISABLED(String message) {
        return new ForgetPasswordException("service exception: " + message, Codes.FEATURE_RESET_PASSWORD_DISABLED.toString(), SENDERS_FAULT);
    }
}
