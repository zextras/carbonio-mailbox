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
public class ZimletStatusCos {

  /**
   * @zm-api-field-tag cos-name
   * @zm-api-field-description Class Of Service (COS) name
   */
  @XmlAttribute(name = AdminConstants.A_NAME, required = true)
  private final String name;

  /**
   * @zm-api-field-description Information on zimlet status
   */
  @XmlElement(name = AdminConstants.E_ZIMLET, required = false)
  private List<ZimletStatus> zimlets = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ZimletStatusCos() {
    this((String) null);
  }

  public ZimletStatusCos(String name) {
    this.name = name;
  }

  public void setZimlets(Iterable<ZimletStatus> zimlets) {
    this.zimlets.clear();
    if (zimlets != null) {
      Iterables.addAll(this.zimlets, zimlets);
    }
  }

  public ZimletStatusCos addZimlet(ZimletStatus zimlet) {
    this.zimlets.add(zimlet);
    return this;
  }

  public String getName() {
    return name;
  }

  public List<ZimletStatus> getZimlets() {
    return Collections.unmodifiableList(zimlets);
  }
}
