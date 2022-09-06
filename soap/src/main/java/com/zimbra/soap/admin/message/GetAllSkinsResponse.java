// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.NamedElement;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ALL_SKINS_RESPONSE)
public class GetAllSkinsResponse {

  /**
   * @zm-api-field-description Skins
   */
  @XmlElement(name = AdminConstants.E_SKIN, required = false)
  private List<NamedElement> skins = Lists.newArrayList();

  public GetAllSkinsResponse() {}

  public void setSkins(Iterable<NamedElement> skins) {
    this.skins.clear();
    if (skins != null) {
      Iterables.addAll(this.skins, skins);
    }
  }

  public GetAllSkinsResponse addSkin(NamedElement skin) {
    this.skins.add(skin);
    return this;
  }

  public List<NamedElement> getSkins() {
    return Collections.unmodifiableList(skins);
  }
}
