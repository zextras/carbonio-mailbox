// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface EmailInfoInterface {
    EmailInfoInterface create(String address, String display,
        String personal, String addressType);
    void setGroup(Boolean group);
    void setCanExpandGroupMembers(Boolean canExpandGroupMembers);
    String getAddress();
    String getDisplay();
    String getPersonal();
    String getAddressType();
    Boolean getGroup();
    Boolean getCanExpandGroupMembers();
}
