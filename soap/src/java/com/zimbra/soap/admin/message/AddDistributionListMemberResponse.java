// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;

@XmlRootElement(name=AdminConstants.E_ADD_DISTRIBUTION_LIST_MEMBER_RESPONSE)
public class AddDistributionListMemberResponse {

    public AddDistributionListMemberResponse() {
    }
}
