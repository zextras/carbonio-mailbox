// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface EmailInfoInterface {
  public EmailInfoInterface create(
      String address, String display, String personal, String addressType);

  public void setGroup(Boolean group);

  public void setCanExpandGroupMembers(Boolean canExpandGroupMembers);

  public String getAddress();

  public String getDisplay();

  public String getPersonal();

  public String getAddressType();

  public Boolean getGroup();

  public Boolean getCanExpandGroupMembers();
}
