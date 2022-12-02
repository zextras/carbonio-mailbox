// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap.apidesc;

import com.zimbra.doc.soap.XmlElementDescription;
import com.zimbra.soap.util.JaxbInfo;

public class SoapApiSimpleElement
implements SoapApiNamedElement {
    private final String name;
    private final String namespace;
    private final String type;
    private final String description;
    private final boolean required;
    private final boolean list;

    /* no-argument constructor needed for deserialization */
    protected SoapApiSimpleElement() {
        name = null;
        namespace = null;
        type = null;
        description = null;
        required = false;
        list = false;
    }

    public SoapApiSimpleElement(XmlElementDescription descNode) {
        name = descNode.getName();
        String ns = descNode.getTargetNamespace();
        if (ns == null || (JaxbInfo.DEFAULT_MARKER.equals(ns))) {
            namespace = null;
        } else {
            namespace = ns;
        }
        description = descNode.getRawDescription();
        list = !descNode.isSingleton();
        required = !descNode.isOptional();
        type = descNode.getTypeName();
    }

    @Override
    public String getName() { return name; }
    @Override
    public String getNamespace() { return namespace; }
    public String getDescription() { return description; }
    public boolean isRequired() { return required; }
    public boolean isList() { return list; }
    public String getType() { return type; }
    public String getJaxb() { return null; }
}
