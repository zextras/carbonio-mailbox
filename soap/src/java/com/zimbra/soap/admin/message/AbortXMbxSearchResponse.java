// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.XMbxSearchConstants;
import com.zimbra.soap.admin.type.SearchNode;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=XMbxSearchConstants.E_ABORT_XMBX_SEARCH_RESPONSE)
@XmlType(propOrder = {})
public class AbortXMbxSearchResponse {

    /**
     * @zm-api-field-description Search task information
     */
    @XmlElement(name=XMbxSearchConstants.E_SrchTask /* searchtask */, required=true)
    private final SearchNode searchNode;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private AbortXMbxSearchResponse() {
        this((SearchNode) null);
    }

    public AbortXMbxSearchResponse(SearchNode searchNode) {
        this.searchNode = searchNode;
    }

    public SearchNode getSearchNode() { return searchNode; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("searchNode", searchNode);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
