// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.Right;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * Delete this class in bug 66989
 */

/**
 * @zm-api-command-deprecation-info Note: to be deprecated in Zimbra 9. Use zimbraAccount GetRights
 *     instead.
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get account level permissions <br>
 *     If no <b>&lt;ace></b> elements are provided, all ACEs are returned in the response. <br>
 *     If <b>&lt;ace></b> elements are provided, only those ACEs with specified rights are returned
 *     in the response.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_PERMISSION_REQUEST)
public class GetPermissionRequest {

  /**
   * @zm-api-field-description Specification of rights
   */
  @XmlElement(name = MailConstants.E_ACE /* ace */, required = false)
  private List<Right> aces = Lists.newArrayList();

  public GetPermissionRequest() {}

  public void setAces(Iterable<Right> aces) {
    this.aces.clear();
    if (aces != null) {
      Iterables.addAll(this.aces, aces);
    }
  }

  public GetPermissionRequest addAce(Right ace) {
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
