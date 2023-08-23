// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

import com.zimbra.cs.service.UserServlet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;

/** Represents a {@link com.zimbra.cs.service.UserServlet} request */
public class UserServletRequest {

  final String authType;
  final String itemId;
  final String itemPart;

  private UserServletRequest(String authType, String itemId, String itemPart) {
    this.authType = authType;
    this.itemId = itemId;
    this.itemPart = itemPart;
  }

  public static UserServletRequest buildRequest(String authType, String itemId, String itemPart) {
    return new UserServletRequest(authType, itemId, itemPart);
  }

  /**
   * Returns a query representation
   *
   * @return query string
   */
  @Override
  public String toString() {
    final List<String> queryList = new ArrayList<>();
    if (!(StringUtils.isEmpty(authType))) {
      queryList.add(UserServlet.QP_AUTH + "=" + authType);
    }
    if (!(StringUtils.isEmpty(itemId))) {
      queryList.add(UserServlet.QP_ID + "=" + itemId);
    }
    if (!(StringUtils.isEmpty(authType))) {
      queryList.add(UserServlet.QP_PART + "=" + itemPart);
    }
    return String.join("&", queryList);
  }

  @Override
  public boolean equals(Object o) {
    return Objects.equals(this.toString(), o.toString());
  }
}
