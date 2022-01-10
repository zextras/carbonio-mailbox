// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

public interface AutoCompleteGalInterface {

    public void setMore(Boolean more);
    public void setTokenizeKey(Boolean tokenizeKey);
    public void setPagingSupported(Boolean pagingSupported);
    public void setContactInterfaces(Iterable <ContactInterface> contacts);
    public void addContactInterface(ContactInterface contact);
    public Boolean getMore();
    public Boolean getTokenizeKey();
    public Boolean getPagingSupported();
    public List<ContactInterface> getContactInterfaces();
}
