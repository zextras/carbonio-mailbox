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
 * @zm-api-command-description Purge bulk IMAP import tasks
 */
@XmlRootElement(name=AdminExtConstants.E_PURGE_BULK_IMAP_IMPORT_TASKS_REQUEST)
public class PurgeBulkIMAPImportTasksRequest {
}
