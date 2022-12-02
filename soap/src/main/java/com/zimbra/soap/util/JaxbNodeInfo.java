// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.util;

public interface JaxbNodeInfo {
    public String getName();
    public String getNamespace();
    public boolean isRequired();
    public boolean isMultiElement();
}
