// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

public interface ZimletHostConfigInfo {

    void setName(String name);
    void setZimletProperties(Iterable<ZimletProperty> properties);
    void addZimletProperty(ZimletProperty property);

    String getName();
    List<ZimletProperty> getZimletProperties();
}
