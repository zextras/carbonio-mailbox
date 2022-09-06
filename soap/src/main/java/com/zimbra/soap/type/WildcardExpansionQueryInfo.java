// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class WildcardExpansionQueryInfo implements BaseQueryInfo {

  /**
   * @zm-api-field-tag wildcard-str
   * @zm-api-field-description Wildcard expansion string
   */
  @XmlAttribute(name = "str", required = true)
  private final String str;

  /**
   * @zm-api-field-tag wildcard-expanded
   * @zm-api-field-description If value is <b>1 (true)</b>, then the wildcard was expanded and the
   *     matches are included in the search. If value is <b>0 (false)</b> then the wildcard was not
   *     specific enough and therefore no wildcard matches are included (exact-match *is* included
   *     in results).
   */
  @XmlAttribute(name = "expanded", required = true)
  private final ZmBoolean expanded;

  /**
   * @zm-api-field-tag wildcard-num-expanded
   * @zm-api-field-description Number expanded
   */
  @XmlAttribute(name = "numExpanded", required = true)
  private final int numExpanded;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private WildcardExpansionQueryInfo() {
    this((String) null, false, -1);
  }

  public WildcardExpansionQueryInfo(String str, boolean expanded, int numExpanded) {
    this.str = str;
    this.expanded = ZmBoolean.fromBool(expanded);
    this.numExpanded = numExpanded;
  }

  public String getStr() {
    return str;
  }

  public boolean getExpanded() {
    return ZmBoolean.toBool(expanded);
  }

  public int getNumExpanded() {
    return numExpanded;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("str", str).add("expanded", expanded).add("numExpanded", numExpanded);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
