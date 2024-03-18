// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.HashMap;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.JWEUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.type.Channel;

public abstract class ChannelProvider {
    private static Map<String, ChannelProvider> providers = new HashMap<>();

    static {
        try {
            registerChannelProvider(Channel.EMAIL.toString(), new EmailChannel());
        } catch (ServiceException e) {
            ZimbraLog.passwordreset.warn("Channel registration failed for %s", Channel.EMAIL.toString());
        }
    }

    public static void registerChannelProvider(String channel, ChannelProvider provider) throws ServiceException {
        if (StringUtil.isNullOrEmpty(channel) || provider == null) {
            ZimbraLog.passwordreset.error("Channel or channel provider is invalid");
            throw ServiceException.FAILURE("Channel and channel provider must be provided", null);
        }
        if (providers.get(channel) != null) {
            ZimbraLog.passwordreset.warn("Channel provider %s already registered for %s, so not adding new provider",
                    providers.get(channel).getClass().getName(), channel);
        } else {
            providers.put(channel, provider);
            ZimbraLog.passwordreset.info("Channel provider %s registered for %s", providers.get(channel).getClass().getName(),
                    channel);
        }
    }

    public static ChannelProvider getProviderForChannel(String channel) {
        if (StringUtil.isNullOrEmpty(channel)) {
            return null;
        }
        return providers.get(channel);
    }

    public static ChannelProvider getProviderForChannel(Channel channel) {
        if (channel == null) {
            return null;
        }
        return providers.get(channel.toString());
    }

    // RecoverAccount API methods
    public Map<String, String> getResetPasswordRecoveryCodeMap(Account account) throws ServiceException {
        String encoded = account.getResetPasswordRecoveryCode();
        return JWEUtil.getDecodedJWE(encoded);
    }
    public abstract String getRecoveryAccount(Account account) throws ServiceException;
    public abstract void sendAndStoreResetPasswordRecoveryCode(ZimbraSoapContext zsc, Account account,
            Map<String, String> recoveryCodeMap) throws ServiceException;

    // SetRecoveryAccount API methods
    public Map<String, String> getSetRecoveryAccountCodeMap(Account account) throws ServiceException {
        Map<String, String> recoveryCodeMap = null;
        String encoded = account.getRecoveryAccountVerificationData();
        recoveryCodeMap = JWEUtil.getDecodedJWE(encoded);
        return recoveryCodeMap;
    }
    public abstract void validateSetRecoveryAccountCode(String recoveryAccountVerificationCode, Account account,
            Mailbox mbox, ZimbraSoapContext zsc) throws ServiceException;
    public abstract void sendAndStoreSetRecoveryAccountCode(Account account, Mailbox mbox,
            Map<String, String> recoveryCodeMap, ZimbraSoapContext zsc, OperationContext octxt,
            HashMap<String, Object> prefs) throws ServiceException;
}
