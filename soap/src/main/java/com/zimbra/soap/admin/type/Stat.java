// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zimbra.common.soap.AdminConstants;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
public class Stat {

  @XmlTransient
  private static class FilterByName implements Predicate<Stat> {
    private String name;

    private FilterByName(String name) {
      this.name = name;
    }

    @Override
    public boolean apply(Stat input) {
      return (input != null && Objects.equal(input.getName(), name));
    }
  }

  public static Iterator<Stat> filterByName(Iterable<Stat> unfiltered, String statName) {
    return Iterables.filter(unfiltered, new FilterByName(statName)).iterator();
  }

  /**
   * @zm-api-field-tag stat-name
   * @zm-api-field-description Stat name
   */
  @XmlAttribute(name = AdminConstants.A_NAME /* name */, required = false)
  private String name;

  /**
   * @zm-api-field-tag stat-description
   * @zm-api-field-description Stat description
   */
  @XmlAttribute(name = AdminConstants.A_DESCRIPTION /* description */, required = false)
  private String description;

  /**
   * @zm-api-field-tag stat-value
   * @zm-api-field-description Stat value
   */
  @XmlValue private String value;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getValue() {
    return value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
