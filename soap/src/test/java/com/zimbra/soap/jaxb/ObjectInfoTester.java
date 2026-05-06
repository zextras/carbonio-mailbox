// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.soap.account.type.ObjectInfo;

/**
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="object-info-tester")
public class ObjectInfoTester {
    @XmlElement(name="obj-info", required=true)
    private ObjectInfo objectInfo;
    public ObjectInfoTester() { }
    public ObjectInfoTester(ObjectInfo oi) { setObjectInfo(oi); }
    public ObjectInfo getObjectInfo() { return objectInfo; }
    public void setObjectInfo(ObjectInfo objectInfo) { this.objectInfo = objectInfo; }
}
