// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import com.zimbra.soap.type.ShareInfo;
import java.util.List;

public interface GetShareInfoResponseInterface {
  public void setShares(Iterable<ShareInfo> shares);

  public GetShareInfoResponseInterface addShare(ShareInfo shar);

  public List<ShareInfo> getShares();
}
