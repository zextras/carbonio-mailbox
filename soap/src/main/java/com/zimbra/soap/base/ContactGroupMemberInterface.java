// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

public interface ContactGroupMemberInterface {
    void setType(String type);
    void setValue(String value);
    void setContact(ContactInterface contact);
    String getType();
    String getValue();
    ContactInterface getContact();
}
