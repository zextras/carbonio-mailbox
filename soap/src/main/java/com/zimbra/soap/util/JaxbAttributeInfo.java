// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.util;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import java.lang.reflect.Type;
import javax.xml.bind.annotation.XmlAttribute;

public class JaxbAttributeInfo {
  private static final Log LOG = ZimbraLog.soap;
  private String name;
  private boolean required;
  private String fieldName;
  private String stamp;
  private Class<?> atomClass;

  public JaxbAttributeInfo(
      JaxbInfo jaxbInfo, XmlAttribute annot, String fieldName, Type defaultGenericType) {
    this.required = annot.required();
    this.fieldName = fieldName;
    stamp = jaxbInfo.getStamp() + "[attr=" + name + "]:";
    name = annot.name();
    if ((name == null) || JaxbInfo.DEFAULT_MARKER.equals(name)) {
      name = fieldName;
    }
    if (name == null) {
      LOG.debug("%s Ignoring element with annotation '%s' unable to determine name", stamp, annot);
    }
    atomClass = jaxbInfo.classFromType(defaultGenericType);
    if (atomClass == null) {
      LOG.debug(
          "%s Ignoring attribute with annotation '%s' unable to determine class", stamp, annot);
    }
  }

  public String getName() {
    return name;
  }

  public boolean isRequired() {
    return required;
  }

  public String getFieldName() {
    return fieldName;
  }

  public Class<?> getAtomClass() {
    return atomClass;
  }
}
