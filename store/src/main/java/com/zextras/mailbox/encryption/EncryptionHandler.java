// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.encryption;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;

import javax.mail.internet.MimeMessage;

public interface EncryptionHandler {

    MimeMessage decryptMessage(Mailbox mailbox, MimeMessage mime, int itemId) throws ServiceException;
}
