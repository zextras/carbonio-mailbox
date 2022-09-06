// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.RightInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ALL_RIGHTS_RESPONSE)
public class GetAllRightsResponse {

  /**
   * @zm-api-field-description Information for rights
   */
  @XmlElement(name = AdminConstants.E_RIGHT, required = false)
  private List<RightInfo> rights = Lists.newArrayList();

  public GetAllRightsResponse() {}

  public GetAllRightsResponse(Collection<RightInfo> rights) {
    setRights(rights);
  }

  public GetAllRightsResponse setRights(Collection<RightInfo> rights) {
    this.rights.clear();
    if (rights != null) {
      this.rights.addAll(rights);
    }
    return this;
  }

  public GetAllRightsResponse addRight(RightInfo right) {
    rights.add(right);
    return this;
  }

  public List<RightInfo> getRights() {
    return Collections.unmodifiableList(rights);
  }
}
