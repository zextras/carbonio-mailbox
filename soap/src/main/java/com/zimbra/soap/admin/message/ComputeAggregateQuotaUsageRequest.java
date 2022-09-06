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
 * @zm-api-command-description Computes the aggregate quota usage for all domains in the system.
 *     <br>
 *     The request handler issues <b>GetAggregateQuotaUsageOnServerRequest</b> to all mailbox
 *     servers and computes the aggregate quota used by each domain. <br>
 *     The request handler updates the <b>zimbraAggregateQuotaLastUsage</b> domain attribute and
 *     sends out warning messages for each domain having quota usage greater than a defined
 *     percentage threshold.
 */
@XmlRootElement(name = AdminConstants.E_COMPUTE_AGGR_QUOTA_USAGE_REQUEST)
public class ComputeAggregateQuotaUsageRequest {

  public ComputeAggregateQuotaUsageRequest() {}
}
