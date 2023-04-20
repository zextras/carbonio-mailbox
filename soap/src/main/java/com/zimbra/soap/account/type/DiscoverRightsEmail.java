// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AccountConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class DiscoverRightsEmail {

    /**
     * @zm-api-field-tag email-address
     * @zm-api-field-description Email address
     */
    @XmlAttribute(name=AccountConstants.A_ADDR /* addr */, required=true)
    private String addr;

    public DiscoverRightsEmail() {
        this(null);
    }

    public DiscoverRightsEmail(String addr) {
        setAddr(addr);
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getAddr() {
        return addr;
    }
}
