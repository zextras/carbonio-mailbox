// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import com.zimbra.soap.mail.type.NewContactAttr;
import com.zimbra.soap.mail.type.NewContactGroupMember;

public interface SpecifyContact <T extends NewContactAttr, M extends NewContactGroupMember> {
    public void setId(Integer id);
    public void setTagNames(String tagNames);
    public void setAttrs(Iterable <T> attrs);
    public void addAttr(T attr);
    public T addAttrWithName(String name);
    public T addAttrWithNameAndValue(String name, String value);
    public void setContactGroupMembers(Iterable <M> contactGroupMembers);
    public void addContactGroupMember(M contactGroupMember);
    public M addContactGroupMemberWithTypeAndValue(String type, String value);
    public Integer getId();
    public String getTagNames();
    public List<T> getAttrs();
    public List<M> getContactGroupMembers();
}
