// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

public interface ZimletConfigInfo {
    public void setName(String name);
    public void setVersion(String version);
    public void setDescription(String description);
    public void setExtension(String extension);
    public void setTarget(String target);
    public void setLabel(String label);
    public void setGlobal(ZimletGlobalConfigInfo global);
    public void setHost(ZimletHostConfigInfo host);
    public String getName();
    public String getVersion();
    public String getDescription();
    public String getExtension();
    public String getTarget();
    public String getLabel();
    public ZimletGlobalConfigInfo getGlobal();
    public ZimletHostConfigInfo getHost();
}
