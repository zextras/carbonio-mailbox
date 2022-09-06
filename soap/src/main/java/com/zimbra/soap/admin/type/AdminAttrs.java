// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.service.ServiceException;
import java.util.List;
import java.util.Map;

public interface AdminAttrs {
  public void setAttrs(Iterable<Attr> attrs);

  public void setAttrs(Map<String, ? extends Object> attrs) throws ServiceException;

  public void addAttr(Attr attr);

  public void addAttr(String n, String value);

  public List<Attr> getAttrs();
}
