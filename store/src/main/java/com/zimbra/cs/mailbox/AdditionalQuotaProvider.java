// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

public interface AdditionalQuotaProvider {
  long getAdditionalQuota(Mailbox mailbox);
}
