// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap.apidesc;

import com.zimbra.doc.soap.XmlElementDescription;

public class SoapApiElement extends SoapApiSimpleElement {
  private final String jaxb; /* Name of JAXB class associated with this element */

  /* no-argument constructor needed for deserialization */
  @SuppressWarnings("unused")
  private SoapApiElement() {
    super();
    jaxb = null;
  }

  public SoapApiElement(XmlElementDescription descNode) {
    super(descNode);
    Class<?> jaxbClass = descNode.getJaxbClass();
    jaxb = jaxbClass == null ? null : jaxbClass.getName();
  }

  @Override
  public String getJaxb() {
    return jaxb;
  }

  @Override
  public String getType() {
    return null;
  }
}
