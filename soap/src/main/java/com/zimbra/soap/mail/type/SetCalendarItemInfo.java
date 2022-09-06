// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class SetCalendarItemInfo {

  /**
   * @zm-api-field-tag participation-status
   * @zm-api-field-description iCalendar PTST (Participation status) <br>
   *     Valid values: <b>NE|AC|TE|DE|DG|CO|IN|WE|DF</b> <br>
   *     Meanings: <br>
   *     "NE"eds-action, "TE"ntative, "AC"cept, "DE"clined, "DG" (delegated), "CO"mpleted (todo),
   *     "IN"-process (todo), "WA"iting (custom value only for todo), "DF" (deferred; custom value
   *     only for todo)
   */
  @XmlAttribute(name = MailConstants.A_CAL_PARTSTAT /* ptst */, required = false)
  private String partStat;

  /**
   * @zm-api-field-description Message
   */
  @ZimbraUniqueElement
  @XmlElement(name = MailConstants.E_MSG /* m */, required = false)
  private Msg msg;

  public SetCalendarItemInfo() {}

  public void setPartStat(String partStat) {
    this.partStat = partStat;
  }

  public void setMsg(Msg msg) {
    this.msg = msg;
  }

  public String getPartStat() {
    return partStat;
  }

  public Msg getMsg() {
    return msg;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("partStat", partStat).add("msg", msg);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
