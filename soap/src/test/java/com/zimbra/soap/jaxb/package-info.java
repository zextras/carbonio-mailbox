// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

@XmlSchema(
    xmlns = {@XmlNs(prefix = "zmTest", namespaceURI = "urn:zimbraTest")},
    namespace = "urn:zimbraTest",
    elementFormDefault = XmlNsForm.QUALIFIED)
@XmlAccessorType(XmlAccessType.NONE)
package com.zimbra.soap.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
