// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

@XmlEnum
public enum Channel {
    @XmlEnumValue("email") EMAIL("email");

    private static Map<String, Channel> nameToChannel = Maps.newHashMap();
    static {
        for (Channel v : Channel.values()) {
            nameToChannel.put(v.toString(), v);
        }
    }

    private String name;

    private Channel(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Channel fromString(String name) {
        return nameToChannel.get(Strings.nullToEmpty(name));
    }
}