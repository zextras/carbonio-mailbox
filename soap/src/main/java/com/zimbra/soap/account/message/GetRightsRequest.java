// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.Right;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get account level rights. <br>
 *     If no <b>&lt;ace></b> elements are provided, all ACEs are returned in the response. <br>
 *     If <b>&lt;ace></b> elements are provided, only those ACEs with specified rights are returned
 *     in the response.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_GET_RIGHTS_REQUEST)
public class GetRightsRequest {

  /**
   * @zm-api-field-description Specify Access Control Entries to return
   */
  @XmlElement(name = AccountConstants.E_ACE /* ace */, required = false)
  private List<Right> aces = Lists.newArrayList();

  public GetRightsRequest() {}

  public void setAces(Iterable<Right> aces) {
    this.aces.clear();
    if (aces != null) {
      Iterables.addAll(this.aces, aces);
    }
  }

  public GetRightsRequest addAce(Right ace) {
    this.aces.add(ace);
    return this;
  }

  public List<Right> getAces() {
    return Collections.unmodifiableList(aces);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("aces", aces);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
