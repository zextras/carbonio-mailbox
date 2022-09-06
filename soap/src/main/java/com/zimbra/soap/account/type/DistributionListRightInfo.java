// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class DistributionListRightInfo {

  /**
   * @zm-api-field-description Right
   */
  @XmlAttribute(name = AccountConstants.A_RIGHT, required = true)
  private final String right;

  /**
   * @zm-api-field-description Grantees
   */
  @XmlElement(name = AccountConstants.E_GRANTEE, required = false)
  protected List<DistributionListGranteeInfo> grantees;

  public DistributionListRightInfo() {
    this(null);
  }

  public DistributionListRightInfo(String right) {
    this.right = right;
  }

  public String getRight() {
    return right;
  }

  public void addGrantee(DistributionListGranteeInfo grantee) {
    if (grantees == null) {
      grantees = Lists.newArrayList();
    }
    grantees.add(grantee);
  }

  public void setGrantees(List<DistributionListGranteeInfo> grantees) {
    this.grantees = null;
    if (grantees != null) {
      this.grantees = Lists.newArrayList();
      Iterables.addAll(this.grantees, grantees);
    }
  }

  public List<DistributionListGranteeInfo> getGrantees() {
    return grantees;
  }
}
