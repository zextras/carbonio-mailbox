// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DistributionListInfo;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_DISTRIBUTION_LIST_RESPONSE)
public class GetDistributionListResponse {

  /*
   * more/total are only present if the list of members is given
   */
  /**
   * @zm-api-field-tag more-flag
   * @zm-api-field-description <b>1 (true)</b> if more mailboxes left to return <br>
   *     Only present if the list of members is given
   */
  @XmlAttribute(name = AdminConstants.A_MORE, required = false)
  ZmBoolean more;

  /**
   * @zm-api-field-tag total-members
   * @zm-api-field-description Total number of members (not affected by limit/offset) <br>
   *     Only present if the list of members is given
   */
  @XmlAttribute(name = AdminConstants.A_TOTAL, required = false)
  Integer total;

  /**
   * @zm-api-field-description Information about distribution list
   */
  @XmlElement(name = AdminConstants.E_DL, required = false)
  DistributionListInfo dl;

  public GetDistributionListResponse() {
    this((DistributionListInfo) null, (Boolean) null, (Integer) null);
  }

  public GetDistributionListResponse(DistributionListInfo dl, Boolean more, Integer total) {
    setDl(dl);
    setMore(more);
    setTotal(total);
  }

  public void setDl(DistributionListInfo dl) {
    this.dl = dl;
  }

  public void setMore(Boolean more) {
    this.more = ZmBoolean.fromBool(more);
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public DistributionListInfo getDl() {
    return dl;
  }

  public Boolean isMore() {
    return ZmBoolean.toBool(more);
  }

  public Integer getTotal() {
    return total;
  }
}
