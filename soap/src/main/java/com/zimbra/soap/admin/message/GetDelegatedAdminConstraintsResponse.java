// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ConstraintAttr;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_DELEGATED_ADMIN_CONSTRAINTS_RESPONSE)
public class GetDelegatedAdminConstraintsResponse {

    /**
     * @zm-api-field-description Constraint attributes
     */
    @XmlElement(name=AdminConstants.E_A, required=false)
    private List<ConstraintAttr> attrs = Lists.newArrayList();

    public GetDelegatedAdminConstraintsResponse() {
    }

    public void setAttrs(Iterable <ConstraintAttr> attrs) {
        this.attrs.clear();
        if (attrs != null) {
            Iterables.addAll(this.attrs,attrs);
        }
    }

    public GetDelegatedAdminConstraintsResponse addAttr(ConstraintAttr attr) {
        this.attrs.add(attr);
        return this;
    }

    public List<ConstraintAttr> getAttrs() {
        return Collections.unmodifiableList(attrs);
    }
}
