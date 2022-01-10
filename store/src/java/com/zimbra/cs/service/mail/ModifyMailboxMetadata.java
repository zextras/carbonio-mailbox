// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import org.dom4j.QName;

import com.zimbra.common.soap.MailConstants;


public class ModifyMailboxMetadata extends SetMailboxMetadata {

    @Override
    boolean isModify() {
        return true;
    }
    
    QName getResponseName() {
        return MailConstants.MODIFY_MAILBOX_METADATA_RESPONSE;
    }
}
