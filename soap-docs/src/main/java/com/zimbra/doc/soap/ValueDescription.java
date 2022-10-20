// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.zimbra.soap.type.ZmBoolean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlEnumValue;

public class ValueDescription {
  @JsonIgnore private static final Joiner PIPE_JOINER = Joiner.on("|");
  private final String valueName;
  private final String representation;
  private final String className;
  private ArrayList<String> enumConsts = null;

  private ValueDescription() {
    valueName = null;
    representation = null;
    className = null;
  }

  private ValueDescription(String valueName, Class<?> klass) {
    this.valueName = valueName;
    className = klass.getName();
    buildEnumConsts(klass);
    representation = buildRepresentation(klass);
  }

  public static ValueDescription create(Class<?> klass) {
    return new ValueDescription(null, klass);
  }

  public static ValueDescription create(String valueName, Class<?> klass) {
    return new ValueDescription(valueName, klass);
  }

  private void buildEnumConsts(Class<?> klass) {
    if (!klass.isEnum()) {
      enumConsts = null;
      return;
    }
    enumConsts = Lists.newArrayList();
    for (Field field : klass.getFields()) {
      if (field.isEnumConstant()) {
        XmlEnumValue xmlEnumVal = field.getAnnotation(XmlEnumValue.class);
        if (xmlEnumVal == null) {
          enumConsts.add(field.getName());
        } else {
          enumConsts.add(xmlEnumVal.value());
        }
      }
    }
  }

  private String getEnumRepresentation(Class<?> klass) {
    return PIPE_JOINER.join(enumConsts);
  }

  private String buildRepresentation(Class<?> klass) {
    String value;
    if (klass.isAssignableFrom(ZmBoolean.class)) {
      value = "0|1";
    } else if (klass.isEnum()) {
      value = getEnumRepresentation(klass);
    } else {
      String classNameBase =
          className.contains(".") ? className.substring(className.lastIndexOf('.') + 1) : className;
      if (valueName == null) {
        value = classNameBase;
      } else {
        if ("String".equals(classNameBase)) {
          if (Strings.isNullOrEmpty(valueName)) {
            value = "\"...\"";
          } else {
            value = String.format("{%s}", valueName);
          }
        } else {
          if (Strings.isNullOrEmpty(valueName)) {
            value = String.format("(%s)", classNameBase);
          } else {
            value = String.format("{%s} (%s)", valueName, classNameBase);
          }
        }
      }
    }
    return value;
  }

  public String getValueName() {
    return valueName;
  }

  public String getRepresentation() {
    return representation;
  }

  public String getClassName() {
    return className;
  }

  public ArrayList<String> getEnumConsts() {
    return enumConsts;
  }

  public boolean isSame(ValueDescription other) {
    if (other == null) {
      return false;
    }
    if (className == null) {
      return (other.getClassName() == null);
    } else {
      if (other.getClassName() == null) {
        return false;
      }
    }
    return false;
  }
}
