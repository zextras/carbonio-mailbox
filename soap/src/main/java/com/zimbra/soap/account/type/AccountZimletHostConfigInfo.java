// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.ZimletConstants;
import com.zimbra.soap.base.ZimletHostConfigInfo;
import com.zimbra.soap.base.ZimletProperty;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class AccountZimletHostConfigInfo
implements ZimletHostConfigInfo {

    /**
     * @zm-api-field-tag zimlet-host-name
     * @zm-api-field-description Designates the zimbra host name for the properties.
     * <br />
     * Must be a valid Zimbra host name
     */
    @XmlAttribute(name=ZimletConstants.ZIMLET_ATTR_NAME /* name */, required=false)
    private String name;

    /**
     * @zm-api-field-description Host specifice zimlet configuration properties
     */
    @XmlElement(name=ZimletConstants.ZIMLET_TAG_PROPERTY /* property */, required=false)
    private List<AccountZimletProperty> properties = Lists.newArrayList();

    public AccountZimletHostConfigInfo() {
    }

    private AccountZimletHostConfigInfo(String name) {
        setName(name);
    }

    public static AccountZimletHostConfigInfo createForName(String name) {
        return new AccountZimletHostConfigInfo(name);
    }

    @Override
    public void setName(String name) { this.name = name; }
    public void setProperties(Iterable <AccountZimletProperty> properties) {
        this.properties.clear();
        if (properties != null) {
            Iterables.addAll(this.properties,properties);
        }
    }

    public void addProperty(AccountZimletProperty property) {
        this.properties.add(property);
    }

    @Override
    public String getName() { return name; }
    public List<AccountZimletProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public void setZimletProperties(Iterable<ZimletProperty> properties) {
        setProperties(AccountZimletProperty.fromInterfaces(properties));
    }

    @Override
    public void addZimletProperty(ZimletProperty property) {
        addProperty((AccountZimletProperty) property);
    }

    @Override
    public List<ZimletProperty> getZimletProperties() {
        return AccountZimletProperty.toInterfaces(properties);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("properties", properties);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
