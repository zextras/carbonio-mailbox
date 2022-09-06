// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zimbra.soap.json.jackson.ZmBooleanSerializer;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Zimbra SOAP uses the string "1" to represent true and the string "0" to represent false in XML
 * BUT Zimbra SOAP uses "true" and "false" in JSON. "0" and "1" are acceptable in XML
 *
 * <p>See this extract from XML Schema Part 2 at http://www.w3.org/TR/xmlschema-2/#boolean 3.2.2
 * boolean
 *
 * <p>[Definition:] boolean has the value space required to support the mathematical concept of :
 * binary-valued logic: {true, false}
 *
 * <p>3.2.2.1 Lexical representation
 *
 * <p>An instance of a datatype that is defined as boolean can have the following legal literals
 * {true, false, 1, 0}. 3.2.2.2 Canonical representation
 *
 * <p>The canonical representation for boolean is the set of literals {true, false}.
 *
 * <p>This enum has 4 valid values but is meant to represent just 2 values. It would be nice to use
 * something like: @XmlJavaTypeAdapter(ZmBooleanAdapter.class) to be 100% sure that only "0" and "1"
 * will get output but that causes schemagen to use type "xs:string" when writing types
 * corresponding to fields of this class instead of "zmBoolean".
 *
 * <p>JAXB class setters for fields of type ZmBoolean should go via Boolean to ensure that JAXB
 * objects only get populated with ZmBoolean.ZERO and ZmBoolean.ONE
 */
@XmlType
@XmlEnum(String.class)
@JsonSerialize(using = ZmBooleanSerializer.class)
public enum ZmBoolean {
  @XmlEnumValue("0")
  ZERO,
  @XmlEnumValue("1")
  ONE,
  @XmlEnumValue("false")
  FALSE,
  @XmlEnumValue("true")
  TRUE;

  /** Convert {@link Boolean} to {@link ZmBoolean} equivalent - May return null */
  public static ZmBoolean fromBool(Boolean val) {
    if (val == null) {
      return null;
    } else {
      return val ? ONE : ZERO;
    }
  }

  /**
   * Convert {@link Boolean} to ZmBoolean equivalent - If {@code val} is null returns equivalent of
   * {@code defaultval}
   */
  public static ZmBoolean fromBool(Boolean val, boolean defaultVal) {
    return (val == null) ? fromBool(defaultVal) : fromBool(val);
  }

  /** Convert {@link ZmBoolean} to {@link Boolean} equivalent - May return null */
  public static Boolean toBool(ZmBoolean val) {
    return (val == null) ? null : ((val.equals(ONE)) || (val.equals(TRUE)));
  }

  /**
   * Convert {@link ZmBoolean} to boolean equivalent - If {@code val} is null returns {@code
   * defaultval}
   */
  public static boolean toBool(ZmBoolean val, boolean defaultVal) {
    return (val == null) ? defaultVal : ((val.equals(ONE)) || (val.equals(TRUE)));
  }

  /**
   * Convert {@link Boolean} to boolean equivalent - If {@code val} is null returns {@code
   * defaultval}
   */
  public static boolean toBool(Boolean val, boolean defaultVal) {
    return (val == null) ? defaultVal : val;
  }
}
