// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;

public interface AdminAttrs {
    void setAttrs(Iterable<Attr> attrs);
    void setAttrs(Map<String, ? extends Object> attrs) throws ServiceException;
    void addAttr(Attr attr);
    void addAttr(String n, String value);
    List<Attr> getAttrs();
}
