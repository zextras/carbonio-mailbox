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

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Modifies admin saved searches.
 * <br />
 * Returns the admin saved searches.
 * <br />
 * <br />
 * If <b>{search-query}</b> is empty => delete the search if it exists
 * <br />
 * If <b>{search-name}</b> already exists => replace with new <b>{search-query}</b>
 * <br />
 * If <b>{search-name}</b> does not exist => save as a new search
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MODIFY_ADMIN_SAVED_SEARCHES_REQUEST)
public class ModifyAdminSavedSearchesRequest {

    @XmlElement(name=AdminConstants.E_SEARCH, required=false)
    private List<NamedValue> searches = Lists.newArrayList();

    public ModifyAdminSavedSearchesRequest() {
    }

    public void setSearches(Iterable <NamedValue> searches) {
        this.searches.clear();
        if (searches != null) {
            Iterables.addAll(this.searches,searches);
        }
    }

    public ModifyAdminSavedSearchesRequest addSearch(NamedValue search) {
        this.searches.add(search);
        return this;
    }

    public List<NamedValue> getSearches() {
        return Collections.unmodifiableList(searches);
    }
}
