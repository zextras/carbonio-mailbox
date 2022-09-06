// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.zimbra.common.service.ServiceException;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

/** relates to target for rights */
@XmlEnum
public enum TargetBy {
  // case must match protocol
  id,
  name;

  public static TargetBy fromString(String s) throws ServiceException {
    try {
      return TargetBy.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST(
          "unknown 'By' key: " + s + ", valid values: " + Arrays.asList(TargetBy.values()), null);
    }
  }
}
