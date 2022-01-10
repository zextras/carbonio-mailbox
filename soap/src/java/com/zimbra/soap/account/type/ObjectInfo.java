// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.KeyValuePair;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;

/**
 * Information for an Object - attributes are encoded as Key/Value pairs in JSON - i.e. using "_attrs"
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class ObjectInfo {

    /**
     * @zm-api-field-tag object-name
     * @zm-api-field-description Name
     */
    @XmlAttribute(name=AccountConstants.A_NAME, required=true)
    private final String name;

    /**
     * @zm-api-field-tag object-id
     * @zm-api-field-description ID
     */
    @XmlAttribute(name=AccountConstants.A_ID, required=true)
    private final String id;

    /**
     * @zm-api-field-description Attributes
     */
    @ZimbraKeyValuePairs
    @XmlElement(name=AccountConstants.E_A /* a */, required=false)
    private final List<KeyValuePair> attrList;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ObjectInfo() {
        this(null, null, null);
    }

    public ObjectInfo(String id, String name, Collection <KeyValuePair> attrs) {
        this.name = name;
        this.id = id;
        this.attrList = new ArrayList<KeyValuePair>();
        if (attrs != null) {
            this.attrList.addAll(attrs);
        }
    }

    public String getName() { return name; }
    public String getId() { return id; }
    public List<? extends KeyValuePair> getAttrList() {
        return Collections.unmodifiableList(attrList);
    }
}
