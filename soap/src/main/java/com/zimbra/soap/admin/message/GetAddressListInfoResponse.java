// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.EntrySearchFilterInfo;
import com.zimbra.soap.type.GalSearchType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description get address list info response
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ADDRESS_LIST_INFO_RESPONSE)
public class GetAddressListInfoResponse {

  /**
   * @zm-api-field-tag id
   * @zm-api-field-description zimbra id of the address list
   */
  @XmlAttribute(name = AdminConstants.E_ID /* id */, required = true)
  private String id;

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description name of the address list
   */
  @XmlAttribute(name = AdminConstants.E_NAME /* name */, required = true)
  private String name;

  /**
   * @zm-api-field-tag desc
   * @zm-api-field-description description of the address list
   */
  @XmlElement(name = AdminConstants.E_DESC /* desc */, required = false)
  private String desc;

  /**
   * @zm-api-field-tag domain
   * @zm-api-field-description domain of the address list
   */
  @XmlElement(name = AdminConstants.E_DOMAIN, /* domain */ required = false)
  private DomainSelector domain;

  /**
   * @zm-api-field-tag searchFilter
   * @zm-api-field-description search filter for conditions
   */
  @XmlElement(name = AdminConstants.E_SEARCH_FILTER /* searchFilter */, required = false)
  private EntrySearchFilterInfo searchFilter;

  /**
   * @zm-api-field-tag type
   * @zm-api-field-description gal search type
   */
  @XmlAttribute(name = AdminConstants.A_TYPE /* type */, required = true)
  private GalSearchType type;

  /**
   * @return The id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id The id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the desc
   */
  public String getDesc() {
    return desc;
  }

  /**
   * @param desc the desc to set
   */
  public void setDesc(String desc) {
    this.desc = desc;
  }

  /**
   * @return the domain
   */
  public DomainSelector getDomain() {
    return domain;
  }

  /**
   * @param domain the domain to set
   */
  public void setDomain(DomainSelector domain) {
    this.domain = domain;
  }

  /**
   * @return the searchFilter
   */
  public EntrySearchFilterInfo getSearchFilter() {
    return searchFilter;
  }

  /**
   * @param searchFilter the searchFilter to set
   */
  public void setSearchFilter(EntrySearchFilterInfo searchFilter) {
    this.searchFilter = searchFilter;
  }

  /**
   * @return the type
   */
  public GalSearchType getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(GalSearchType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "GetAddressListInfoResponse [type="
        + type
        + ", id="
        + id
        + ", name="
        + name
        + ", desc="
        + desc
        + ", searchFilter="
        + searchFilter
        + "]";
  }
}
