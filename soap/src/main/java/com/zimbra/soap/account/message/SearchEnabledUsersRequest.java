package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-description Search Users Enabled to a particular feature
 * <br />
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name= AccountConstants.E_SEARCH_ENABLED_USERS_REQUEST)
public class SearchEnabledUsersRequest {
  /**
   * @zm-api-field-description Query string - should be an LDAP-style filter string (RFC 2254)
   */
  @XmlAttribute(name=AccountConstants.E_NAME, required=false)
  private String name;
  /**
   * @zm-api-field-description The maximum number of accounts to return (0 is default and means all)
   */
  @XmlAttribute(name=AccountConstants.A_LIMIT, required=false)
  private Integer limit;

  /**
   * @zm-api-field-description The starting offset (0, 25, etc)
   */
  @XmlAttribute(name=AccountConstants.A_OFFSET, required=false)
  private Integer offset;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }
}
