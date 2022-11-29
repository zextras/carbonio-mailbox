// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;

public class DistributionListRightSpec {

    /**
     * @zm-api-field-description Right
     */
    @XmlAttribute(name=AccountConstants.A_RIGHT, required=true)
    private final String right;

    /**
     * @zm-api-field-description Grantees
     */
    @XmlElement(name=AccountConstants.E_GRANTEE, required=false)
    protected List<DistributionListGranteeSelector> grantees;

    public DistributionListRightSpec() {
        this(null);
    }

    public DistributionListRightSpec(String right) {
        this.right = right;
    }

    public String getRight() {
        return right;
    }

    public void addGrantee(DistributionListGranteeSelector grantee) {
        if (grantees == null) {
            grantees = Lists.newArrayList();
        }
        grantees.add(grantee);
    }

    public void setGrantees(List<DistributionListGranteeSelector> grantees) {
        this.grantees = null;
        if (grantees != null) {
            this.grantees = Lists.newArrayList();
            Iterables.addAll(this.grantees, grantees);
        }
    }

    public List<DistributionListGranteeSelector> getGrantees() {
        return grantees;
    }
}
