// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ExportAndDeleteMailboxSpec {

    /**
     * @zm-api-field-tag id
     * @zm-api-field-description ID
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private final int id;

    /**
     * @zm-api-field-description Items
     */
    @XmlElement(name=AdminConstants.E_ITEM, required=false)
    private List<ExportAndDeleteItemSpec> items = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ExportAndDeleteMailboxSpec() {
        this(-1);
    }

    public ExportAndDeleteMailboxSpec(int id) {
        this.id = id;
    }

    public void setItems(Iterable <ExportAndDeleteItemSpec> items) {
        this.items.clear();
        if (items != null) {
            Iterables.addAll(this.items,items);
        }
    }

    public ExportAndDeleteMailboxSpec addItem(ExportAndDeleteItemSpec item) {
        this.items.add(item);
        return this;
    }

    public int getId() { return id; }
    public List<ExportAndDeleteItemSpec> getItems() {
        return Collections.unmodifiableList(items);
    }
}
