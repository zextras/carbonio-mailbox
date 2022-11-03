// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap.apidesc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zimbra.doc.soap.ValueDescription;
import com.zimbra.doc.soap.XmlAttributeDescription;
import com.zimbra.soap.JaxbUtil;

public class SoapApiAttribute {
  private final String name;
  private final String description;
  private final boolean required;
  private ValueDescription valueType = null;
  private String jaxb = null;

  /* no-argument constructor needed for deserialization */
  @SuppressWarnings("unused")
  private SoapApiAttribute() {
    name = null;
    description = null;
    required = false;
  }

  public SoapApiAttribute(XmlAttributeDescription desc) {
    name = desc.getName();
    description = desc.getRawDescription();
    valueType = desc.getValueDescription();
    Class<?> klass;
    try {
      klass = Class.forName(valueType.getClassName());
      if (JaxbUtil.isJaxbType(klass)) {
        jaxb = valueType.getClassName();
        valueType = null;
      }
    } catch (ClassNotFoundException e) {
      klass = null;
    }
    required = desc.isRequired();
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public boolean isRequired() {
    return required;
  }

  public ValueDescription getValueType() {
    return valueType;
  }

  public String getJaxb() {
    return jaxb;
  }

  @JsonIgnore
  public boolean isSame(SoapApiAttribute other) {
    if (other == null) {
      return false;
    }
    return true;
  }
}
