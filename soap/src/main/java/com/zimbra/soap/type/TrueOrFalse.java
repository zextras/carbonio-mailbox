// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import com.google.common.collect.Maps;
import com.zimbra.common.service.ServiceException;

/**
 * Type that renders "true" for true and "false" for false for BOTH XML and JSON SOAP variants.
 * Compare with {@link ZmBoolean} and {@link ZeroOrOne}
 */
@XmlEnum
public enum TrueOrFalse {
    @XmlEnumValue("true") TRUE("true"),
    @XmlEnumValue("false") FALSE("false");

    private static Map<String, TrueOrFalse> nameToView = Maps.newHashMap();

    static {
        for (TrueOrFalse v : TrueOrFalse.values()) {
            nameToView.put(v.toString(), v);
        }
    }

    private String name;

    TrueOrFalse(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static TrueOrFalse fromString(String name)
    throws ServiceException {
        TrueOrFalse op = nameToView.get(name);
        if (op == null) {
           throw ServiceException.INVALID_REQUEST("unknown value: " + name + ", valid values: " +
                   Arrays.asList(TrueOrFalse.values()), null);
        }
        return op;
    }

    public static TrueOrFalse fromBool(boolean val) {
        if (val) {
            return TRUE;
        } else {
            return FALSE;
        }
    }
    public static boolean toBool(TrueOrFalse val) {
        if (val.equals(TRUE)) {
            return true;
        } else {
            return false;
        }
    }
}
