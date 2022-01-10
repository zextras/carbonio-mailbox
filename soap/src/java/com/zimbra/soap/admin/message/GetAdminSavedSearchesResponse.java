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
import com.zimbra.soap.type.NamedValue;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ADMIN_SAVED_SEARCHES_RESPONSE)
public class GetAdminSavedSearchesResponse {

    /**
     * @zm-api-field-description Information on saved searches
     */
    @XmlElement(name=AdminConstants.E_SEARCH, required=false)
    private List<NamedValue> searches = Lists.newArrayList();

    public GetAdminSavedSearchesResponse() {
    }

    public void setSearches(Iterable <NamedValue> searches) {
        this.searches.clear();
        if (searches != null) {
            Iterables.addAll(this.searches,searches);
        }
    }

    public GetAdminSavedSearchesResponse addSearch(NamedValue search) {
        this.searches.add(search);
        return this;
    }

    public List<NamedValue> getSearches() {
        return Collections.unmodifiableList(searches);
    }
}
