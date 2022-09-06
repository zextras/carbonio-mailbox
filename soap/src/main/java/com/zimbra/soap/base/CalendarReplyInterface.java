// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface CalendarReplyInterface extends RecurIdInfoInterface {
  public void setSentBy(String sentBy);

  public void setPartStat(String partStat);

  public int getSeq();

  public long getDate();

  public String getAttendee();

  public String getSentBy();

  public String getPartStat();
}
