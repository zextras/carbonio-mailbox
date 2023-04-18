// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class FreeBusyUserInfo {

    /**
     * @zm-api-field-tag account-email
     * @zm-api-field-description "id" is always account email; it is not zimbraId as the attribute name may suggest
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-description Free/Busy slots
     */
    @XmlElements({
        @XmlElement(name=MailConstants.E_FREEBUSY_FREE /* f */, type=FreeBusyFREEslot.class),
        @XmlElement(name=MailConstants.E_FREEBUSY_BUSY /* b */, type=FreeBusyBUSYslot.class),
        @XmlElement(name=MailConstants.E_FREEBUSY_BUSY_TENTATIVE /* t */, type=FreeBusyBUSYTENTATIVEslot.class),
        @XmlElement(name=MailConstants.E_FREEBUSY_BUSY_UNAVAILABLE /* u */, type=FreeBusyBUSYUNAVAILABLEslot.class),
        @XmlElement(name=MailConstants.E_FREEBUSY_NODATA /* n */, type=FreeBusyNODATAslot.class)
    })
    private List<FreeBusySlot> elements = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private FreeBusyUserInfo() {
        this((String) null);
    }

    public FreeBusyUserInfo(String id) {
        this.id = id;
    }

    public void setElements(Iterable <FreeBusySlot> elements) {
        this.elements.clear();
        if (elements != null) {
            Iterables.addAll(this.elements,elements);
        }
    }

    public FreeBusyUserInfo addElement(FreeBusySlot element) {
        this.elements.add(element);
        return this;
    }

    public String getId() { return id; }
    public List<FreeBusySlot> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("elements", elements);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
