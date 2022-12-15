// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.soap.account.type.Attr;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;

/**
 * Test {@link ZimbraKeyValuePairs} annotation, where the key/value pairs use different key/value names
 * to the default
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="key-value-pairs-tester")
public class OddKeyValuePairsTester {
    @XmlElement(name="oddElemName")
    @ZimbraKeyValuePairs
    private List<Attr> attrList;

    public OddKeyValuePairsTester() { }
    public OddKeyValuePairsTester(List<Attr> attrs) { setAttrList(attrs); }

    public List<Attr> getAttrList() { return attrList; }
    public void setAttrList(List<Attr> attrList) { this.attrList = attrList; }
}
