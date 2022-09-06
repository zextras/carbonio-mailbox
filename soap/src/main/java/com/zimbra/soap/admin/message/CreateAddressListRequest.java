// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
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
 * @zm-api-command-description crteate address list request
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CREATE_ADDRESS_LIST_REQUEST)
public class CreateAddressListRequest {

  /**
   * @zm-api-field-tag type
   * @zm-api-field-description gal search type
   */
  @XmlAttribute(name = AdminConstants.A_TYPE /* type */, required = false)
  private GalSearchType type;

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description name of the address list
   */
  @XmlElement(name = AdminConstants.E_NAME /* name */, required = true)
  private String name;

  /**
   * @zm-api-field-tag desc
   * @zm-api-field-description description of the address list
   */
  @XmlElement(name = AdminConstants.E_DESC /* desc */, required = false)
  private String desc;

  /**
   * @zm-api-field-tag searchFilter
   * @zm-api-field-description search filter for conditions
   */
  @XmlElement(name = AdminConstants.E_SEARCH_FILTER /* searchFilter */, required = false)
  private EntrySearchFilterInfo searchFilter;

  /**
   * @zm-api-field-tag domain
   * @zm-api-field-description Domain selector
   */
  @XmlElement(name = AdminConstants.E_DOMAIN, required = false)
  private final DomainSelector domain;

  /** default private constructor to block the usage */
  @SuppressWarnings("unused")
  private CreateAddressListRequest() {
    this(null);
  }

  /**
   * @param name
   */
  public CreateAddressListRequest(String name) {
    this(name, null, null, null, null);
  }

  /**
   * @param name
   * @param desc
   * @param type
   * @param searchFilter
   * @param domain
   */
  public CreateAddressListRequest(
      String name,
      String desc,
      GalSearchType type,
      EntrySearchFilterInfo searchFilter,
      DomainSelector domain) {
    this.name = name;
    this.desc = desc;
    this.type = type != null ? type : GalSearchType.all; // set gal search type to "all" if null
    this.searchFilter = searchFilter;
    this.domain = domain;
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
   * @return the domain
   */
  public DomainSelector getDomain() {
    return domain;
  }

  public void validateCreateAddressListRequest() throws ServiceException {
    if (type == null) {
      ZimbraLog.addresslist.debug("Setting gal search type to all.");
      type = GalSearchType.all;
    }
    if (StringUtil.isNullOrEmpty(name)) {
      ZimbraLog.addresslist.debug("Missing name input.");
      throw ServiceException.INVALID_REQUEST("Missing name input", null);
    }
    if (StringUtil.isNullOrEmpty(desc)) {
      desc = "";
    }
    if (searchFilter == null) {
      ZimbraLog.addresslist.debug("searchFilter is empty, so search all the contacts in GAL.");
    }
    if (domain == null) {
      ZimbraLog.addresslist.debug("Missing domain selector, auth account's domain will be used.");
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CreateAddressListRequest [type="
        + type
        + ", name="
        + name
        + ", desc="
        + desc
        + ", searchFilter="
        + searchFilter
        + ", domain="
        + domain
        + "]";
  }
}
