// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.base.GetShareInfoResponseInterface;
import com.zimbra.soap.type.ShareInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_GET_SHARE_INFO_RESPONSE)
public class GetShareInfoResponse implements GetShareInfoResponseInterface {

  /**
   * @zm-api-field-description Shares
   */
  @XmlElement(name = AccountConstants.E_SHARE, required = false)
  private final List<ShareInfo> shares = Lists.newArrayList();

  public GetShareInfoResponse() {}

  @Override
  public void setShares(Iterable<ShareInfo> shares) {
    this.shares.clear();
    if (shares != null) {
      Iterables.addAll(this.shares, shares);
    }
  }

  @Override
  public GetShareInfoResponse addShare(ShareInfo share) {
    this.shares.add(share);
    return this;
  }

  @Override
  public List<ShareInfo> getShares() {
    return Collections.unmodifiableList(shares);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("shares", shares).toString();
  }
}
