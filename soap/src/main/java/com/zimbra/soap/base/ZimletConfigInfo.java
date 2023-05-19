// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

public interface ZimletConfigInfo {
    void setName(String name);
    void setVersion(String version);
    void setDescription(String description);
    void setExtension(String extension);
    void setTarget(String target);
    void setLabel(String label);
    void setGlobal(ZimletGlobalConfigInfo global);
    void setHost(ZimletHostConfigInfo host);
    String getName();
    String getVersion();
    String getDescription();
    String getExtension();
    String getTarget();
    String getLabel();
    ZimletGlobalConfigInfo getGlobal();
    ZimletHostConfigInfo getHost();
}
