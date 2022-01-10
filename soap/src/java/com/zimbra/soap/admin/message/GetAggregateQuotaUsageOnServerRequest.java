// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Gets the aggregate quota usage for all domains on the server.
 */
@XmlRootElement(name=AdminConstants.E_GET_AGGR_QUOTA_USAGE_ON_SERVER_REQUEST)
public class GetAggregateQuotaUsageOnServerRequest {

    public GetAggregateQuotaUsageOnServerRequest() {
    }
}
