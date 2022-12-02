// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.adminext.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminExtConstants;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Task list for bulk IMAP Imports
 */
@XmlRootElement(name=AdminExtConstants.E_GET_BULK_IMAP_IMPORT_TASKLIST_REQUEST)
public class GetBulkIMAPImportTaskListRequest {
}
