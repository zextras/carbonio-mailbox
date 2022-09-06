// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.NamedElement;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Returns admin saved searches. <br>
 *     If no <b>&lt;search></b> is present server will return all saved searches.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ADMIN_SAVED_SEARCHES_REQUEST)
public class GetAdminSavedSearchesRequest {

  /**
   * @zm-api-field-description Search information
   */
  @XmlElement(name = AdminConstants.E_SEARCH, required = false)
  private List<NamedElement> searches = Lists.newArrayList();

  public GetAdminSavedSearchesRequest() {}

  public void setSearches(Iterable<NamedElement> searches) {
    this.searches.clear();
    if (searches != null) {
      Iterables.addAll(this.searches, searches);
    }
  }

  public GetAdminSavedSearchesRequest addSearch(NamedElement search) {
    this.searches.add(search);
    return this;
  }

  public List<NamedElement> getSearches() {
    return Collections.unmodifiableList(searches);
  }
}
