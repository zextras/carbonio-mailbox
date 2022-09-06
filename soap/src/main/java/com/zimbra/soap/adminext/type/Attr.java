// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.adminext.type;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.zclient.ZClientException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/*
<attr name="{name}">{value}</attr>
 */
public class Attr {

  public static Function<Attr, Attr> COPY =
      new Function<Attr, Attr>() {
        @Override
        public Attr apply(Attr from) {
          return new Attr(from);
        }
      };

  /**
   * @zm-api-field-tag attr-name
   * @zm-api-field-description Attribute name
   */
  @XmlAttribute(name = AdminConstants.A_NAME /* name */, required = true)
  private String name;

  /**
   * @zm-api-field-tag attr-value
   * @zm-api-field-description Attribute Value
   */
  @XmlValue private String value;

  public Attr() {}

  public Attr(Attr attr) {
    name = attr.getName();
    value = attr.getValue();
  }

  public Attr(String name) {
    setName(name);
  }

  public Attr(String name, String value) {
    setName(name);
    setValue(value);
  }

  public String getName() {
    return name;
  }

  public Attr setName(String name) {
    this.name = name;
    return this;
  }

  public String getValue() {
    return value;
  }

  public Attr setValue(String value) {
    this.value = value;
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("value", value).toString();
  }

  public static Multimap<String, String> toMultimap(List<Attr> attrs) {
    Multimap<String, String> map = ArrayListMultimap.create();
    if (attrs != null) {
      for (Attr a : attrs) {
        map.put(a.getName(), a.getValue());
      }
    }
    return map;
  }

  public static List<Attr> fromMultimap(Multimap<String, String> attrMap) {
    List<Attr> attrs = new ArrayList<Attr>();
    if (attrMap != null) {
      for (Map.Entry<String, String> entry : attrMap.entries()) {
        attrs.add(new Attr(entry.getKey(), entry.getValue()));
      }
    }
    return attrs;
  }

  public static List<Attr> fromMap(Map<String, ? extends Object> attrs) throws ServiceException {
    List<Attr> newAttrs = Lists.newArrayList();
    if (attrs == null) return newAttrs;

    for (Entry<String, ? extends Object> entry : attrs.entrySet()) {
      String key = (String) entry.getKey();
      Object value = entry.getValue();
      if (value == null) {
        newAttrs.add(new Attr(key, (String) null));
      } else if (value instanceof String) {
        newAttrs.add(new Attr(key, (String) value));
      } else if (value instanceof String[]) {
        String[] values = (String[]) value;
        if (values.length == 0) {
          // an empty array == removing the attr
          newAttrs.add(new Attr(key, (String) null));
        } else {
          for (String v : values) {
            newAttrs.add(new Attr(key, v));
          }
        }
      } else {
        throw ZClientException.CLIENT_ERROR(
            "invalid attr type: " + key + " " + value.getClass().getName(), null);
      }
    }
    return newAttrs;
  }
}
