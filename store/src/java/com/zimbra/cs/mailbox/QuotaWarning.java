// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.MailDateFormat;

import com.zimbra.common.util.Constants;
import com.zimbra.common.util.EmailUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapDateUtil;
import com.zimbra.cs.lmtpserver.LmtpCallback;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.util.AccountUtil;

/**
 * LMTP callback that sends the user a warning if he is getting
 * close to exceeding his mailbox quota.
 *
 * @author bburtin
 */
public class QuotaWarning implements LmtpCallback {

    private static final QuotaWarning sInstance = new QuotaWarning();

    private QuotaWarning() {
    }

    public static QuotaWarning getInstance() {
        return sInstance;
    }

    @Override
    public void afterDelivery(Account account, Mailbox mbox, String envelopeSender,
                              String recipientEmail, Message newMessage){
        try {
            int warnPercent = account.getIntAttr(Provisioning.A_zimbraQuotaWarnPercent, 90);
            long quota = AccountUtil.getEffectiveQuota(account);
            long warnInterval = account.getTimeInterval(Provisioning.A_zimbraQuotaWarnInterval, Constants.MILLIS_PER_DAY);
            String template = account.getAttr(Provisioning.A_zimbraQuotaWarnMessage, null);

            Date lastWarnTime = account.getGeneralizedTimeAttr(Provisioning.A_zimbraQuotaLastWarnTime, null);
            Date now = new Date();

            ZimbraLog.lmtp.debug("Checking quota warning: mbox size=%d, quota=%d, lastWarnTime=%s, warnInterval=%d, warnPercent=%d",
                mbox.getSize(), quota, lastWarnTime, warnInterval, warnPercent);

            // Bail out if there's no quota, we haven't hit the warning threshold, or a warning
            // has already been sent.
            if (quota == 0 || warnPercent == 0) {
                return;
            }
            if (mbox.getSize() * 100 / quota < warnPercent) {
                return;
            }
            if (lastWarnTime != null && now.getTime() - lastWarnTime.getTime() < warnInterval) {
                return;
            }
            if (template == null) {
                ZimbraLog.lmtp.warn("%s not specified.  Unable to send quota warning message.",
                    Provisioning.A_zimbraQuotaWarnMessage);
            }

            // Send the quota warning
            ZimbraLog.lmtp.info("Sending quota warning: mbox size=%d, quota=%d, lastWarnTime=%s, warnInterval=%dms, warnPercent=%d",
                mbox.getSize(), quota, lastWarnTime, warnInterval, warnPercent);
            String address = account.getAttr(Provisioning.A_zimbraMailDeliveryAddress);
            String domain = EmailUtil.getLocalPartAndDomain(address)[1];

            Map<String, Object> vars = new HashMap<String, Object>();
            vars.put("RECIPIENT_NAME", account.getAttr(Provisioning.A_displayName));
            vars.put("RECIPIENT_DOMAIN", domain);
            vars.put("RECIPIENT_ADDRESS", address);
            vars.put("DATE", new MailDateFormat().format(new Date()));
            vars.put("MBOX_SIZE_MB", String.format("%.2f", mbox.getSize() / 1024.0 / 1024.0));
            vars.put("QUOTA_MB", String.format("%.2f", quota / 1024.0 / 1024.0));
            vars.put("WARN_PERCENT", warnPercent);
            vars.put("NEWLINE", "\r\n");

            String msgBody = StringUtil.fillTemplate(template, vars);
            ParsedMessage pm = new ParsedMessage(msgBody.getBytes(), now.getTime(), false);
            DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX).setFlags(Flag.BITMASK_UNREAD | Flag.BITMASK_HIGH_PRIORITY);
            mbox.addMessage(null, pm, dopt, null);

            // Update last sent date
            Map<String, String> attrs = new HashMap<String, String>();
            attrs.put(Provisioning.A_zimbraQuotaLastWarnTime, LdapDateUtil.toGeneralizedTime(now));
            Provisioning.getInstance().modifyAttrs(account, attrs);
        } catch (Exception e) {
            ZimbraLog.lmtp.warn("Unable to send quota warning message", e);
        }
    }

    @Override
    public void forwardWithoutDelivery(Account account, Mailbox mbox, String envelopeSender, String recipientEmail, ParsedMessage pm) {
        // no delivery, so no need to check anything
    }
}
