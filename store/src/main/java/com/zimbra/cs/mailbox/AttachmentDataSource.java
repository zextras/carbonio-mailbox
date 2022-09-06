// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mime.Mime;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;

public class AttachmentDataSource implements DataSource {

  private Contact mContact;
  private String mPartName;

  public AttachmentDataSource(Contact contact, String partName) {
    if (contact == null) {
      throw new NullPointerException("contact cannot be null");
    }
    if (partName == null) {
      throw new NullPointerException("partName cannot be null");
    }
    mContact = contact;
    mPartName = partName;
  }

  public String getContentType() {
    String contentType = null;
    MimePart mp = null;
    try {
      mp = getMimePart();
      if (mp != null) {
        contentType = mp.getContentType();
      }
    } catch (Exception e) {
      ZimbraLog.mailbox.error(
          "Unable to determine content type for contact %d.", mContact.getId(), e);
    }
    return contentType;
  }

  private MimePart getMimePart() throws MessagingException, ServiceException {
    MimeMessage msg = mContact.getMimeMessage(false);
    MimePart mp = null;
    try {
      mp = Mime.getMimePart(msg, mPartName);
    } catch (IOException e) {
      throw ServiceException.FAILURE(
          "Unable to look up part " + mPartName + " for contact " + mContact.getId(), null);
    }

    if (mp == null) {
      ZimbraLog.mailbox.warn("Unable to find part %s for contact %d.", mPartName, mContact.getId());
    }
    return mp;
  }

  public InputStream getInputStream() throws IOException {
    try {
      return getMimePart().getInputStream();
    } catch (Exception e) {
      ZimbraLog.mailbox.error(
          "Unable to get stream to part %s for contact %d.", mPartName, mContact.getId());
      throw new IOException(e.toString());
    }
  }

  public String getName() {
    MimePart mp = null;
    String name = null;
    try {
      mp = getMimePart();
      if (mp != null) {
        name = mp.getFileName();
      }
    } catch (Exception e) {
      ZimbraLog.mailbox.error(
          "Unable to determine the filename for contact %d, part %s.",
          mContact.getId(), mPartName, e);
    }
    return name;
  }

  public OutputStream getOutputStream() {
    throw new UnsupportedOperationException();
  }
}
