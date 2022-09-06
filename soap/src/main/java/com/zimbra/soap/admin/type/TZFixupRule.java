// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class TZFixupRule {

  /**
   * @zm-api-field-description Match
   */
  @XmlElement(name = AdminConstants.E_MATCH /* match */, required = true)
  private TZFixupRuleMatch match;

  // Need either "touch" or "replace" but not both

  // Force sync clients to refetch
  /**
   * @zm-api-field-tag touch
   * @zm-api-field-description Force sync clients to refetch. <br>
   *     Need either "touch" or "replace" but not both
   */
  @XmlElement(name = AdminConstants.E_TOUCH /* touch */, required = false)
  private SimpleElement touch;

  /**
   * @zm-api-field-description Replace any matching timezone with this timezone <br>
   *     Need either "touch" or "replace" but not both
   */
  @XmlElement(name = AdminConstants.E_REPLACE /* replace */, required = false)
  private TZReplaceInfo replace;

  public TZFixupRule() {}

  public void setMatch(TZFixupRuleMatch match) {
    this.match = match;
  }

  public void setTouch(SimpleElement touch) {
    this.touch = touch;
  }

  public void setReplace(TZReplaceInfo replace) {
    this.replace = replace;
  }

  public TZFixupRuleMatch getMatch() {
    return match;
  }

  public SimpleElement getTouch() {
    return touch;
  }

  public TZReplaceInfo getReplace() {
    return replace;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("match", match).add("touch", touch).add("replace", replace);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
