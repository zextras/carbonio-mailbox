// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ZimletInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ALL_ZIMLETS_RESPONSE)
public class GetAllZimletsResponse {

  /**
   * @zm-api-field-description Information about zimlets
   */
  @XmlElement(name = AdminConstants.E_ZIMLET, required = false)
  private List<ZimletInfo> zimlets = Lists.newArrayList();

  public GetAllZimletsResponse() {}

  public void setZimlets(Iterable<ZimletInfo> zimlets) {
    this.zimlets.clear();
    if (zimlets != null) {
      Iterables.addAll(this.zimlets, zimlets);
    }
  }

  public GetAllZimletsResponse addZimlet(ZimletInfo zimlet) {
    this.zimlets.add(zimlet);
    return this;
  }

  public List<ZimletInfo> getZimlets() {
    return Collections.unmodifiableList(zimlets);
  }
}
