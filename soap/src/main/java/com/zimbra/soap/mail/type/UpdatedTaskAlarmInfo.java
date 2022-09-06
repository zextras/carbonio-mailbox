// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public class UpdatedTaskAlarmInfo extends UpdatedAlarmInfo {

  public UpdatedTaskAlarmInfo() {
    this(null);
  }

  public UpdatedTaskAlarmInfo(String calItemId) {
    super(calItemId);
  }
}
