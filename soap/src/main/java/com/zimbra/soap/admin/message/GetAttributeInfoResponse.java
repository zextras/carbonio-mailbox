// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AttributeDescription;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ATTRIBUTE_INFO_RESPONSE)
@XmlType(propOrder = {})
public class GetAttributeInfoResponse {

  /**
   * @zm-api-field-description Attribute descriptions
   */
  @XmlElement(name = AdminConstants.E_A /* a */, required = false)
  private List<AttributeDescription> attrs = Lists.newArrayList();

  public GetAttributeInfoResponse() {}

  public void setAttrs(Iterable<AttributeDescription> attrs) {
    this.attrs.clear();
    if (attrs != null) {
      Iterables.addAll(this.attrs, attrs);
    }
  }

  public void addAttr(AttributeDescription attr) {
    this.attrs.add(attr);
  }

  public List<AttributeDescription> getAttrs() {
    return Collections.unmodifiableList(attrs);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("attrs", attrs);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
