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
public class CosSelector {

  @XmlEnum
  public static enum CosBy {
    // case must match protocol
    id,
    name;

    public static CosBy fromString(String s) throws ServiceException {
      try {
        return CosBy.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  /**
   * @zm-api-field-tag cos-selector-by
   * @zm-api-field-description Select the meaning of <b>{cos-selector-key}</b>
   */
  @XmlAttribute(name = AdminConstants.A_BY)
  private final CosBy cosBy;

  /**
   * @zm-api-field-tag cos-selector-key
   * @zm-api-field-description The key used to identify the COS. Meaning determined by
   *     <b>{cos-selector-by}</b>
   */
  @XmlValue private final String key;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CosSelector() {
    this.cosBy = null;
    this.key = null;
  }

  public CosSelector(CosBy by, String key) {
    this.cosBy = by;
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public CosBy getBy() {
    return cosBy;
  }
}
