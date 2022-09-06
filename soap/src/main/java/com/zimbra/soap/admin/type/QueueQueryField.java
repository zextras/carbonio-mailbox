// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class QueueQueryField {

  /**
   * @zm-api-field-tag field-name
   * @zm-api-field-description Field name
   */
  @XmlAttribute(name = AdminConstants.A_NAME, required = true)
  private final String name;

  /**
   * @zm-api-field-description Match specification
   */
  @XmlElement(name = AdminConstants.E_MATCH, required = false)
  private List<ValueAttrib> matches = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private QueueQueryField() {
    this((String) null);
  }

  public QueueQueryField(String name) {
    this.name = name;
  }

  public void setMatches(Iterable<ValueAttrib> matches) {
    this.matches.clear();
    if (matches != null) {
      Iterables.addAll(this.matches, matches);
    }
  }

  public QueueQueryField addMatch(ValueAttrib match) {
    this.matches.add(match);
    return this;
  }

  public String getName() {
    return name;
  }

  public List<ValueAttrib> getMatches() {
    return Collections.unmodifiableList(matches);
  }
}
