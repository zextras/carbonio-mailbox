// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.ServerSelector;
import com.zimbra.soap.mail.type.FilterRule;
import com.zimbra.soap.type.AccountSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Modify Filter rules
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MODIFY_OUTGOING_FILTER_RULES_REQUEST)
public final class ModifyOutgoingFilterRulesRequest extends ModifyFilterRulesRequest {
    public ModifyOutgoingFilterRulesRequest() {
        super();
    }

    public ModifyOutgoingFilterRulesRequest(AccountSelector account, List<FilterRule> filterRules, String type) {
        super(account, filterRules, type);
    }

    public ModifyOutgoingFilterRulesRequest(DomainSelector domain, List<FilterRule> filterRules, String type) {
        super(domain, filterRules, type);
    }

    public ModifyOutgoingFilterRulesRequest(CosSelector cos, List<FilterRule> filterRules, String type) {
        super(cos, filterRules, type);
    }

    public ModifyOutgoingFilterRulesRequest(ServerSelector server, List<FilterRule> filterRules, String type) {
        super(server, filterRules, type);
    }
}
