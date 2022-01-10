// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.AccountACEInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_GET_RIGHTS_RESPONSE)
@XmlType(propOrder = {})
public class GetRightsResponse {

    /**
     * @zm-api-field-description  Access Control Entries
     */
    @XmlElement(name=AccountConstants.E_ACE /* ace */, required=false)
    private List<AccountACEInfo> aces = Lists.newArrayList();

    public GetRightsResponse() {
    }

    public void setAces(Iterable <AccountACEInfo> aces) {
        this.aces.clear();
        if (aces != null) {
            Iterables.addAll(this.aces,aces);
        }
    }

    public GetRightsResponse addAce(AccountACEInfo ace) {
        this.aces.add(ace);
        return this;
    }

    public List<AccountACEInfo> getAces() {
        return Collections.unmodifiableList(aces);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("aces", aces);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
