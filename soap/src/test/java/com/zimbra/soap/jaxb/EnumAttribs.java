// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/** Test JAXB class with 2 enum XmlAttributes */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="enumEttribs")
public class EnumAttribs {
    @XmlAttribute(name="fold1", required=true)
    private ViewEnum fold1;
    @XmlAttribute(name="fold2", required=true)
    private ViewEnum fold2;

    public EnumAttribs() {}

    public ViewEnum getFold1() { return fold1; }
    public void setFold1(ViewEnum fold1) { this.fold1 = fold1; }
    public ViewEnum getFold2() { return fold2; }
    public void setFold2(ViewEnum fold2) { this.fold2 = fold2; }
}
