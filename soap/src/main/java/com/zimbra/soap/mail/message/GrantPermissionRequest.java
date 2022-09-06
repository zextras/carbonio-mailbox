// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.AccountACEinfo;
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
 * @zm-api-command-deprecation-info Note: to be deprecated in Zimbra 9. Use zimbraAccount
 *     GrantRights instead.
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Grant account level permissions <br>
 *     GrantPermissionResponse returns permissions that are successfully granted.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GRANT_PERMISSION_REQUEST)
public class GrantPermissionRequest {

  /**
   * @zm-api-field-description Specify Access Control Entries (ACEs)
   */
  @XmlElement(name = MailConstants.E_ACE /* ace */, required = false)
  private List<AccountACEinfo> aces = Lists.newArrayList();

  public GrantPermissionRequest() {}

  public void setAces(Iterable<AccountACEinfo> aces) {
    this.aces.clear();
    if (aces != null) {
      Iterables.addAll(this.aces, aces);
    }
  }

  public GrantPermissionRequest addAce(AccountACEinfo ace) {
    this.aces.add(ace);
    return this;
  }

  public List<AccountACEinfo> getAces() {
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
