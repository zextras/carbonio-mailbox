// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class LinkInfo {

  /**
   * @zm-api-field-tag shared-item-id
   * @zm-api-field-description Shared item ID
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = true)
  private final String id;

  /**
   * @zm-api-field-tag uuid
   * @zm-api-field-description Item's UUID - a globally unique identifier
   */
  @XmlAttribute(name = MailConstants.A_UUID /* uuid */, required = true)
  private final String uuid;

  /**
   * @zm-api-field-tag item-name
   * @zm-api-field-description Item name
   */
  @XmlAttribute(name = MailConstants.A_NAME /* name */, required = true)
  private final String name;

  /**
   * @zm-api-field-tag item-type
   * @zm-api-field-description Item type
   */
  @XmlAttribute(name = MailConstants.A_DEFAULT_VIEW /* view */, required = true)
  private final String defaultView;

  /**
   * @zm-api-field-tag permissions-granted
   * @zm-api-field-description Permissions granted
   */
  @XmlAttribute(name = MailConstants.A_RIGHTS /* perm */, required = false)
  private final String rights;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private LinkInfo() {
    this((String) null, (String) null, (String) null, (String) null, (String) null);
  }

  public LinkInfo(String id, String uuid, String name, String defaultView, String rights) {
    this.id = id;
    this.uuid = uuid;
    this.name = name;
    this.defaultView = defaultView;
    this.rights = rights;
  }

  public String getId() {
    return id;
  }

  public String getUuid() {
    return uuid;
  }

  public String getName() {
    return name;
  }

  public String getDefaultView() {
    return defaultView;
  }

  public String getRights() {
    return rights;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("id", id)
        .add("uuid", uuid)
        .add("name", name)
        .add("defaultView", defaultView)
        .add("rights", rights);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
