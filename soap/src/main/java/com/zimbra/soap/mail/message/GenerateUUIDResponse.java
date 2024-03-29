// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GENERATE_UUID_RESPONSE)
public class GenerateUUIDResponse {

    /**
     * @zm-api-field-tag generated-uuid
     * @zm-api-field-description Generated globally unique UUID
     */
    @XmlValue
    private final String uuid;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GenerateUUIDResponse() {
        this(null);
    }

    public GenerateUUIDResponse(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() { return uuid; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("uuid", uuid)
            .toString();
    }
}
