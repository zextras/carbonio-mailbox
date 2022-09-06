// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.zimbra.common.service.ServiceException;
import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum DistributionListSubscribeOp {
  subscribe,
  unsubscribe;

  public static DistributionListSubscribeOp fromString(String str) throws ServiceException {
    try {
      return DistributionListSubscribeOp.valueOf(str);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST("invalid op: " + str, e);
    }
  }
}
