// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.AttrsImpl;
import com.zimbra.soap.account.type.DistributionListAction;
import com.zimbra.soap.type.DistributionListSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Perform an action on a Distribution List
 * <br />
 * Notes:
 * <ul>
 * <li> Authorized account must be one of the list owners
 * <li> For owners/rights, only grants on the group itself will be modified, grants on domain and globalgrant (from
 *      which the right can be inherited) will not be touched.  Only admins can modify grants on domains and
 *      globalgrant, owners of groups can only modify grants on the group entry.
 * </ul>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_DISTRIBUTION_LIST_ACTION_REQUEST)
public class DistributionListActionRequest extends AttrsImpl {

    /**
     * @zm-api-field-description Identifies the distribution list to act upon
     */
    @XmlElement(name=AccountConstants.E_DL, required=true)
    private DistributionListSelector dl;

    /**
     * @zm-api-field-description Specifies the action to perform
     */
    @XmlElement(name=AccountConstants.E_ACTION, required=true)
    private final DistributionListAction action;

    public DistributionListActionRequest() {
        this((DistributionListSelector) null, (DistributionListAction) null);
    }

    public DistributionListActionRequest(DistributionListSelector dl, 
            DistributionListAction action) {
        this.setDl(dl);
        this.action = action;
    }

    public void setDl(DistributionListSelector dl) { this.dl = dl; }

    public DistributionListSelector getDl() { return dl; }
    public DistributionListAction getAction() { return action; }

}
