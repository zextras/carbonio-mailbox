// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import com.zimbra.soap.type.SearchFilterCondition;

public interface EntrySearchFilterInterface {
    void setCondition(SearchFilterCondition condition);
    SearchFilterCondition getCondition();
}
