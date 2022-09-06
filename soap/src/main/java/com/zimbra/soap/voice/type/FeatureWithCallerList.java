// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.VoiceConstants;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class FeatureWithCallerList extends CallFeatureInfo {

  /**
   * @zm-api-field-description Phones
   */
  @XmlElement(name = VoiceConstants.E_PHONE /* phone */, required = false)
  private List<CallerListEntry> phones = Lists.newArrayList();

  public FeatureWithCallerList() {}

  public void setPhones(Iterable<CallerListEntry> phones) {
    this.phones.clear();
    if (phones != null) {
      Iterables.addAll(this.phones, phones);
    }
  }

  public void addPhone(CallerListEntry phone) {
    this.phones.add(phone);
  }

  public List<CallerListEntry> getPhones() {
    return phones;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    helper = super.addToStringInfo(helper);
    return helper.add("phones", phones);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
