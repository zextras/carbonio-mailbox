// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
public class FailedTestInfo {

    /**
     * @zm-api-field-tag failed-test-name
     * @zm-api-field-description Failed test name
     */
    @XmlAttribute(name="name", required=true)
    private final String name;

    /**
     * @zm-api-field-tag failed-test-exec-seconds
     * @zm-api-field-description Failed test execution time
     */
    @XmlAttribute(name="execSeconds", required=true)
    private final String execSeconds;

    /**
     * @zm-api-field-tag failed-test-class
     * @zm-api-field-description Failed test class name
     */
    @XmlAttribute(name="class", required=true)
    private final String className;

    /**
     * @zm-api-field-tag throwable
     * @zm-api-field-description Text of any exception thrown during the test
     */
    @XmlValue
    private final String throwable;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private FailedTestInfo() {
        this((String) null, (String) null, (String) null, (String) null);
    }

    public FailedTestInfo(String name, String execSeconds,
                    String className, String throwable) {
        this.name = name;
        this.execSeconds = execSeconds;
        this.className = className;
        this.throwable = throwable;
    }

    public String getName() { return name; }
    public String getExecSeconds() { return execSeconds; }
    public String getClassName() { return className; }
    public String getThrowable() { return throwable; }
}
