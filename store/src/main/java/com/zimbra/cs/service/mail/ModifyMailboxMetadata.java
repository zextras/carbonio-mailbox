// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.soap.MailConstants;
import org.dom4j.QName;

public class ModifyMailboxMetadata extends SetMailboxMetadata {

  @Override
  boolean isModify() {
    return true;
  }

  QName getResponseName() {
    return MailConstants.MODIFY_MAILBOX_METADATA_RESPONSE;
  }
}
