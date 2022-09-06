// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.zclient.ZClientException;
import java.util.Arrays;

public enum ZimbraSortBy {
  dateDesc,
  dateAsc,
  subjDesc,
  subjAsc,
  nameDesc,
  nameAsc,
  durDesc,
  durAsc,
  none,
  sizeAsc,
  sizeDesc,
  attachAsc,
  attachDesc,
  flagAsc,
  flagDesc,
  priorityAsc,
  priorityDesc,
  taskDueAsc,
  taskDueDesc,
  taskStatusAsc,
  taskStatusDesc,
  taskPercCompletedAsc,
  taskPercCompletedDesc,
  rcptAsc,
  rcptDesc,
  idAsc,
  idDesc,
  readAsc,
  readDesc;

  public static ZimbraSortBy fromString(String s) throws ServiceException {
    try {
      return ZimbraSortBy.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ZClientException.CLIENT_ERROR(
          String.format(
              "unknown 'sortBy':'%s' - valid values: ", s, Arrays.asList(ZimbraSortBy.values())),
          null);
    }
  }
}
