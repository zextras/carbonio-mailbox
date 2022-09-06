// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.base.LocaleInterface;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class LocaleInfo implements LocaleInterface {

  /**
   * @zm-api-field-tag locale-id
   * @zm-api-field-description Locale ID
   */
  @XmlAttribute(name = AccountConstants.A_ID /* id */, required = true)
  private final String id;

  /**
   * @zm-api-field-tag locale-own-name
   * @zm-api-field-description Name of the locale in the locale itself
   */
  @XmlAttribute(name = AccountConstants.A_NAME /* name */, required = true)
  private final String name;

  /**
   * @zm-api-field-tag locale-local-name
   * @zm-api-field-description Name of the locale in the users' locale
   */
  @XmlAttribute(name = AccountConstants.A_LOCAL_NAME /* localName */, required = false)
  private final String localName;

  /** no-argument constructor wanted by JAXB */
  private LocaleInfo() {
    this((String) null, (String) null, (String) null);
  }

  private LocaleInfo(String id, String name, String localName) {
    this.id = id;
    this.name = name;
    this.localName = localName;
  }

  public static LocaleInfo createForIdNameAndLocalName(String id, String name, String localName) {
    return new LocaleInfo(id, name, localName);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getLocalName() {
    return localName;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("name", name).add("localName", localName);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
