// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.service.ServiceException;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum DataSourceType {
  // case must match protocol
  pop3,
  imap,
  caldav,
  contacts,
  yab,
  rss,
  cal,
  gal,
  xsync,
  tagmap,
  unknown;

  public static DataSourceType fromString(String s) throws ServiceException {
    try {
      return DataSourceType.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST(
          "invalid type: " + s + ", valid values: " + Arrays.asList(DataSourceType.values()), e);
    }
  }
}
