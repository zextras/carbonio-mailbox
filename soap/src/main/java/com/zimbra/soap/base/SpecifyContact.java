// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import com.zimbra.soap.mail.type.NewContactAttr;
import com.zimbra.soap.mail.type.NewContactGroupMember;

public interface SpecifyContact <T extends NewContactAttr, M extends NewContactGroupMember> {
    void setId(Integer id);
    void setTagNames(String tagNames);
    void setAttrs(Iterable<T> attrs);
    void addAttr(T attr);
    T addAttrWithName(String name);
    T addAttrWithNameAndValue(String name, String value);
    void setContactGroupMembers(Iterable<M> contactGroupMembers);
    void addContactGroupMember(M contactGroupMember);
    M addContactGroupMemberWithTypeAndValue(String type, String value);
    Integer getId();
    String getTagNames();
    List<T> getAttrs();
    List<M> getContactGroupMembers();
}
