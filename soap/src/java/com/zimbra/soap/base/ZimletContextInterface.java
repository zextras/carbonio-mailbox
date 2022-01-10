// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

public interface ZimletContextInterface {
    public void setZimletBaseUrl(String zimletBaseUrl);
    public void setZimletPriority(Integer zimletPriority);
    public void setZimletPresence(String zimletPresence);

    public String getZimletBaseUrl();
    public Integer getZimletPriority();
    public String getZimletPresence();
}
