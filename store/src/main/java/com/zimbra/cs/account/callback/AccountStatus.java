// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.events.services.mailbox.UserStatusChanged;
import com.zextras.mailbox.messagebroker.MessageBrokerFactory;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import java.util.Map;
import java.util.Set;

public class AccountStatus extends AttributeCallback {

    /**
     * disable mail delivery if account status is changed to closed
     * reset lockout attributes if account status is changed to active
     */
    @SuppressWarnings("unchecked")
    @Override
    public void preModify(CallbackContext context, String attrName, Object value,
            Map attrsToModify, Entry entry)
    throws ServiceException {

        String status;

        SingleValueMod mod = singleValueMod(attrName, value);
        if (mod.unsetting())
            throw ServiceException.INVALID_REQUEST(Provisioning.A_zimbraAccountStatus+" is a required attribute", null);
        else
            status = mod.value();

        if (status.equals(Provisioning.ACCOUNT_STATUS_CLOSED) || status.equals(Provisioning.ACCOUNT_STATUS_PENDING)) {
            attrsToModify.put(Provisioning.A_zimbraMailStatus, Provisioning.MAIL_STATUS_DISABLED);
        } else if (attrsToModify.get(Provisioning.A_zimbraMailStatus) == null) {
            // the request is not also changing zimbraMailStatus, set = zimbraMailStatus to enabled
            attrsToModify.put(Provisioning.A_zimbraMailStatus, Provisioning.MAIL_STATUS_ENABLED);
        }

        if ((entry instanceof Account) && (status.equals(Provisioning.ACCOUNT_STATUS_ACTIVE))) {
            if (entry.getAttr(Provisioning.A_zimbraPasswordLockoutFailureTime, null) != null)
                attrsToModify.put(Provisioning.A_zimbraPasswordLockoutFailureTime, "");
            if (entry.getAttr(Provisioning.A_zimbraPasswordLockoutLockedTime, null) != null)
                attrsToModify.put(Provisioning.A_zimbraPasswordLockoutLockedTime, "");
        }
    }

    @Override
    public void postModify(CallbackContext context, String attrName, Entry entry) {
        if (context.isDoneAndSetIfNot(AccountStatus.class)) {
            return;
        }

        if (!context.isCreate()) {
            if (entry instanceof Account) {
                try {
                    publishStatusChangedEvent((Account)entry);
                    handleAccountStatusClosed((Account)entry);
                } catch (Exception e) {
                    ZimbraLog.account.warn("Exception thrown on account status changed callback", e);
                }
            }
        }
    }

    private void publishStatusChangedEvent(Account account) {
        Provisioning prov = Provisioning.getInstance();
        String status = account.getAccountStatus(prov);
        String userId = account.getId();

        try {
            MessageBrokerClient messageBrokerClient = MessageBrokerFactory.getMessageBrokerClientInstance();
            boolean result = messageBrokerClient.publish(
                new UserStatusChanged(userId, status.toUpperCase()));
            if (result) {
                ZimbraLog.messageBroker.info("Published status changed event for user: " + userId);
            } else {
                ZimbraLog.messageBroker.error(
                    "Failed to publish status changed event for user: " + userId);
            }
        } catch (Exception e) {
            ZimbraLog.messageBroker.error(
                "Exception while publishing status changed event for user: " + userId, e);
        }

    }

    private void handleAccountStatusClosed(Account account)  throws ServiceException {
        Provisioning prov = Provisioning.getInstance();
        String status = account.getAccountStatus(prov);

        if (status.equals(Provisioning.ACCOUNT_STATUS_CLOSED)) {
            ZimbraLog.misc.info("removing account address and all its aliases from all distribution lists");

            String[] addrToRemove = new String[] {account.getName()};

            Set<String> dlIds = prov.getDistributionLists(account);
            for (String dlId : dlIds) {
                DistributionList dl = prov.get(Key.DistributionListBy.id, dlId);
                if (dl != null) {
                    try {
                        // will remove all members that are aliases of the account too
                        prov.removeMembers(dl, addrToRemove);
                    } catch (ServiceException se) {
                        if (AccountServiceException.NO_SUCH_MEMBER.equals(se.getCode())) {
                            ZimbraLog.misc.debug("Member not found in dlist; skipping", se);
                        } else {
                            throw se;
                        }
                    }
                }
            }
        }
    }


}
