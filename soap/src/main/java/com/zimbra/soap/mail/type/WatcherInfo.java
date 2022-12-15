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

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class WatcherInfo {

    /**
     * @zm-api-field-tag account-id
     * @zm-api-field-description Account ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private String id;

    /**
     * @zm-api-field-tag email-address
     * @zm-api-field-description Email address
     */
    @XmlAttribute(name=MailConstants.A_EMAIL /* email */, required=true)
    private String email;

    /**
     * @zm-api-field-tag display-name
     * @zm-api-field-description Display name
     */
    @XmlAttribute(name=MailConstants.A_NAME /* name */, required=true)
    private String name;

    /**
     * @zm-api-field-description List of items in the user's mailbox currently being watched by other users
     */
    @XmlElement(name=MailConstants.E_ITEM /* item */, required=false)
    private List<IntegerIdAttr> items = Lists.newArrayList();

    private WatcherInfo() {
    }

    private WatcherInfo(String id, String email, String name) {
        setId(id);
        setEmail(email);
        setName(name);
    }

    public static WatcherInfo createForIdEmailAndName(String id, String email, String name) {
        return new WatcherInfo(id, email, name);
    }

    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setItems(Iterable <IntegerIdAttr> items) {
        this.items.clear();
        if (items != null) {
            Iterables.addAll(this.items,items);
        }
    }

    public void addItem(IntegerIdAttr item) {
        this.items.add(item);
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public List<IntegerIdAttr> getItems() {
        return Collections.unmodifiableList(items);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("email", email)
            .add("name", name)
            .add("items", items);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
