// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Lists;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CosCountInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_COUNT_ACCOUNT_RESPONSE)
@XmlType(propOrder = {})
public class CountAccountResponse {

    /**
     * @zm-api-field-description Account count information by Class Of Service (COS)
     */
    @XmlElement(name=AdminConstants.E_COS, required=false)
    private List <CosCountInfo> cos = Lists.newArrayList();

    public CountAccountResponse() {
    }

    public CountAccountResponse setCos(Collection<CosCountInfo> cos) {
        this.cos.clear();
        if (cos != null) {
            this.cos.addAll(cos);
        }
        return this;
    }

    public CountAccountResponse addCos(CosCountInfo cos) {
        this.cos.add(cos);
        return this;
    }

    public List <CosCountInfo> getCos() {
        return Collections.unmodifiableList(cos);
    }
}
