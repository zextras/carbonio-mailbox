// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.ZimletConstants;
import com.zimbra.soap.base.ZimletGlobalConfigInfo;
import com.zimbra.soap.base.ZimletProperty;

@XmlAccessorType(XmlAccessType.NONE)
public class AdminZimletGlobalConfigInfo
implements ZimletGlobalConfigInfo {

    /**
     * @zm-api-field-tag global-zimlet-config-prop
     * @zm-api-field-description Global zimlet configuration property
     */
    @XmlElement(name=ZimletConstants.ZIMLET_TAG_PROPERTY /* property */, required=false)
    private List<AdminZimletProperty> properties = Lists.newArrayList();

    public AdminZimletGlobalConfigInfo() {
    }

    public void setProperties(Iterable <AdminZimletProperty> properties) {
        this.properties.clear();
        if (properties != null) {
            Iterables.addAll(this.properties,properties);
        }
    }

    public void addProperty(AdminZimletProperty property) {
        this.properties.add(property);
    }

    public List<AdminZimletProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public void setZimletProperties(Iterable<ZimletProperty> properties) {
        setProperties(AdminZimletProperty.fromInterfaces(properties));
    }

    @Override
    public void addZimletProperty(ZimletProperty property) {
        addProperty((AdminZimletProperty) property);
    }

    @Override
    public List<ZimletProperty> getZimletProperties() {
        return AdminZimletProperty.toInterfaces(properties);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("properties", properties);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
