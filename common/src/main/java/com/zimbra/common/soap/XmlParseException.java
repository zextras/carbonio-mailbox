// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.soap;

import com.zimbra.common.service.ServiceException;

public class XmlParseException extends ServiceException {

  private static final long serialVersionUID = 2012769501847268691L;

  protected XmlParseException(String message) {
    super(message, PARSE_ERROR, SENDERS_FAULT);
  }

  /** Note: very generic message used to provide data hiding. */
  public static XmlParseException PARSE_ERROR() {
    return new XmlParseException("Document parse failed");
  }
}
