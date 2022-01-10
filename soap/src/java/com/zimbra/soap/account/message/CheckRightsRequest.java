// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.account.type.CheckRightsTargetSpec;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Check if the authed user has the specified right(s) on a target.
 */
@XmlRootElement(name=AccountConstants.E_CHECK_RIGHTS_REQUEST)
public class CheckRightsRequest {
    /**
     * @zm-api-field-description The targets
     */
    @XmlElement(name=MailConstants.E_TARGET /* target */, required=true)
    private List<CheckRightsTargetSpec> targets = Lists.newArrayList();

    public CheckRightsRequest() {
        this(null);
    }

    public CheckRightsRequest(Iterable<CheckRightsTargetSpec> targets) {
        if (targets != null) {
            setTargets(targets);
        }
    }

    public void setTargets(Iterable<CheckRightsTargetSpec> targets) {

        this.targets = Lists.newArrayList(targets);
    }

    public void addTarget(CheckRightsTargetSpec target) {
        targets.add(target);
    }

    public List<CheckRightsTargetSpec> getTargets() {
        return targets;
    }
}
