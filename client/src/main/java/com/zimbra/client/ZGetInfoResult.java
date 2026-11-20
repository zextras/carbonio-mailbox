// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.util.ListUtil;
import com.zimbra.soap.account.message.GetInfoResponse;
import java.util.List;

public class ZGetInfoResult {

  private final GetInfoResponse data;
  private final long expiration;

	public ZGetInfoResult(GetInfoResponse res) {
    this.data = res;
    expiration = data.getLifetime() + System.currentTimeMillis();
  }

  void setSignatures(List<ZSignature> sigs) {
    data.setSignatures(ListUtil.newArrayList(sigs, SoapConverter.TO_SOAP_SIGNATURE::apply));
  }

	public String getName() {
    return data.getAccountName();
  }

	public ZPrefs getPrefs() {
    return new ZPrefs(data.getPrefsMultimap().asMap());
  }

  public ZFeatures getFeatures() {
    return new ZFeatures(data.getAttrsMultimap().asMap());
  }

  public String getRestURLBase() {
    return data.getRestUrl();
  }

	public Boolean getIsImapTracked() {
    return data.getIsTrackingIMAP();
  }

  @Override
  public String toString() {
    return String.format("[ZGetInfoResult %s]", getName());
  }

	public String getId() {
    return data.getAccountId();
  }

}
