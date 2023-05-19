// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import com.zimbra.common.service.ServiceException;

public interface Attrs {
    Attrs setAttrs(Iterable<? extends Attr> attrs);
    Attrs setAttrs(Map<String, ? extends Object> attrs)
        throws ServiceException;
    Attrs addAttr(Attr attr);
    List<? extends Attr> getAttrs();
    Multimap<String, String> getAttrsMultimap();
    String getFirstMatchingAttr(String name);
    Map<String, Object> getAttrsAsOldMultimap();
}
