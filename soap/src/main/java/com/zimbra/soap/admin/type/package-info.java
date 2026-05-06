// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

@XmlSchema(
    xmlns = {
        @XmlNs(prefix="admin", namespaceURI = "urn:zimbraAdmin")
    },
    namespace = "urn:zimbraAdmin",
    elementFormDefault = XmlNsForm.QUALIFIED
)
@XmlAccessorType(XmlAccessType.NONE)

package com.zimbra.soap.admin.type;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
