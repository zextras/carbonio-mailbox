// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/** Test JAXB class to exercise a field annotated with {@link XmlElementRef} */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="elem-ref-tester")
public class ElementRefTester {
    @XmlElementRef(name="ignored-name-root-elem-name-used-instead", type=StringAttribIntValue.class)
    private StringAttribIntValue byRef;

    public ElementRefTester() { }

    public StringAttribIntValue getByRef() { return byRef; }
    public void setByRef(StringAttribIntValue byRef) { this.byRef = byRef; }
}
