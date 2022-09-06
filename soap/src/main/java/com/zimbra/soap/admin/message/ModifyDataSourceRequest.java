// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;
import com.zimbra.soap.admin.type.DataSourceInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Changes attributes of the given data source. Only the attributes
 *     specified in the request are modified. To change the name, specify
 *     <b>"zimbraDataSourceName"</b> as an attribute. <br>
 *     <br>
 *     note: this request is by default proxied to the account's home server
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_MODIFY_DATA_SOURCE_REQUEST)
public class ModifyDataSourceRequest extends AdminAttrsImpl {

  // Id for existing Account
  /**
   * @zm-api-field-tag account-id
   * @zm-api-field-description Existing account ID
   */
  @XmlAttribute(name = AdminConstants.E_ID, required = true)
  private final String id;

  /**
   * @zm-api-field-description Data source specification
   */
  @XmlElement(name = AccountConstants.E_DATA_SOURCE, required = true)
  private final DataSourceInfo dataSource;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ModifyDataSourceRequest() {
    this((String) null, (DataSourceInfo) null);
  }

  public ModifyDataSourceRequest(String id, DataSourceInfo dataSource) {
    this.id = id;
    this.dataSource = dataSource;
  }

  public String getId() {
    return id;
  }

  public DataSourceInfo getDataSource() {
    return dataSource;
  }
}
