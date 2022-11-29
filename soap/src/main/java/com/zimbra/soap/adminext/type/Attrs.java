// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.adminext.type;

import java.util.List;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Multimap;

import com.zimbra.common.service.ServiceException;

public interface Attrs {
    public Attrs setAttrs(Iterable<Attr> attrs);
    public Attrs setAttrs(Map<String, ? extends Object> attrs)
        throws ServiceException;
    public Attrs addAttr(Attr attr);
    public List<Attr> getAttrs();
    public Multimap<String, String> getAttrsMultimap();
    public Map<String, Object> getAttrsAsOldMultimap();
}
