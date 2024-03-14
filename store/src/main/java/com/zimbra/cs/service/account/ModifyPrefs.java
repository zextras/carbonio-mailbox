// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.account;

import com.zimbra.common.account.ZAttrProvisioning;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.codec.binary.Hex;

import com.google.common.base.Strings;
import com.zimbra.common.account.ZAttrProvisioning.FeatureAddressVerificationStatus;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.KeyValuePair;
import com.zimbra.common.util.BlobMetaData;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.util.L10nUtil.MsgKey;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author schemers
 */
public class ModifyPrefs extends AccountDocumentHandler {

    public static final String PREF_PREFIX = "zimbraPref";
    public static final String PREF_PREFIX2 = "carbonioPref";

    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);

        OperationContext octxt = getOperationContext(zsc, context);
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account, false);
        if (!canModifyOptions(zsc, account))
            throw ServiceException.PERM_DENIED("can not modify options");

        HashMap<String, Object> prefs = new HashMap<>();
        Map<String, Set<String>> name2uniqueAttrValues = new HashMap<>();
        for (KeyValuePair kvp : request.listKeyValuePairs(AccountConstants.E_PREF, AccountConstants.A_NAME)) {
            final String name = kvp.getKey();
            final String value = kvp.getValue();
            char ch = name.length() > 0 ? name.charAt(0) : 0;
            int offset = ch == '+' || ch == '-' ? 1 : 0;
            if (!name.startsWith(PREF_PREFIX, offset) && !name.startsWith(PREF_PREFIX2, offset)) {
                throw ServiceException.INVALID_REQUEST("pref name must start with " + PREF_PREFIX + " or " + PREF_PREFIX2, null);
            }

            AttributeInfo attrInfo = AttributeManager.getInstance().getAttributeInfo(name.substring(offset));
            if (attrInfo == null) {
                throw ServiceException.INVALID_REQUEST("no such attribute: " + name, null);
            }

            if (attrInfo.isCaseInsensitive()) {
                String valueLowerCase = Strings.nullToEmpty(value).toLowerCase();
                name2uniqueAttrValues.computeIfAbsent(name, k -> new HashSet<>()).add(valueLowerCase);
            }
            StringUtil.addToMultiMap(prefs, name, value);
        }


        if (prefs.containsKey(ZAttrProvisioning.A_zimbraPrefMailForwardingAddress)) {
            if (!account.getBooleanAttr(ZAttrProvisioning.A_zimbraFeatureMailForwardingEnabled, false)) {
                throw ServiceException.PERM_DENIED("forwarding not enabled");
            } else {
                if (account.getBooleanAttr(ZAttrProvisioning.A_zimbraFeatureAddressVerificationEnabled, false)) {
                    /*
                     * forwarding address verification enabled, store the email
                     * ID in 'zimbraFeatureAddressUnderVerification'
                     * until it's verified
                     */
                    String emailIdToVerify = (String) prefs.get(ZAttrProvisioning.A_zimbraPrefMailForwardingAddress);
                    if (!Strings.isNullOrEmpty(emailIdToVerify)) {
                        prefs.remove(ZAttrProvisioning.A_zimbraPrefMailForwardingAddress);
                        prefs.put(ZAttrProvisioning.A_zimbraFeatureAddressUnderVerification, emailIdToVerify);
                        Account authAccount = getAuthenticatedAccount(zsc);
                        sendEmailVerificationLink(authAccount, account, emailIdToVerify, octxt, mbox);
                        prefs.put(ZAttrProvisioning.A_zimbraFeatureAddressVerificationStatus, FeatureAddressVerificationStatus.pending.toString());
                    } else {
                        account.unsetFeatureAddressUnderVerification();
                        account.unsetFeatureAddressVerificationStatus();
                    }
                }
            }
        }
        Provisioning.getInstance().modifyAttrs(account, prefs, true, zsc.getAuthToken());
        return zsc.createElement(AccountConstants.MODIFY_PREFS_RESPONSE);
    }

    public static void sendEmailVerificationLink(Account authAccount, Account ownerAccount,
        String emailIdToVerify, OperationContext octxt, Mailbox mbox) throws ServiceException {
        Locale locale = authAccount.getLocale();
        String ownerAcctDisplayName = ownerAccount.getDisplayName();
        if (ownerAcctDisplayName == null) {
            ownerAcctDisplayName = ownerAccount.getName();
        }
        String subject = L10nUtil.getMessage(MsgKey.verifyEmailSubject, locale,
            ownerAcctDisplayName);
        String charset = authAccount.getAttr(ZAttrProvisioning.A_zimbraPrefMailDefaultCharset,
            MimeConstants.P_CHARSET_UTF8);
        try {
            long expiry = ownerAccount.getFeatureAddressVerificationExpiry();
            Date now = new Date();
            long expiryTime = now.getTime() + expiry;
            DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String gmtDate = format.format(expiryTime);
            String url = generateAddressVerificationURL(ownerAccount, expiryTime, emailIdToVerify);
            if (ZimbraLog.account.isDebugEnabled()) {
                ZimbraLog.account.debug(
                    "Expiry of Forwarding address verification link sent to %s is %s",
                    emailIdToVerify, gmtDate);
                ZimbraLog.account.debug("Forwarding address verification URL sent to %s is %s",
                    emailIdToVerify, url);
            }
            String mimePartText = L10nUtil.getMessage(MsgKey.verifyEmailBodyText, locale,
                ownerAcctDisplayName, url, gmtDate);
            String mimePartHtml = L10nUtil.getMessage(MsgKey.verifyEmailBodyHtml, locale,
                ownerAcctDisplayName, url, gmtDate);
            MimeMultipart mmp = AccountUtil.generateMimeMultipart(mimePartText, mimePartHtml, null);
            MimeMessage mm = AccountUtil.generateMimeMessage(authAccount, ownerAccount, subject,
                charset, null, null, emailIdToVerify, mmp);
            mbox.getMailSender().sendMimeMessage(octxt, mbox, false, mm, null, null, null, null,
                false);
        } catch (MessagingException e) {
            ZimbraLog.account
                .warn("Failed to send verification link to email ID: '" + emailIdToVerify + "'", e);
            throw ServiceException
                .FAILURE("Failed to send verification link to email ID: " + emailIdToVerify, e);
        }
    }

    private static String generateAddressVerificationURL(Account account, long expiry,
        String externalUserEmail) throws ServiceException {
        StringBuilder encodedBuff = new StringBuilder();
        BlobMetaData.encodeMetaData(AccountConstants.P_ACCOUNT_ID, account.getId(), encodedBuff);
        BlobMetaData.encodeMetaData(AccountConstants.P_LINK_EXPIRY, expiry, encodedBuff);
        BlobMetaData.encodeMetaData(AccountConstants.P_EMAIL, externalUserEmail, encodedBuff);
        BlobMetaData.encodeMetaData(AccountConstants.P_ADDRESS_VERIFICATION, true, encodedBuff);
        String data = new String(Hex.encodeHex(encodedBuff.toString().getBytes()));
        return AccountUtil.generateExtUserProvURL(account, data);
    }
}
