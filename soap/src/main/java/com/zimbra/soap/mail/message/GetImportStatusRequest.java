// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Returns current import status for all data sources.  Status values for a data source
 * are reinitialized when either (a) another import process is started or (b) when the server is restarted.  If
 * import has not run yet, the success and error attributes are not specified in the response.
 */
@XmlRootElement(name=MailConstants.E_GET_IMPORT_STATUS_REQUEST)
public class GetImportStatusRequest {
}
