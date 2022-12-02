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
    public Attrs setAttrs(Iterable<? extends Attr> attrs);
    public Attrs setAttrs(Map<String, ? extends Object> attrs)
        throws ServiceException;
    public Attrs addAttr(Attr attr);
    public List<? extends Attr> getAttrs();
    public Multimap<String, String> getAttrsMultimap();
    public String getFirstMatchingAttr(String name);
    public Map<String, Object> getAttrsAsOldMultimap();
}
