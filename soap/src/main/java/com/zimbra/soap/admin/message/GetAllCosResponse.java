// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CosInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ALL_COS_RESPONSE)
@XmlType(propOrder = {})
public class GetAllCosResponse {

  /**
   * @zm-api-field-description Information on Classes of Service (COS)
   */
  @XmlElement(name = AdminConstants.E_COS)
  private List<CosInfo> cosList = Lists.newArrayList();

  public GetAllCosResponse() {}

  public List<CosInfo> getCosList() {
    return Collections.unmodifiableList(cosList);
  }

  public GetAllCosResponse setCosList(Iterable<CosInfo> cosList) {
    this.cosList.clear();
    if (cosList != null) {
      Iterables.addAll(this.cosList, cosList);
    }
    return this;
  }

  public GetAllCosResponse addCos(CosInfo cos) {
    cosList.add(cos);
    return this;
  }
}
