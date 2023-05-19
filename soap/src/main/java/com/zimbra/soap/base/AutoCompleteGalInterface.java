// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

public interface AutoCompleteGalInterface {

    void setMore(Boolean more);
    void setTokenizeKey(Boolean tokenizeKey);
    void setPagingSupported(Boolean pagingSupported);
    void setContactInterfaces(Iterable<ContactInterface> contacts);
    void addContactInterface(ContactInterface contact);
    Boolean getMore();
    Boolean getTokenizeKey();
    Boolean getPagingSupported();
    List<ContactInterface> getContactInterfaces();
}
