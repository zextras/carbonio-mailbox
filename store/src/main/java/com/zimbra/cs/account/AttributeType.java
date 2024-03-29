// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.HashMap;
import java.util.Map;

public enum AttributeType {
    TYPE_BOOLEAN("boolean"),
    TYPE_BINARY("binary"),
    TYPE_CERTIFICATE("certificate"),
    TYPE_DURATION("duration"),
    TYPE_GENTIME("gentime"),
    TYPE_EMAIL("email"),
    TYPE_EMAILP("emailp"),
    TYPE_CS_EMAILP("cs_emailp"),
    TYPE_ENUM("enum"),
    TYPE_ID("id"),
    TYPE_INTEGER("integer"),
    TYPE_PORT("port"),
    TYPE_PHONE("phone"),
    TYPE_STRING("string"),
    TYPE_ASTRING("astring"),
    TYPE_OSTRING("ostring"),
    TYPE_CSTRING("cstring"),
    TYPE_REGEX("regex"),
    TYPE_LONG("long");

    private static class TM {
        static Map<String, AttributeType> sTypeMap = new HashMap<>();
    }
    
    private String mName; 
    AttributeType(String name) {
        mName = name;
        TM.sTypeMap.put(name, this);
    }
    
    public static AttributeType getType(String name) {
        return TM.sTypeMap.get(name);
    }
    
    String getName() {
        return mName;
    }
}
