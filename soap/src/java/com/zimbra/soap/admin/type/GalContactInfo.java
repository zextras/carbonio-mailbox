// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class GalContactInfo extends AdminAttrsImpl {

    /**
     * @zm-api-field-tag gal-contact-id
     * @zm-api-field-description Global Address List contact ID
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private String id;

    public GalContactInfo() {
        this((String) null);
    }

    public GalContactInfo(String id) { this.id = id; }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
}
