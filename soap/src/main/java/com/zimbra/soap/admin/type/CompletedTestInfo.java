// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class CompletedTestInfo {

    /**
     * @zm-api-field-tag test-name
     * @zm-api-field-description Test name
     */
    @XmlAttribute(name="name", required=true)
    private final String name;

    /**
     * @zm-api-field-tag exec-seconds
     * @zm-api-field-description Number of seconds to execute the test
     */
    @XmlAttribute(name="execSeconds", required=true)
    private final String execSeconds;

    /**
     * @zm-api-field-tag test-class
     * @zm-api-field-description Test class
     */
    @XmlAttribute(name="class", required=true)
    private final String className;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CompletedTestInfo() {
        this((String) null, (String) null, (String) null);
    }

    public CompletedTestInfo(String name, String execSeconds, String className) {
        this.name = name;
        this.execSeconds = execSeconds;
        this.className = className;
    }

    public String getName() { return name; }
    public String getExecSeconds() { return execSeconds; }
    public String getClassName() { return className; }
}
