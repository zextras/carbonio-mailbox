// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AccountConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class DataSourceInfo extends AdminAttrsImpl {

  /**
   * @zm-api-field-tag data-source-name
   * @zm-api-field-description Data source name
   */
  @XmlAttribute(name = AccountConstants.A_NAME, required = true)
  private final String name;

  /**
   * @zm-api-field-tag data-source-id
   * @zm-api-field-description Data source id
   */
  @XmlAttribute(name = AccountConstants.A_ID, required = true)
  private final String id;

  /**
   * @zm-api-field-tag data-source-type
   * @zm-api-field-description Data source type
   */
  @XmlAttribute(name = AccountConstants.A_TYPE, required = true)
  private final DataSourceType type;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DataSourceInfo() {
    this((String) null, (String) null, (DataSourceType) null);
  }

  public DataSourceInfo(String name, String id, DataSourceType type) {
    this.name = name;
    this.id = id;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  public DataSourceType getType() {
    return type;
  }
}
