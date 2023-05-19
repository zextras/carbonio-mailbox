// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import com.zimbra.soap.type.ShareInfo;

public interface GetShareInfoResponseInterface {
    void setShares(Iterable<ShareInfo> shares);
    GetShareInfoResponseInterface addShare(ShareInfo shar);
    List<ShareInfo> getShares();
}
