// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ShareNotificationInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_SHARE_NOTIFICATIONS_RESPONSE)
public class GetShareNotificationsResponse {

  /**
   * @zm-api-field-description Share notification information
   */
  @XmlElement(name = MailConstants.E_SHARE /* share */, required = false)
  private List<ShareNotificationInfo> shares = Lists.newArrayList();

  public GetShareNotificationsResponse() {}

  public void setShares(Iterable<ShareNotificationInfo> shares) {
    this.shares.clear();
    if (shares != null) {
      Iterables.addAll(this.shares, shares);
    }
  }

  public void addShare(ShareNotificationInfo share) {
    this.shares.add(share);
  }

  public List<ShareNotificationInfo> getShares() {
    return Collections.unmodifiableList(shares);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("shares", shares);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
