// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.soap.base.KeyAndValue;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/*
  <pref name="{name}" modified="{modified-time}">{value}</pref>
*/
@GraphQLType(name = GqlConstants.CLASS_PREF, description = "Preferences")
public class Pref implements KeyAndValue {

  /**
   * @zm-api-field-tag pref-name
   * @zm-api-field-description Preference name
   */
  @XmlAttribute(name = "name", required = true)
  private String name;

  /**
   * @zm-api-field-tag pref-modified-time
   * @zm-api-field-description Preference modified time (may not be present)
   */
  @XmlAttribute(name = "modified", required = false)
  private Long modifiedTimestamp;

  /**
   * @zm-api-field-tag pref-value
   * @zm-api-field-description Preference value
   */
  @XmlValue private String value;

  public Pref() {}

  public Pref(String name) {
    setName(name);
  }

  public Pref(String name, String value) {
    setName(name);
    setValue(value);
  }

  public static Pref createPrefWithNameAndValue(String name, String value) {
    return new Pref(name, value);
  }

  @GraphQLNonNull
  @GraphQLQuery(name = "name", description = "Preference name")
  public String getName() {
    return name;
  }

  @GraphQLInputField(name = "name", description = "Preference name")
  public void setName(@GraphQLNonNull String name) {
    this.name = name;
  }

  @GraphQLQuery(name = "modifiedTimestamp", description = "Preference modified time")
  public Long getModifiedTimestamp() {
    return modifiedTimestamp;
  }

  @GraphQLInputField(name = "modifiedTimestamp", description = "Preference modified time")
  public void setModifiedTimestamp(Long timestamp) {
    this.modifiedTimestamp = timestamp;
  }

  @Override
  @GraphQLQuery(name = "value", description = "Preference value")
  public String getValue() {
    return value;
  }

  @Override
  @GraphQLIgnore
  public void setValue(String value) {
    this.value = value;
  }

  public static Multimap<String, String> toMultimap(Iterable<Pref> prefs) {
    final Multimap<String, String> map = ArrayListMultimap.create();
    for (final Pref p : prefs) {
      map.put(p.getName(), p.getValue());
    }
    return map;
  }

  @Override
  @GraphQLIgnore
  public void setKey(String key) {
    setName(key);
  }

  @Override
  @GraphQLIgnore
  public String getKey() {
    return getName();
  }
}
