// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description create address list response
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CREATE_ADDRESS_LIST_RESPONSE)
public class CreateAddressListResponse {
  /**
   * @zm-api-field-tag id
   * @zm-api-field-description zimbra id of the created address list
   */
  @XmlElement(name = AdminConstants.E_ID /* id */, required = true)
  private String id;

  // default constructor
  public CreateAddressListResponse() {
    this("");
  }

  /**
   * @param id
   */
  public CreateAddressListResponse(String id) {
    this.id = id;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CreateAddressListResponse [id=" + id + "]";
  }
}
