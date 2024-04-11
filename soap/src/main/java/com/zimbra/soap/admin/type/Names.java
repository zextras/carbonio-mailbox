// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class Names {

    private static Splitter COMMA_SPLITTER =
        Splitter.on(",").trimResults().omitEmptyStrings();

    private static Joiner COMMA_JOINER = Joiner.on(",");

    /**
     * @zm-api-field-tag comma-sep-names
     * @zm-api-field-description Comma separated list of names
     */
    @XmlAttribute(name=AdminConstants.A_NAME, required=true)
    private final String names;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private Names() {
        this((String) null);
    }

    public Names(String names) {
        this.names = names;
    }

    public Names(List<String> names) {
        if (names == null)
            this.names = null;
        else
            this.names = COMMA_JOINER.join(names);

    }

    public String getNames() { return names; }
    public Iterable<String> getListOfNames() {
        return COMMA_SPLITTER.split(Strings.nullToEmpty(names));
    }
}
