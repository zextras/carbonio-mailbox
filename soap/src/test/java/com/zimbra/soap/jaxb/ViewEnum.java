// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import com.google.common.collect.Maps;

/** Test enum for JAXB testing.  Values differ from representation in Xml/JSON. Has 1 empty string representation */
@XmlEnum
public enum ViewEnum {
    @XmlEnumValue("") UNKNOWN (""),
    @XmlEnumValue("search folder") SEARCH_FOLDER ("search folder"),
    @XmlEnumValue("tag") TAG ("tag"),
    @XmlEnumValue("conversation") CONVERSATION ("conversation"),
    @XmlEnumValue("message") MESSAGE ("message"),
    @XmlEnumValue("contact") CONTACT ("contact"),
    @XmlEnumValue("document") DOCUMENT ("document"),
    @XmlEnumValue("appointment") APPOINTMENT ("appointment"),
    @XmlEnumValue("virtual conversation") VIRTUAL_CONVERSATION ("virtual conversation"),
    @XmlEnumValue("remote folder") REMOTE_FOLDER ("remote folder"),
    @XmlEnumValue("wiki") WIKI ("wiki"),
    @XmlEnumValue("task") TASK ("task"),
    @XmlEnumValue("chat") CHAT ("chat");

    private static Map<String, ViewEnum> nameToView = Maps.newHashMap();

    static {
        for (ViewEnum v : ViewEnum.values()) {
            nameToView.put(v.toString(), v);
        }
    }

    private String name;

    private ViewEnum(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ViewEnum fromString(String name) {
        if (name == null) {
            name = "";
        }
        return nameToView.get(name);
    }
}
