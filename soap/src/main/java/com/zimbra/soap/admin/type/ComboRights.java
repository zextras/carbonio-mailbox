// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ComboRights {

  /**
   * @zm-api-field-description Rights information
   */
  @XmlElement(name = AdminConstants.E_R, required = false)
  private List<ComboRightInfo> comboRights = Lists.newArrayList();

  public ComboRights() {}

  public ComboRights(Collection<ComboRightInfo> comboRights) {
    this.setComboRights(comboRights);
  }

  public ComboRights setComboRights(Collection<ComboRightInfo> comboRights) {
    this.comboRights.clear();
    if (comboRights != null) {
      this.comboRights.addAll(comboRights);
    }
    return this;
  }

  public ComboRights addComboRight(ComboRightInfo comboRight) {
    comboRights.add(comboRight);
    return this;
  }

  public List<ComboRightInfo> getComboRights() {
    return Collections.unmodifiableList(comboRights);
  }
}
