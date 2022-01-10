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
import com.zimbra.soap.account.type.CheckRightsTargetInfo;

@XmlRootElement(name=AccountConstants.E_CHECK_RIGHTS_RESPONSE)
public class CheckRightsResponse {

    /**
     * @zm-api-field-description Rights information for targets
     */
    @XmlElement(name=MailConstants.E_TARGET /* target */, required=true)
    private List<CheckRightsTargetInfo> targets = Lists.newArrayList();

    public CheckRightsResponse() {
        this(null);
    }

    public CheckRightsResponse(List<CheckRightsTargetInfo> targets) {
        if (targets != null) {
            setTargets(targets);
        }
    }

    public void setTargets(List<CheckRightsTargetInfo> targets) {
        this.targets = Lists.newArrayList(targets);
    }

    public void addTarget(CheckRightsTargetInfo target) {
        targets.add(target);
    }

    public List<CheckRightsTargetInfo> getTargets() {
        return targets;
    }
}
