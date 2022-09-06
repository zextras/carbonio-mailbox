// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.zimbra.common.service.ServiceException;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum GranteeType {
  // case must match protocol - keep in sync with com.zimbra.cs.account.accesscontrol.GranteeType
  usr,
  grp,
  egp,
  all,
  dom,
  edom,
  gst,
  key,
  pub,
  email;

  public static GranteeType fromString(String s) throws ServiceException {
    try {
      return GranteeType.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST(
          "Invalid grantee type: " + s + ", valid values: " + Arrays.asList(GranteeType.values()),
          null);
    }
  }
}
