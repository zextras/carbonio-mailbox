// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CountObjectsType;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.type.ZmBoolean;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Count number of objects. <br>
 *     Returns number of objects of requested type. <br>
 *     <br>
 *     Note: For account/alias/dl, if a domain is specified, only entries on the specified domain
 *     are counted. If no domain is specified, entries on all domains are counted.
 *     <p>For domain, if onlyRelated attribute is true and the request is sent by a delegate or
 *     domain admin, counts only domain on which has rights, without requiring countDomain right.
 */
@XmlRootElement(name = AdminConstants.E_COUNT_OBJECTS_REQUEST)
public class CountObjectsRequest {

  /**
   * @zm-api-field-description Object type
   */
  @XmlAttribute(name = AdminConstants.A_TYPE /* type */, required = true)
  private CountObjectsType type;

  /**
   * @zm-api-field-description Get only related if delegated/domain admin
   */
  @XmlAttribute(name = AdminConstants.A_ONLY_RELATED /* onlyrelated */, required = false)
  private ZmBoolean onlyRelated;

  /**
   * @zm-api-field-description Domain
   */
  @XmlElement(name = AdminConstants.E_DOMAIN /* domain */, required = false)
  private final List<DomainSelector> domains = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  public CountObjectsRequest() {
    this(null, null);
  }

  public CountObjectsRequest(CountObjectsType type) {
    this(type, null);
  }

  public CountObjectsRequest(CountObjectsType type, DomainSelector domain) {
    setType(type);
    addDomain(domain);
  }

  public void setType(CountObjectsType type) {
    this.type = type;
  }

  public CountObjectsType getType() {
    return type;
  }

  public CountObjectsRequest setDomains(Collection<DomainSelector> domains) {
    this.domains.clear();
    if (domains != null) {
      this.domains.addAll(domains);
    }
    return this;
  }

  public CountObjectsRequest addDomain(DomainSelector domain) {
    if (domain != null) {
      domains.add(domain);
    }
    return this;
  }

  public List<DomainSelector> getDomains() {
    return Collections.unmodifiableList(domains);
  }

  public void setOnlyRelated(Boolean onlyRelated) {
    this.onlyRelated = ZmBoolean.fromBool(onlyRelated);
  }

  public Boolean getOnlyRelated() {
    return ZmBoolean.toBool(onlyRelated, false);
  }
}
