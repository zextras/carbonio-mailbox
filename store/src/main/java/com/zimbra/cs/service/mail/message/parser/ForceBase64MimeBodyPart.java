// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail.message.parser;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.zmime.ZMimeBodyPart;
import com.zimbra.cs.mime.Mime;
import javax.mail.MessagingException;

/**
 * Overrides the default transfer encoding and sets the encoding of all non-message attachments to
 * base64, so that we preserve line endings of text attachments (bugs 45858 and 53405).
 */
class ForceBase64MimeBodyPart extends ZMimeBodyPart {

  public ForceBase64MimeBodyPart() {
  }

  @Override
  protected void updateHeaders() throws MessagingException {
    super.updateHeaders();
    if (LC.text_attachments_base64.booleanValue()) {
      String ct = Mime.getContentType(this);
      if (!(ct.startsWith(MimeConstants.CT_MESSAGE_PREFIX) || ct.startsWith(
          MimeConstants.CT_MULTIPART_PREFIX))) {
        setHeader("Content-Transfer-Encoding", "base64");
      }
    }
  }
}
