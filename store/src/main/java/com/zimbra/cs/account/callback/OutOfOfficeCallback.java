// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import java.util.Map;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zextras.mailbox.mail.OutOfOfficeRepository;
import com.zimbra.cs.db.DbOutOfOffice;
import java.time.Instant;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Notification;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;

public class OutOfOfficeCallback extends AttributeCallback {

    private final OutOfOfficeRepository outOfOfficeRepository = new DbOutOfOffice();

    @Override 
    public void preModify(CallbackContext context, String attrName, Object value,
            Map attrsToModify, Entry entry) {
    }

    @Override 
    public void postModify(CallbackContext context, String attrName, Entry entry) {
        
        if (context.isDoneAndSetIfNot(OutOfOfficeCallback.class)) {
            return;
        }
        
        if (!context.isCreate()) {
            ZimbraLog.misc.info("need to reset vacation info");
            if (entry instanceof Account) {
                handleOutOfOffice((Account)entry);
            }
        }
    }

    private void handleOutOfOffice(Account account) {
        try {
            Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

            outOfOfficeRepository.clear(mbox.getId());
            ZimbraLog.misc.info("reset vacation info");
            long interval = account.getTimeInterval(Provisioning.A_zimbraPrefOutOfOfficeCacheDuration, Notification.DEFAULT_OUT_OF_OFFICE_CACHE_DURATION_MILLIS);
            outOfOfficeRepository.prune(Instant.now().minusMillis(interval));
        } catch (ServiceException e) {
            ZimbraLog.misc.warn("error handling out-of-office", e);
        }
    }
}
