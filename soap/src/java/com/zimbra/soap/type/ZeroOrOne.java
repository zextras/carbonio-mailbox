// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum ZeroOrOne {
    @XmlEnumValue("0") ZERO,
    @XmlEnumValue("1") ONE;

    public static ZeroOrOne fromBool(boolean val) {
        if (val) {
            return ONE;
        } else {
            return ZERO;
        }
    }
    public static boolean toBool(ZeroOrOne val) {
        if (val.equals(ONE)) {
            return true;
        } else {
            return false;
        }
    }
}
