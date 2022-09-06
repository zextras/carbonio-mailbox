// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.service.ServiceException;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum XMPPComponentBy {
  // case must match protocol
  id,
  name,
  serviceHostname;

  public static XMPPComponentBy fromString(String s) throws ServiceException {
    try {
      return XMPPComponentBy.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST(
          "invalid by key: " + s + ", valid values: " + Arrays.asList(values()), null);
    }
  }
}
