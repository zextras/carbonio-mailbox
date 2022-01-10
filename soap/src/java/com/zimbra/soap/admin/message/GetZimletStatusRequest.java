// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;

// Note: soap-admin.txt implies there is an attrs attribute but the handler doesn't appear to get it.
/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get status for Zimlets
 * <br />
 * priority is listed in the global list &lt;zimlets> ... &lt;/zimlets> only.  This is because the priority value is
 * relative to other Zimlets in the list.  The same Zimlet may show different priority number depending on what
 * other Zimlets priorities are.  the same Zimlet will show priority 0 if all by itself, or priority 3 if there are
 * three other Zimlets with higher priority.
 */
@XmlRootElement(name=AdminConstants.E_GET_ZIMLET_STATUS_REQUEST)
public class GetZimletStatusRequest {
}
