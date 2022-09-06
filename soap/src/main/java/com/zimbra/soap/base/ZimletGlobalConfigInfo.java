// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

public interface ZimletGlobalConfigInfo {

  public void setZimletProperties(Iterable<ZimletProperty> properties);

  public void addZimletProperty(ZimletProperty property);

  public List<ZimletProperty> getZimletProperties();
}
