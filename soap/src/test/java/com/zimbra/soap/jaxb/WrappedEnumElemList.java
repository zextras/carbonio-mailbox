// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/** Test JAXB class with an XmlElement list of enums */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="wrapped-enum-elem-list")
public class WrappedEnumElemList {
    @XmlElementWrapper(name="wrapper", required=false)
    @XmlElement(name="enum-entry", required=false)
    private List<ViewEnum> entries = Lists.newArrayList();

    public WrappedEnumElemList() { }

    public void setEntries(Iterable <ViewEnum> entries) {
        this.entries.clear();
        if (entries != null) {
            Iterables.addAll(this.entries,entries);
        }
    }

    public void addEntry(ViewEnum entry) { this.entries.add(entry); }
    public List<ViewEnum> getEntries() { return entries; }
}
