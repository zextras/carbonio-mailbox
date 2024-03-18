// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.HABGroup;
/**
 * @author zimbra
 *
 */

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-description Get the groups in a HAB org unit.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_GET_HAB_RESPONSE)
public class GetHABResponse {
    
    /**
     * @zm-api-field-description List of HabGroups under the root group
     */
    @XmlElement(name=AccountConstants.E_HAB_GROUPS, required=false)
    private List<HABGroup> habGroupList = new ArrayList<>();
    
    public GetHABResponse() {
        
    }
}
