// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
public class DomainSelector {
  @XmlEnum
  public enum DomainBy {
    // case must match protocol
    id,
    name,
    virtualHostname,
    krb5Realm,
    foreignName;

    public static DomainBy fromString(String s) throws ServiceException {
      try {
        return DomainBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }

    public com.zimbra.common.account.Key.DomainBy toKeyDomainBy() throws ServiceException {
      return com.zimbra.common.account.Key.DomainBy.fromString(this.name());
    }
  }

  /**
   * @zm-api-field-tag domain-selector-by
   * @zm-api-field-description Select the meaning of <b>{domain-selector-key}</b>
   */
  @XmlAttribute(name = AdminConstants.A_BY)
  private final DomainBy domainBy;

  /**
   * @zm-api-field-tag domain-selector-key
   * @zm-api-field-description The key used to identify the domain. Meaning determined by
   *     <b>{domain-selector-by}</b>
   */
  @XmlValue private final String key;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DomainSelector() {
    this(null, null);
  }

  public DomainSelector(DomainBy by, String key) {
    this.domainBy = by;
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public DomainBy getBy() {
    return domainBy;
  }

  public static DomainSelector fromId(String id) {
    return new DomainSelector(DomainBy.id, id);
  }

  public static DomainSelector fromName(String name) {
    return new DomainSelector(DomainBy.name, name);
  }
}
