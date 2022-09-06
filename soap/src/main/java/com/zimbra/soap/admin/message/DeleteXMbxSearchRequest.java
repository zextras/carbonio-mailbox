// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.XMbxSearchConstants;
import com.zimbra.soap.admin.type.SearchID;
import com.zimbra.soap.type.AccountSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Attempts to delete a search task. <br>
 *     Returns empty <b>&lt;DeleteXMbxSearchResponse/></b> element on success or Fault document on
 *     error.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = XMbxSearchConstants.E_DELETE_XMBX_SEARCH_REQUEST)
public class DeleteXMbxSearchRequest {

  /**
   * @zm-api-field-description Search task information
   */
  @XmlElement(name = XMbxSearchConstants.E_SrchTask /* searchtask */, required = true)
  private final SearchID searchTask;

  /**
   * @zm-api-field-description Account
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT /* account */, required = false)
  private AccountSelector account;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DeleteXMbxSearchRequest() {
    this((SearchID) null, (AccountSelector) null);
  }

  /*public DeleteXMbxSearchRequest(SearchID searchTask) {
      this.searchTask = searchTask;
  }*/
  public DeleteXMbxSearchRequest(SearchID searchTask, AccountSelector account) {
    this.searchTask = searchTask;
    this.account = account;
  }

  public AccountSelector getAccount() {
    return account;
  }

  public SearchID getSearchTask() {
    return searchTask;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("searchTask", searchTask);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
