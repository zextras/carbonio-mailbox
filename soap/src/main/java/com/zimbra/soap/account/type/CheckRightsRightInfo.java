// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class CheckRightsRightInfo {

    /**
     * @zm-api-field-tag right-allow
     * @zm-api-field-description Flags whether the authed user has the right on the target
     * <ul>
     * <li> <b>1 (true)</b> - the authed user has the right on the target
     * <li> <b>0 (false)</b> - the authed user does not have the right on the target
     * </ul>
     */
    @XmlAttribute(name=AccountConstants.A_ALLOW /* allow */, required=true)
    private ZmBoolean allow;

    /**
     * @zm-api-field-description right-name
     * @zm-api-field-description Name of right
     */
    @XmlValue
    String right;

    public CheckRightsRightInfo() {
        this(null, false);
    }

    public CheckRightsRightInfo(String right, boolean allow) {
        setRight(right);
        setAllow(allow);
    }

    public void setRight(String right) {
        this.right = right;
    }

    public void setAllow(boolean allow) {
        this.allow = ZmBoolean.fromBool(allow);
    }

    public String getRight() {
        return right;
    }

    public boolean getAllow() {
        return ZmBoolean.toBool(allow);
    }
}
