// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

public interface ZimletServerExtensionInterface {
    void setHasKeyword(String hasKeyword);
    void setExtensionClass(String extensionClass);
    void setRegex(String regex);

    String getHasKeyword();
    String getExtensionClass();
    String getRegex();
}
