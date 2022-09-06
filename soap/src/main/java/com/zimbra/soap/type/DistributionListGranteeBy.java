// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.zimbra.common.service.ServiceException;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum DistributionListGranteeBy {
  // case must match protocol
  id,
  name;

  public static DistributionListGranteeBy fromString(String s) throws ServiceException {
    try {
      return DistributionListGranteeBy.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST(
          "unknown 'By' key: "
              + s
              + ", valid values: "
              + Arrays.asList(DistributionListGranteeBy.values()),
          null);
    }
  }
}
