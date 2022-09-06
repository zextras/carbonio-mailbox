// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.EnumSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/*
         {types}   = comma-separated list.  Legal values are:
                     appointment|chat|contact|conversation|document|
                     message|tag|task|wiki
                     (default is &quot;conversation&quot;)

*/
@XmlEnum
public enum ItemType {
  @XmlEnumValue("appointment")
  APPOINTMENT,
  @XmlEnumValue("chat")
  CHAT,
  @XmlEnumValue("contact")
  CONTACT,
  @XmlEnumValue("conversation")
  CONVERSATION,
  @XmlEnumValue("document")
  DOCUMENT,
  @XmlEnumValue("message")
  MESSAGE,
  @XmlEnumValue("tag")
  TAG,
  @XmlEnumValue("task")
  TASK,
  @XmlEnumValue("wiki")
  WIKI;

  @Override
  public String toString() {
    return name().toLowerCase();
  }

  @XmlTransient
  static final class CSVAdapter extends XmlAdapter<String, Set<ItemType>> {
    @Override
    public String marshal(Set<ItemType> set) throws Exception {
      return Joiner.on(',').skipNulls().join(set);
    }

    @Override
    public Set<ItemType> unmarshal(String csv) throws Exception {
      Set<ItemType> result = EnumSet.noneOf(ItemType.class);
      for (String token : Splitter.on(',').trimResults().omitEmptyStrings().split(csv)) {
        result.add(ItemType.valueOf(token.toUpperCase()));
      }
      return result;
    }
  }
}
