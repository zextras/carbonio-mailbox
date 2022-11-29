// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.filter.RuleManager;
import org.dom4j.QName;

/**
 */
public class ApplyOutgoingFilterRules extends ApplyFilterRules {

    @Override
    protected String getRules(Account account) {
        return RuleManager.getOutgoingRules(account);
    }

    @Override
    protected QName getResponseElementName() {
        return MailConstants.APPLY_OUTGOING_FILTER_RULES_RESPONSE;
    }
}
