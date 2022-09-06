// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.FreeBusyUserInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "GetWorkingHoursResponse")
public class GetWorkingHoursResponse {

  /**
   * @zm-api-field-description Working hours information by user
   */
  @XmlElement(name = MailConstants.E_FREEBUSY_USER /* usr */, required = false)
  private List<FreeBusyUserInfo> freebusyUsers = Lists.newArrayList();

  public GetWorkingHoursResponse() {}

  public void setFreebusyUsers(Iterable<FreeBusyUserInfo> freebusyUsers) {
    this.freebusyUsers.clear();
    if (freebusyUsers != null) {
      Iterables.addAll(this.freebusyUsers, freebusyUsers);
    }
  }

  public GetWorkingHoursResponse addFreebusyUser(FreeBusyUserInfo freebusyUser) {
    this.freebusyUsers.add(freebusyUser);
    return this;
  }

  public List<FreeBusyUserInfo> getFreebusyUsers() {
    return Collections.unmodifiableList(freebusyUsers);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("freebusyUsers", freebusyUsers);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
