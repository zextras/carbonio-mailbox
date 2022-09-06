// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.util;

import com.zimbra.common.util.ZimbraLog;
import java.lang.reflect.Type;
import javax.xml.bind.annotation.XmlValue;

public class JaxbValueInfo {
  private String fieldName;
  private Class<?> atomClass;

  public JaxbValueInfo(XmlValue annot, String fieldName, Type defaultGenericType) {
    this.fieldName = fieldName;
    atomClass = JaxbInfo.classFromType(defaultGenericType);
    if (atomClass == null) {
      ZimbraLog.soap.debug(
          "Unable to determine class for value field %s with annotation '%s'", fieldName, annot);
    }
  }

  public String getFieldName() {
    return fieldName;
  }

  /**
   * @return the class associated with the value
   */
  public Class<?> getAtomClass() {
    return atomClass;
  }
}
