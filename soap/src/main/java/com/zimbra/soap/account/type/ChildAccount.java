// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.collect.Iterables;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.ZmBoolean;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/*
    <childAccount name="{child-account-name}" visible="0|1" id="{child-account-id}">
        <attrs>
           <attr name="{name}">{value}</attr>*
        </attrs>
    </childAccount>*

*/
@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name = GqlConstants.CLASS_CHILD_ACCOUNT, description = "")
public class ChildAccount {

  /**
   * @zm-api-field-tag child-account-id
   * @zm-api-field-description Child account ID
   */
  @XmlAttribute(name = AccountConstants.A_ID, required = true)
  private final String id;

  /**
   * @zm-api-field-tag child-account-name
   * @zm-api-field-description Child account name
   */
  @XmlAttribute(name = AccountConstants.A_NAME, required = true)
  private final String name;

  /**
   * @zm-api-field-tag child-account-is-visible
   * @zm-api-field-description Flag whether child account is visible or not
   */
  @XmlAttribute(name = AccountConstants.A_VISIBLE, required = true)
  private final ZmBoolean isVisible;

  /**
   * @zm-api-field-tag child-account-is-active
   * @zm-api-field-description Flag whether child account is active or not
   */
  @XmlAttribute(name = AccountConstants.A_ACTIVE, required = true)
  private final ZmBoolean isActive;

  /**
   * @zm-api-field-description Attributes of the child account, including <b>displayName</b>
   */
  @XmlElementWrapper(name = AccountConstants.E_ATTRS, required = false)
  @XmlElement(name = AccountConstants.E_ATTR, required = false)
  private List<Attr> attrs = new ArrayList<Attr>();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ChildAccount() {
    this((String) null, (String) null, false, false);
  }

  public ChildAccount(String id, String name, boolean isVisible, boolean isActive) {
    this(id, name, isVisible, isActive, (Iterable<Attr>) null);
  }

  public ChildAccount(
      String id, String name, boolean isVisible, boolean isActive, Iterable<Attr> attrs) {
    this.id = id;
    this.name = name;
    this.isVisible = ZmBoolean.fromBool(isVisible);
    this.isActive = ZmBoolean.fromBool(isActive);
    setAttrs(attrs);
  }

  @GraphQLQuery(
      name = GqlConstants.ATTRS,
      description = "Attributes of the child account, including displayName")
  public List<Attr> getAttrs() {
    return Collections.unmodifiableList(attrs);
  }

  public void setAttrs(Iterable<Attr> attrs) {
    this.attrs.clear();
    if (attrs != null) {
      Iterables.addAll(this.attrs, attrs);
    }
  }

  @GraphQLNonNull
  @GraphQLQuery(name = GqlConstants.ID, description = "Child account ID")
  public String getId() {
    return id;
  }

  @GraphQLNonNull
  @GraphQLQuery(name = GqlConstants.NAME, description = "Child account name")
  public String getName() {
    return name;
  }

  @GraphQLNonNull
  @GraphQLQuery(
      name = GqlConstants.IS_VISIBLE,
      description = "Flag whether child account is visible or not")
  public boolean isVisible() {
    return ZmBoolean.toBool(isVisible);
  }

  @GraphQLNonNull
  @GraphQLQuery(
      name = GqlConstants.IS_ACTIVE,
      description = "Flag whether child account is active or not")
  public boolean isActive() {
    return ZmBoolean.toBool(isActive);
  }
}
