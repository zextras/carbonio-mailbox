// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.GrantInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_GRANTS_RESPONSE)
public class GetGrantsResponse {

    /**
     * @zm-api-field-description Information about grants
     */
    @XmlElement(name=AdminConstants.E_GRANT, required=false)
    private List<GrantInfo> grants = Lists.newArrayList();

    public GetGrantsResponse() {
    }

    public void setGrants(Iterable <GrantInfo> grants) {
        this.grants.clear();
        if (grants != null) {
            Iterables.addAll(this.grants,grants);
        }
    }

    public GetGrantsResponse addGrant(GrantInfo grant) {
        this.grants.add(grant);
        return this;
    }


    public List<GrantInfo> getGrants() {
        return Collections.unmodifiableList(grants);
    }
}
