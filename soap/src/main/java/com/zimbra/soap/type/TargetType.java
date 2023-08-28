// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.zimbra.common.service.ServiceException;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

/** JAXB analog to {@com.zimbra.cs.account.accesscontrol.TargetType} */
@XmlEnum
public enum TargetType {
  // case must match protocol
  account,
  calresource,
  cos,
  dl,
  group,
  domain,
  server,
  ucservice,
  xmppcomponent,
  zimlet,
  config,
  global;

  public static TargetType fromString(String s) throws ServiceException {
    try {
      return TargetType.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST(
          "unknown 'TargetType' key: "
              + s
              + ", valid values: "
              + Arrays.asList(TargetType.values()),
          null);
    }
  }
}
