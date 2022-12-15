// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.HeaderConstants;

@XmlAccessorType(XmlAccessType.NONE)
public final class AuthTokenControl {

    /**
     * @zm-api-field-tag voidOnExpired
     * @zm-api-field-description if set to true, expired authToken in the header will be ignored
     */
    @XmlAttribute(name=HeaderConstants.A_VOID_ON_EXPIRED/* voidOnExpired */, required=false)
    private ZmBoolean voidOnExpired;

    public AuthTokenControl() {
        voidOnExpired = ZmBoolean.FALSE;
    }

    public AuthTokenControl(Boolean voidExpired) {
        voidOnExpired = ZmBoolean.fromBool(voidExpired);
    }

    public void setVoidOnExpired(Boolean voidExpired) {
        voidOnExpired = ZmBoolean.fromBool(voidExpired);
    }

    public Boolean isVoidOnExpired() {
        return ZmBoolean.toBool(voidOnExpired);
    }
}
