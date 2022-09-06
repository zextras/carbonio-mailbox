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
public class CallLogItem extends VoiceCallItem {

  /**
   * @zm-api-field-description Parties involved in the call or voice mail. Information for both
   *     calling and called parties is returned
   */
  @XmlElement(name = VoiceConstants.E_CALLPARTY /* cp */, required = false)
  private List<CallLogCallParty> callParties = Lists.newArrayList();

  public CallLogItem() {}

  public void setCallParties(Iterable<CallLogCallParty> callParties) {
    this.callParties.clear();
    if (callParties != null) {
      Iterables.addAll(this.callParties, callParties);
    }
  }

  public void addCallParty(CallLogCallParty callParty) {
    this.callParties.add(callParty);
  }

  public List<CallLogCallParty> getCallParties() {
    return callParties;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    helper = super.addToStringInfo(helper);
    return helper.add("callParties", callParties);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
