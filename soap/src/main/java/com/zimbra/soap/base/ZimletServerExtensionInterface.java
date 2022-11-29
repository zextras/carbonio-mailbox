// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

public interface ZimletServerExtensionInterface {
    public void setHasKeyword(String hasKeyword);
    public void setExtensionClass(String extensionClass);
    public void setRegex(String regex);

    public String getHasKeyword();
    public String getExtensionClass();
    public String getRegex();
}
